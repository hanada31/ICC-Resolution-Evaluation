package eu.vranckaert.worktime.comparators.reporting;

import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.Comparator;

/**
 * @author Dirk Vranckaert
 *         Date: 14/11/11
 *         Time: 12:00
 */
public class TimeRegistrationByProjectNameDescComparator implements Comparator<TimeRegistration> {
    @Override
    public int compare(TimeRegistration tr1, TimeRegistration tr2) {
        return (tr1.getTask().getProject().getName().compareTo(tr2.getTask().getProject().getName()) * -1);
    }
}
