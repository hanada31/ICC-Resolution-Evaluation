package eu.vranckaert.worktime.comparators.reporting;

import eu.vranckaert.worktime.model.TimeRegistration;

import java.util.Comparator;

/**
 * @author Dirk Vranckaert
 *         Date: 14/11/11
 *         Time: 12:00
 */
public class TimeRegistrationByStartDateDescComparator implements Comparator<TimeRegistration> {
    @Override
    public int compare(TimeRegistration tr1, TimeRegistration tr2) {
        return (tr1.getStartTime().compareTo(tr2.getStartTime()) * -1);
    }
}
