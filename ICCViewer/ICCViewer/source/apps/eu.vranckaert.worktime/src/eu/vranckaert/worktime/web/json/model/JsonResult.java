/*
 * Copyright 2012 Dirk Vranckaert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package eu.vranckaert.worktime.web.json.model;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Date: 16/10/12
 * Time: 19:00
 *
 * @author Dirk Vranckaert
 */
public class JsonResult {
    private static final String LOG_TAG = JsonResult.class.getSimpleName();

    private String json;

    public JsonResult(String json) {
        this.json = json;
    }

    public <Y extends JsonEntity> Y getSingleResult(Class<Y> entityClass) {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateTimeDeserializer());
        Gson gson = builder.create();

        JSONObject j;
        Y object = null;

        try {
            j = new JSONObject(json);
            object = gson.fromJson(j.toString(), entityClass);
        } catch (JSONException e) {
            String msg = "Could not parse the json data!";
            Log.e(LOG_TAG, msg, e);
            throw new JsonSyntaxException(msg, e);
        }
        return object;
    }

    public <Y extends JsonEntity> List<Y> getResultList(Class<Y> entityClass) {
        //Type listType = new TypeToken<List<Y>>() {}.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();

        JSONArray j;
        List<Y> resultList = new ArrayList<Y>();

        try {
            j = new JSONArray(json);
            for (int i=0; i<j.length(); i++) {
                Y object = gson.fromJson(j.getJSONObject(i).toString(), entityClass);
                resultList.add(object);
            }
            //list = gson.fromJson(j.toString(), listType);
        } catch (JSONException e) {
            String msg = "Could not parse the json data!";
            Log.e(LOG_TAG, msg, e);
            throw new JsonSyntaxException(msg, e);
        }
        return resultList;
    }
}
