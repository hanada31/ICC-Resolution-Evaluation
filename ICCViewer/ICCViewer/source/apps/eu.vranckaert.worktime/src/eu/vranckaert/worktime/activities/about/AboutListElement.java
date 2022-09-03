/*
 *  Copyright 2011 Dirk Vranckaert
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package eu.vranckaert.worktime.activities.about;

import android.content.Intent;

/**
 * User: DIRK VRANCKAERT
 * Date: 23/12/11
 * Time: 14:16
 */
public class AboutListElement {
    private int title;
    private String value;
    private Intent intent;

    public AboutListElement(int title, String value) {
        this.title = title;
        this.value = value;
    }

    public AboutListElement(int title, String value, Intent intent) {
        this.title = title;
        this.value = value;
        this.intent = intent;
    }

    public int getTitle() {
        return title;
    }

    public void setTitle(int title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }
}
