package self.project.web.ticket.service.dto;

import java.util.List;
import java.util.Map;

public record ProjectAnalytics(
    Map<String, Long> statusCounts,
    List<DailyCount> createdVsResolved,
    List<AssigneeResolutionTime> avgResolutionTimes
) {}