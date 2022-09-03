package eu.vranckaert.worktime.dao.web.model.base.exception;

/**
 * This is the base class for all exception that can be returned when making a
 * JSON call.
 * @author dirkvranckaert
 */
public abstract class WorkTimeJSONException {
	private String requestUrl;
    private String message;
	
	public WorkTimeJSONException(String requestUrl, String message) {
		this.requestUrl = requestUrl;
        this.message = message;
	}
	
	public WorkTimeJSONException(String requestUrl) {
		super();
		this.requestUrl = requestUrl;
	}

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
