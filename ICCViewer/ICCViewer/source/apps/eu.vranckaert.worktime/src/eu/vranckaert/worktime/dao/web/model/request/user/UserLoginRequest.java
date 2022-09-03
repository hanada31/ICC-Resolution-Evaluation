package eu.vranckaert.worktime.dao.web.model.request.user;

import com.google.gson.annotations.Expose;
import eu.vranckaert.worktime.dao.web.model.base.request.RegisteredServiceRequest;

/**
 * User: Dirk Vranckaert
 * Date: 13/12/12
 * Time: 15:30
 */
public class UserLoginRequest extends RegisteredServiceRequest {
    @Expose
    private String email;
    @Expose
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
