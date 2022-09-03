package eu.vranckaert.worktime.dao.web.model.base.request;

import com.google.gson.annotations.Expose;
import eu.vranckaert.worktime.constants.EnvironmentConstants;

import java.io.Serializable;

public abstract class RegisteredServiceRequest extends WorkTimeJSONRequest implements Serializable {
    @Expose
	private String serviceKey = EnvironmentConstants.WorkTimeWeb.SERVICE_KEY;

	public String getServiceKey() {
		return serviceKey;
	}
}
