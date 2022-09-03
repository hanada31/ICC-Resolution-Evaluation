package eu.vranckaert.worktime.dao;

import eu.vranckaert.worktime.dao.generic.GenericDao;
import eu.vranckaert.worktime.model.User;

/**
 * User: Dirk Vranckaert
 * Date: 13/12/12
 * Time: 11:34
 */
public interface AccountDao extends GenericDao<User, String> {
    /**
     * Store a newly logged in user in the database. Make sure no other user is available in the database.
     * @param user The user to be stored.
     */
    void storeLoggedInUser(User user);

    /**
     * Get the logged in user. If no user is logged in null will be returned.
     * @return The logged in user or null if no user is logged in at the moment.
     */
    User getLoggedInUser();
}
