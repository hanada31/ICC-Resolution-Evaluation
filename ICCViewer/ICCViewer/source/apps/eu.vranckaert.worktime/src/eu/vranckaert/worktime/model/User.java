/*
 * Copyright 2013 Dirk Vranckaert
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.vranckaert.worktime.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import eu.vranckaert.worktime.dao.web.model.entities.Role;
import eu.vranckaert.worktime.utils.string.StringUtils;

import java.util.Date;

/**
 * User: Dirk Vranckaert
 * Date: 13/12/12
 * Time: 11:30
 */
@DatabaseTable(tableName = "User")
public class User {
    @DatabaseField(id = true, generatedId = false, columnName = "email", dataType = DataType.STRING)
    private String email;
    @DatabaseField
    private String password;
    @DatabaseField
    private String sessionKey;

    @DatabaseField
    private String firstName;
    @DatabaseField
    private String lastName;
    @DatabaseField(dataType = DataType.DATE_STRING)
    private Date loggedInSince;
    @DatabaseField(dataType = DataType.DATE_STRING)
    private Date registeredSince;
    @DatabaseField
    private Role role;

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

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getLoggedInSince() {
        return loggedInSince;
    }

    public void setLoggedInSince(Date loggedInSince) {
        this.loggedInSince = loggedInSince;
    }

    public Date getRegisteredSince() {
        return registeredSince;
    }

    public void setRegisteredSince(Date registeredSince) {
        this.registeredSince = registeredSince;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isProfileComplete() {
        return StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(lastName)
                && loggedInSince != null && registeredSince != null && role != null;
    }
}
