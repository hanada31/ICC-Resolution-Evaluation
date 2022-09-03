/*
 * Copyright 2013 Dirk Vranckaert
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.vranckaert.worktime.web.json.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;

/**
 * Date: 16/10/12
 * Time: 19:00
 *
 * @author Dirk Vranckaert
 */
public class JsonEntity {
    public String toJSON() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Date.class, new DateTimeSerializer());
        builder.excludeFieldsWithoutExposeAnnotation();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        String json = gson.toJson(this);
        return json;
    }
}
