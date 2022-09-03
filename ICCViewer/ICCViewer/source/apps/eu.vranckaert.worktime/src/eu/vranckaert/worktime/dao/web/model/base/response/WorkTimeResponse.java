package eu.vranckaert.worktime.dao.web.model.base.response;

import eu.vranckaert.worktime.dao.web.model.exception.security.ServiceNotAllowedJSONException;
import eu.vranckaert.worktime.dao.web.model.exception.security.UserIncorrectRoleException;
import eu.vranckaert.worktime.dao.web.model.exception.security.UserNotLoggedInJSONException;
import eu.vranckaert.worktime.web.json.model.JsonEntity;

public abstract class WorkTimeResponse extends JsonEntity {
    private ServiceNotAllowedJSONException serviceNotAllowedException;
    private UserNotLoggedInJSONException userNotLoggedInException;
    private UserIncorrectRoleException userIncorrectRoleException;
    private boolean resultOk = true;

    public ServiceNotAllowedJSONException getServiceNotAllowedException() {
        return serviceNotAllowedException;
    }

    public UserNotLoggedInJSONException getUserNotLoggedInException() {
        return userNotLoggedInException;
    }

    public UserIncorrectRoleException getUserIncorrectRoleException() {
        return userIncorrectRoleException;
    }

    public boolean isResultOk() {
        return resultOk;
    }
}

