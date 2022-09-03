package eu.vranckaert.worktime.dao.web.model.exception;

import eu.vranckaert.worktime.dao.web.model.base.exception.WorkTimeJSONException;

/**
 * User: Dirk Vranckaert
 * Date: 13/12/12
 * Time: 15:20
 */
public class FieldRequiredJSONException extends WorkTimeJSONException {
    private String fieldName;

    public FieldRequiredJSONException(String requestUrl, String fieldName) {
        super(requestUrl);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
