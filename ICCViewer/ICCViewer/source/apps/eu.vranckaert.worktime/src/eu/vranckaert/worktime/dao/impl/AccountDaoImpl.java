package eu.vranckaert.worktime.dao.impl;

import android.content.Context;
import com.google.inject.Inject;
import eu.vranckaert.worktime.dao.AccountDao;
import eu.vranckaert.worktime.dao.generic.GenericDaoImpl;
import eu.vranckaert.worktime.model.User;

import java.util.List;

/**
 * User: Dirk Vranckaert
 * Date: 13/12/12
 * Time: 11:36
 */
public class AccountDaoImpl extends GenericDaoImpl<User, String> implements AccountDao {
    private static final String LOG_TAG = AccountDaoImpl.class.getSimpleName();

    @Inject
    public AccountDaoImpl(final Context context) {
        super(User.class, context);
    }

    @Override
    public void storeLoggedInUser(User user) {
        super.deleteAll();
        super.save(user);
    }

    @Override
    public User getLoggedInUser() {
        List<User> users = super.findAll();
        if (users.size() == 1) {
            return users.get(0);
        }
        return null;
    }
}
