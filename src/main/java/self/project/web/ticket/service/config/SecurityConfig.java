package self.project.web.ticket.service.config;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import self.project.web.ticket.service.security.DatabaseAuthenticationProvider;
import self.project.web.ticket.service.security.LocalOidcUserService;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {"/api/auth/login", "/api/auth/refresh",
        "/api/auth/logout", "/api/init-db", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
        "/actuator/health/**"};
    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/login",
        "/api/auth/refresh",
        "/api/auth/logout",
        "/api/init-db",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/actuator/health/**"
    };

    @Bean
    public AuthenticationManager authenticationManager(DatabaseAuthenticationProvider authenticationProvider) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return authenticationConverter;
    }

    @Bean
    @Order(1)
    @ConditionalOnProperty(name = "security.oidc.enabled", havingValue = "true")
    public SecurityFilterChain oidcSecurityFilterChain(HttpSecurity http, LocalOidcUserService localOidcUserService, @Value("${app.frontend-url:http://localhost:5173}") String frontendUrl)
        throws Exception {

        http.securityMatcher("/oauth2/**", "/login/oauth2/**", "/api/auth/oidc/**")
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(
                authorize -> authorize.requestMatchers("/oauth2/**", "/login/oauth2/**",
                    "/api/auth/oidc/token").permitAll().anyRequest().authenticated())
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/oidc/token")).oauth2Login(
                oauth2 -> oauth2.userInfoEndpoint(
                        userInfo -> userInfo.oidcUserService(localOidcUserService::loadUser))
                    .successHandler((request, response, authentication) -> {
                        OidcUser oidcUser = (OidcUser) authentication.getPrincipal();

                        String email = oidcUser.getEmail();

                        request.getSession(true).setAttribute("OIDC_EMAIL", email);

                        System.out.println("OIDC SUCCESS: email=" + email);

                        System.out.println("OIDC SUCCESS: sessionId=" + request.getSession().getId());

                        System.out.println("OIDC SUCCESS: attribute=" + request.getSession()
                            .getAttribute("OIDC_EMAIL"));

                        response.sendRedirect(frontendUrl + "/oidc/callback");
                    }).failureHandler((request, response, exception) -> {
                        System.out.println("OIDC FAILURE: " + exception.getClass().getName());

                        System.out.println("OIDC FAILURE MESSAGE: " + exception.getMessage());

                        response.sendRedirect(frontendUrl + "/login?oidcError=true");
                    })).formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter)
        throws Exception {

        http.csrf(AbstractHttpConfigurer::disable).sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(
                authorize -> authorize.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN").anyRequest().authenticated())
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                (request, response, exception) -> response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED)).accessDeniedHandler(
                (request, response, exception) -> response.sendError(
                    HttpServletResponse.SC_FORBIDDEN))).oauth2ResourceServer(
                oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
            .formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
        JwtGrantedAuthoritiesConverter authoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
            new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
            authoritiesConverter
        );

        return authenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                .requestMatchers("/api/admin/**")
                .hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(
                    (request, response, exception) ->
                        response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED
                        )
                )
                .accessDeniedHandler(
                    (request, response, exception) ->
                        response.sendError(
                            HttpServletResponse.SC_FORBIDDEN
                        )
                )
            )

            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(
                        jwtAuthenticationConverter
                    )
                )
            )

            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)

            .logout(AbstractHttpConfigurer::disable);

        return http.build();
    }
}