package self.project.web.ticket.service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class VersionController {

    private final BuildProperties buildProperties;

    @GetMapping("/api/version")
    public String version() {
        return buildProperties.getVersion();
    }
}