package eu.vranckaert.worktime.enums.reporting;

/**
 * @author Dirk Vranckaert
 *         Date: 14/11/11
 *         Time: 11:21
 */
public enum ReportingDataOrder {
    ASC(0),
    DESC(1);

    private int order;

    ReportingDataOrder(int order) {
        setOrder(order);
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
