package eu.vranckaert.worktime.web.json.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * User: Dirk Vranckaert
 * Date: 3/01/13
 * Time: 10:44
 */
public class DateTimeSerializer implements JsonSerializer<Date> {
    @Override
    public JsonElement serialize(Date source, Type typeOfSrc, JsonSerializationContext context) {
        Long time = source.getTime();
        return new JsonPrimitive(time.toString());
    }
}
