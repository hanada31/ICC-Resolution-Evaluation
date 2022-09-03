package eu.vranckaert.worktime.dao.web.model.exception.security;

import eu.vranckaert.worktime.dao.web.model.base.exception.WorkTimeJSONException;

/**
 * User: Dirk Vranckaert
 * Date: 13/12/12
 * Time: 15:24
 */
public class UserNotLoggedInJSONException extends WorkTimeJSONException {
    public UserNotLoggedInJSONException(String requestUrl) {
        super(requestUrl);
    }
}
