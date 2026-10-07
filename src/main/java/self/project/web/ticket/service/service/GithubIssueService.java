package self.project.web.ticket.service.service;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.server.ResponseStatusException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.regex.Pattern;

@Service
public class GithubIssueService {
    private static final Pattern ISSUE_URL = Pattern.compile("https://github\\.com/([A-Za-z0-9_-]+)/([A-Za-z0-9_.-]+)/issues/([1-9][0-9]*)/?");
    private final RestClient client;
    public GithubIssueService() {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        client = RestClient.builder().baseUrl("https://api.github.com").requestFactory(factory)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("User-Agent", "tech-support-ticket-service").build();
    }
    GithubIssueService(RestClient client) { this.client = client; }
    public record Issue(String title, String state) {}
    public Issue getIssue(String url) {
        var match = ISSUE_URL.matcher(url);
        if (!match.matches() || match.group(2).equals(".") || match.group(2).equals("..")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expected a public GitHub issue URL");
        }
        try {
            Issue issue = client.get().uri("/repos/{owner}/{repo}/issues/{number}",
                    match.group(1), match.group(2), match.group(3)).retrieve().body(Issue.class);
            if (issue == null || issue.title() == null) throw new RestClientException("Empty issue response");
            return issue;
        } catch (RestClientException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GitHub issue is unavailable");
        }
    }
}
