package self.project.web.ticket.service.security;

import org.springframework.stereotype.Service;
import self.project.web.ticket.service.entity.Ticket;
import self.project.web.ticket.service.entity.TicketStatus;
import self.project.web.ticket.service.entity.User;

@Service
public class TicketAccessService {

    public boolean canRead(Ticket ticket, User currentUser) {
        return switch (currentUser.getRole()) {
            case ADMIN, TEAM_LEAD -> true;

            case REQUESTER -> isSameUser(ticket.getCreator(), currentUser);

            case SUPPORT_AGENT -> isSameUser(ticket.getAssignee(), currentUser);
        };
    }

    public boolean canEditContent(Ticket ticket, User currentUser) {
        return switch (currentUser.getRole()) {
            case ADMIN, TEAM_LEAD -> true;

            case REQUESTER -> isSameUser(ticket.getCreator(), currentUser)
                && ticket.getStatus() == TicketStatus.OPEN;

            case SUPPORT_AGENT -> false;
        };
    }

    public boolean canChangeStatus(Ticket ticket, User currentUser) {
        return switch (currentUser.getRole()) {
            case ADMIN, TEAM_LEAD -> true;

            case SUPPORT_AGENT -> isSameUser(ticket.getAssignee(), currentUser);

            case REQUESTER -> false;
        };
    }

    public boolean canManageAssignment(User currentUser) {
        return switch (currentUser.getRole()) {
            case ADMIN, TEAM_LEAD -> true;
            case REQUESTER, SUPPORT_AGENT -> false;
        };
    }

    public boolean canMoveTicket(User currentUser) {
        return switch (currentUser.getRole()) {
            case ADMIN, TEAM_LEAD -> true;
            case REQUESTER, SUPPORT_AGENT -> false;
        };
    }

    public boolean canViewAnalytics(User currentUser) {
        return switch (currentUser.getRole()) {
            case ADMIN, TEAM_LEAD -> true;
            case REQUESTER, SUPPORT_AGENT -> false;
        };
    }

    private boolean isSameUser(User first, User second) {
        return first != null
            && second != null
            && first.getId() != null
            && first.getId().equals(second.getId());
    }
}
