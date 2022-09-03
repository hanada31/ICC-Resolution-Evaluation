package eu.vranckaert.worktime.dao.web.model.exception.user;

import eu.vranckaert.worktime.dao.web.model.base.exception.WorkTimeJSONException;

/**
 * User: Dirk Vranckaert
 * Date: 13/12/12
 * Time: 15:22
 */
public class RegisterEmailAlreadyInUseJSONException extends WorkTimeJSONException {
    private String email;

    public RegisterEmailAlreadyInUseJSONException(String requestUrl, String email) {
        super(requestUrl);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
