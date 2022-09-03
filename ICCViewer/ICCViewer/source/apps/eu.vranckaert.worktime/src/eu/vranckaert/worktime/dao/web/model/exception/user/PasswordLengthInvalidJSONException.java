package eu.vranckaert.worktime.dao.web.model.exception.user;

import eu.vranckaert.worktime.dao.web.model.base.exception.WorkTimeJSONException;

/**
 * User: Dirk Vranckaert
 * Date: 19/12/12
 * Time: 14:41
 */
public class PasswordLengthInvalidJSONException extends WorkTimeJSONException {
    public PasswordLengthInvalidJSONException(String requestUrl) {
        super(requestUrl);
    }
}
