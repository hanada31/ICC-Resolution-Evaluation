package eu.vranckaert.worktime.dao.web.model.exception.security;

import eu.vranckaert.worktime.dao.web.model.base.exception.WorkTimeJSONException;

/**
 * User: Dirk Vranckaert
 * Date: 13/12/12
 * Time: 15:26
 */
public class ServiceNotAllowedJSONException extends WorkTimeJSONException {
    private String serviceKey;

    public ServiceNotAllowedJSONException(String requestUrl, String serviceKey) {
        super(requestUrl);
        this.serviceKey = serviceKey;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }
}
