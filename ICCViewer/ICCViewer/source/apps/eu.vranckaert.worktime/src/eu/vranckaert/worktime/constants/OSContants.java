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
package eu.vranckaert.worktime.constants;

import android.os.Environment;

/**
 * User: DIRK VRANCKAERT
 * Date: 14/12/2011
 * Time: 13:34
 */
public class OSContants {
    public class API {
        public static final int CUPCAKE = 3;
        public static final int DONUT = 4;
        public static final int ECLAIR = 7;
        public static final int FROYO = 8;
        public static final int GINGERBREAD_2_3_2 = 9;
        public static final int GINGERBREAD_2_3_7 = 10;
        public static final int HONEYCOMB_3_0 = 11;
        public static final int HONEYCOMB_3_1 = 12;
        public static final int HONEYCOMB_3_2 = 13;
        public static final int ICS_4_0 = 14;
        public static final int ICS_4_0_3 = 15;
    }
    
    public enum DirectoryContentType {
        DIRECTORY_ALARMS(Environment.DIRECTORY_ALARMS),
        DIRECTORY_DCIM(Environment.DIRECTORY_DCIM),
        DIRECTORY_DOWNLOADS(Environment.DIRECTORY_DOWNLOADS),
        DIRECTORY_MOVIES(Environment.DIRECTORY_MOVIES),
        DIRECTORY_MUSIC(Environment.DIRECTORY_MUSIC),
        DIRECTORY_NOTIFICATIONS(Environment.DIRECTORY_NOTIFICATIONS),
        DIRECTORY_PICTURES(Environment.DIRECTORY_PICTURES),
        DIRECTORY_PODCASTS(Environment.DIRECTORY_PODCASTS),
        DIRECTORY_RINGTONES(Environment.DIRECTORY_RINGTONES);

        private String type;

        DirectoryContentType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
