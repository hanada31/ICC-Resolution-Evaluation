package eu.vranckaert.worktime.dao.web.model.request.user;

import com.google.gson.annotations.Expose;
import eu.vranckaert.worktime.dao.web.model.base.request.RegisteredServiceRequest;

/**
 * User: Dirk Vranckaert
 * Date: 3/01/13
 * Time: 08:55
 */
public class UserRegistrationRequest extends RegisteredServiceRequest {
    @Expose
    private String email;
    @Expose
    private String password;
    @Expose
    private String lastName;
    @Expose
    private String firstName;

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

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
