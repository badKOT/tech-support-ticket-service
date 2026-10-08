package self.project.web.ticket.service.service;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class GithubIssueServiceTest {

    @Test
    void shouldFetchIssueFromFixedApiHost() {
        var builder = RestClient.builder().baseUrl("https://api.github.com");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.github.com/repos/octocat/Hello-World/issues/123"))
            .andRespond(
                withSuccess("{\"title\":\"Issue title\",\"state\":\"open\",\"body\":\"Ignored\"}",
                    MediaType.APPLICATION_JSON));
        var result = new GithubIssueService(builder.build()).getIssue(
            "https://github.com/octocat/Hello-World/issues/123");
        assertThat(result.title()).isEqualTo("Issue title");
        assertThat(result.state()).isEqualTo("open");
        server.verify();
    }

    @Test
    void shouldMapUpstreamFailures() {
        var builder = RestClient.builder().baseUrl("https://api.github.com");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://api.github.com/repos/org/repo/issues/1"))
            .andRespond(withResourceNotFound());
        assertThatThrownBy(() -> new GithubIssueService(builder.build()).getIssue(
            "https://github.com/org/repo/issues/1"))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode().value()).isEqualTo(502));
        server.verify();
    }

    @Test
    void shouldRejectHostSpoofingAndPathTraversal() {
        var service = new GithubIssueService();
        for (String url : new String[]{"https://github.com.evil.test/org/repo/issues/1",
            "https://github.com/org/../issues/1", "http://localhost/issues/1"}) {
            assertThatThrownBy(() -> service.getIssue(url)).isInstanceOfSatisfying(
                ResponseStatusException.class,
                e -> assertThat(e.getStatusCode().value()).isEqualTo(400));
        }
    }
}
