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
package eu.vranckaert.worktime.exceptions;

/**
 * A runtime exception if the project data found in the local database is corrupt.
 *
 * User: DIRK VRANCKAERT
 * Date: 02/03/11
 * Time: 00:08
 */
public class CorruptProjectDataException extends RuntimeException {
    public CorruptProjectDataException(String s) {
        super(s);
    }
}
