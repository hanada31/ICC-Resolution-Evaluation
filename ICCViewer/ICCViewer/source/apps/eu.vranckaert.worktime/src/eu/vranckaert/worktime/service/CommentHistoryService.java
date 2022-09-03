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
package eu.vranckaert.worktime.service;

import android.content.Context;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 26/04/11
 * Time: 18:32
 */
public interface CommentHistoryService {

    /**
     * Save a comment in the comment history.
     * @param comment The comment to save.
     */
    void updateLastComment(String comment);
    /**
     * Find the last comment.
     * @return
     */
    String findLastComment();
}
