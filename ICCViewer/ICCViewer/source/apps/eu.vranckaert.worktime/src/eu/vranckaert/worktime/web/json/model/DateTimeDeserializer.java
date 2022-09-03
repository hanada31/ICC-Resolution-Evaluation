package eu.vranckaert.worktime.web.json.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * User: Dirk Vranckaert
 * Date: 3/01/13
 * Time: 10:46
 */
public class DateTimeDeserializer implements JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Long time = json.getAsJsonPrimitive().getAsLong();
        Date date = new Date(time);
        return date;
    }
}
