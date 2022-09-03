package eu.vranckaert.worktime.dao.web.model.exception.user;

import eu.vranckaert.worktime.dao.web.model.base.exception.WorkTimeJSONException;

public class InvalidEmailJSONException extends WorkTimeJSONException {
	private String email;

	public InvalidEmailJSONException(String requestUrl, String email) {
		super(requestUrl);
		this.email = email;
	}

}
