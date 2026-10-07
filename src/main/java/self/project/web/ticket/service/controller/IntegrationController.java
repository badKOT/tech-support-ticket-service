package self.project.web.ticket.service.controller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import self.project.web.ticket.service.service.GithubIssueService;
@RestController @RequestMapping("/api/integrations/github") @RequiredArgsConstructor @Slf4j
public class IntegrationController {
    private final GithubIssueService githubIssueService;
    @GetMapping("/issue")
    public GithubIssueService.Issue getIssue(@RequestParam String url) {
        log.info("[GET /api/integrations/github/issue] Got request");
        return githubIssueService.getIssue(url);
    }
}
