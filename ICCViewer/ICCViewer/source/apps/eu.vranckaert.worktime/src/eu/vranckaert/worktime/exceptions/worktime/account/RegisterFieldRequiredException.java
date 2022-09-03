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

package eu.vranckaert.worktime.exceptions.worktime.account;

/**
 * User: Dirk Vranckaert
 * Date: 3/01/13
 * Time: 08:51
 */
public class RegisterFieldRequiredException extends Exception {
    private String fieldName;

    public RegisterFieldRequiredException(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
