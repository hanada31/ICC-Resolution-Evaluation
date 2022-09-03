package eu.vranckaert.worktime.dao.web.model.exception.security;

import eu.vranckaert.worktime.dao.web.model.base.exception.WorkTimeJSONException;
import eu.vranckaert.worktime.dao.web.model.entities.Role;

/**
 * User: Dirk Vranckaert
 * Date: 13/12/12
 * Time: 15:25
 */
public class UserIncorrectRoleException extends WorkTimeJSONException {
    Role requiredRole;

    public UserIncorrectRoleException(String requestUrl, Role requiredRole) {
        super(requestUrl);
        this.requiredRole = requiredRole;
    }

    public Role getRequiredRole() {
        return requiredRole;
    }

    public void setRequiredRole(Role requiredRole) {
        this.requiredRole = requiredRole;
    }
}
