package self.project.web.ticket.service.config;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import self.project.web.ticket.service.security.DatabaseAuthenticationProvider;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
        "/api/auth/login",
        "/api/auth/csrf",
        "/api/init-db",

        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",

        "/actuator/health",
        "/actuator/health/**",
        "/actuator/info"
    };

    @Bean
    public AuthenticationManager authenticationManager(
        DatabaseAuthenticationProvider authenticationProvider
    ) {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy(
        CsrfTokenRepository csrfTokenRepository
    ) {
        ChangeSessionIdAuthenticationStrategy sessionIdStrategy =
            new ChangeSessionIdAuthenticationStrategy();

        CsrfAuthenticationStrategy csrfStrategy =
            new CsrfAuthenticationStrategy(csrfTokenRepository);

        return new CompositeSessionAuthenticationStrategy(
            List.of(
                sessionIdStrategy,
                csrfStrategy
            )
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        SecurityContextRepository securityContextRepository,
        CsrfTokenRepository csrfTokenRepository,
        SessionAuthenticationStrategy sessionAuthenticationStrategy
    ) throws Exception {

        http
            .securityContext(context -> context
                .securityContextRepository(
                    securityContextRepository
                )
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(
                    SessionCreationPolicy.IF_REQUIRED
                )
                .sessionAuthenticationStrategy(
                    sessionAuthenticationStrategy
                )
            )

            .csrf(csrf -> csrf
                .csrfTokenRepository(
                    csrfTokenRepository
                )

                .ignoringRequestMatchers(
                    "/api/auth/login"
                )
            )

            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    PUBLIC_ENDPOINTS
                ).permitAll()

                .requestMatchers(
                    "/api/admin/**"
                ).hasRole("ADMIN")

                .anyRequest()
                .authenticated()
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

            .formLogin(
                AbstractHttpConfigurer::disable
            )

            .httpBasic(
                AbstractHttpConfigurer::disable
            )

            .logout(logout -> logout
                .logoutUrl(
                    "/api/auth/logout"
                )
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies(
                    "JSESSIONID"
                )
                .logoutSuccessHandler(
                    (request, response, authentication) ->
                        response.setStatus(
                            HttpServletResponse.SC_NO_CONTENT
                        )
                )
            );

        return http.build();
    }
}
