package self.project.web.ticket.service.security;

import org.springframework.stereotype.Service;
import self.project.web.ticket.service.entity.Ticket;
import self.project.web.ticket.service.entity.TicketStatus;
import self.project.web.ticket.service.entity.User;

@Service
public class TicketAccessService {

    /**
     * Все авторизованные пользователи могут просматривать любые тикеты.
     */
    public boolean canRead(Ticket ticket, User currentUser) {
        return currentUser != null;
    }

    /**
     * Изменение title / description.
     * <p>
     * ADMIN / TEAM_LEAD — любой тикет. REQUESTER — только свой тикет в статусе OPEN. SUPPORT_AGENT
     * — нельзя.
     */
    public boolean canEditContent(Ticket ticket, User currentUser) {
        return switch (currentUser.getRole()) {
            case ADMIN, TEAM_LEAD -> true;

            case REQUESTER -> isSameUser(ticket.getCreator(), currentUser)
                && ticket.getStatus() == TicketStatus.OPEN;

            case SUPPORT_AGENT -> false;
        };
    }

    /**
     * Изменение статуса.
     * <p>
     * ADMIN / TEAM_LEAD — любой переход.
     * <p>
     * SUPPORT_AGENT — только если тикет назначен на него.
     * <p>
     * REQUESTER — только собственный тикет: CLOSED / RESOLVED -> REOPENED.
     */
    public boolean canChangeStatus(Ticket ticket, User currentUser, TicketStatus targetStatus) {
        return switch (currentUser.getRole()) {
            case ADMIN, TEAM_LEAD -> true;

            case SUPPORT_AGENT -> isSameUser(ticket.getAssignee(), currentUser);

            case REQUESTER -> canRequesterReopen(ticket, currentUser, targetStatus);
        };
    }

    /**
     * Назначение исполнителя.
     * <p>
     * ADMIN / TEAM_LEAD могут назначать исполнителей и снимать назначение.
     * <p>
     * SUPPORT_AGENT может только назначить самого себя.
     * <p>
     * REQUESTER назначать исполнителей не может.
     */
    public boolean canManageAssignment(User currentUser, User newAssignee) {
        return switch (currentUser.getRole()) {
            case ADMIN, TEAM_LEAD -> true;

            case SUPPORT_AGENT -> newAssignee != null && isSameUser(currentUser, newAssignee);

            case REQUESTER -> false;
        };
    }

    /**
     * Перенос тикета между проектами.
     */
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

    private boolean canRequesterReopen(Ticket ticket, User currentUser, TicketStatus targetStatus) {
        if (!isSameUser(ticket.getCreator(), currentUser)) {
            return false;
        }

        boolean currentStatusAllowsReopen = ticket.getStatus() == TicketStatus.CLOSED
            || ticket.getStatus() == TicketStatus.RESOLVED;

        return currentStatusAllowsReopen && targetStatus == TicketStatus.REOPENED;
    }

    private boolean isSameUser(User first, User second) {
        return first != null && second != null && first.getId() != null && first.getId()
            .equals(second.getId());
    }
}