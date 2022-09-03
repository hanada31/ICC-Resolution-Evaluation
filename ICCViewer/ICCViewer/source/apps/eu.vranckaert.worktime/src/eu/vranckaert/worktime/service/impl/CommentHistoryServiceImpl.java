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
package eu.vranckaert.worktime.service.impl;

import android.content.Context;
import com.google.inject.Inject;
import eu.vranckaert.worktime.dao.CommentHistoryDao;
import eu.vranckaert.worktime.dao.impl.CommentHistoryDaoImpl;
import eu.vranckaert.worktime.model.CommentHistory;
import eu.vranckaert.worktime.service.CommentHistoryService;

import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 26/04/11
 * Time: 18:34
 */
public class CommentHistoryServiceImpl implements CommentHistoryService {
    @Inject
    private CommentHistoryDao dao;

    public CommentHistoryServiceImpl(Context ctx) {
        getDaos(ctx);
    }

    /**
     * Default constructor required by RoboGuice!
     */
    public CommentHistoryServiceImpl() {}

    @Override
    public void updateLastComment(String comment) {
        dao.deleteAll();
        dao.save(comment);
    }

    @Override
    public String findLastComment() {
        List<CommentHistory> commentHistories = dao.findAll();
        if (commentHistories.size() > 0) {
            return commentHistories.get(0).getComment();
        }
        return null;
    }

    /**
     * Create all the required service instances.
     * @param ctx The widget's context.
     */
    private void getDaos(Context ctx) {
        this.dao = new CommentHistoryDaoImpl(ctx);
    }
}
