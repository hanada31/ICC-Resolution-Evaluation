package eu.vranckaert.worktime.dao.web.model.exception.user;

import eu.vranckaert.worktime.dao.web.model.base.exception.WorkTimeJSONException;

/**
 * User: Dirk Vranckaert
 * Date: 13/12/12
 * Time: 15:23
 */
public class EmailOrPasswordIncorrectJSONException extends WorkTimeJSONException {
    public EmailOrPasswordIncorrectJSONException(String requestUrl) {
        super(requestUrl);
    }
}
