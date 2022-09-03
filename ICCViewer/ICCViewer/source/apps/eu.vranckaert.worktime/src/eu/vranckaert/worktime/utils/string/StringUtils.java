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
package eu.vranckaert.worktime.utils.string;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 19/02/11
 * Time: 18:32
 */
public class StringUtils {
    /**
     * Converts a list of {@link String} objects to an array.
     * @param strings The list to convert.
     * @return The concerted array containing the original {@link String} objects in the same order.
     */
    public static String[] convertListToArray(final List<String> strings) {
        String[] stringArray = new String[strings.size()];
        for(int i = 0; i<strings.size(); i++) {
            stringArray[i] = strings.get(i);
        }
        return stringArray;
    }

    /**
     * Checks if a provided {@link String} instance is null or has a zero-length.
     * @param string The string to check.
     * @return {@link Boolean#TRUE} if the provided string is null or has length zero.
     */
    public static boolean isBlank(final String string) {
        if(string == null || string.trim().length() == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a provided {@link String} instance is NOT null or has length greater than zero.
     * @param string The string to check.
     * @return {@link Boolean#TRUE} if the provided string is NOT null or has NOT length zero.
     */
    public static boolean isNotBlank(final String string) {
        return !isBlank(string);
    }

    /**
     * Optimizes a string to be saved in the database. The string gets trimmed at begin and end.
     * @param string The string to handle.
     * @return The optimized string.
     */
    public static String optimizeString(final String string) {
        return string.trim();
    }

    /**
     * Add a certain text to the left of a specified text to get to the defined maximum text length.<br/>
     * <b><u>Examples:</u></b><br/>
     * leftPad("test", "a", 5) <i> returns </i> <b>atest</b><br/>
     * leftPad("test", "a", 7) <i> returns </i> <b>aaatest</b><br/>
     * leftPad("test", "abc", 5) <i> returns </i> <b>abctest</b><br/>
     * leftPad("test", "abc", -3) <i> returns </i> <b>test</b><br/>
     * leftPad("test", "abc", 4) <i> returns </i> <b>test</b>
     * @param text The to add the padding text to.
     * @param padText The padding text.
     * @param textLength The maximum length of the text.
     * @return The padded text.
     */
    public static String leftPad(String text, String padText, int textLength) {
        if (textLength < 0) {
            return text;
        }

        if (text.length() == textLength) {
            return text;
        }

        while (text.length() < textLength) {
            text = padText + text;
        }
        return text;
    }
}
