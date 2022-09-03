/*
 * Copyright 2012 Dirk Vranckaert
 *
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

package eu.vranckaert.worktime.model.notification;

import android.content.Intent;

/**
 * Created by DIRK VRANCKAERT.
 * Date: 27/08/12
 * Time: 13:55
 */
public class NotificationAction {
    public NotificationAction(String text, Intent intent) {
        this.text = text;
        this.intent = intent;
    }

    public NotificationAction(String text, Intent intent, int intentRequestCode) {
        this.text = text;
        this.intent = intent;
        this.intentRequestCode = intentRequestCode;
    }

    public NotificationAction(int drawable, String text, Intent intent) {
        this.drawable = drawable;
        this.text = text;
        this.intent = intent;
        this.intentRequestCode = intentRequestCode;
    }

    public NotificationAction(int drawable, String text, Intent intent, int intentRequestCode) {
        this.drawable = drawable;
        this.text = text;
        this.intent = intent;
        this.intentRequestCode = intentRequestCode;
    }

    private int drawable;
    private String text;
    private Intent intent;
    private int intentRequestCode;

    public int getDrawable() {
        return drawable;
    }

    public void setDrawable(int drawable) {
        this.drawable = drawable;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public int getIntentRequestCode() {
        return intentRequestCode;
    }

    public void setIntentRequestCode(int intentRequestCode) {
        this.intentRequestCode = intentRequestCode;
    }
}
