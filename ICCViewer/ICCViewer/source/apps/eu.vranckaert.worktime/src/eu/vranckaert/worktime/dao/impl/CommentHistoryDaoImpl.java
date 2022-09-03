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
package eu.vranckaert.worktime.dao.impl;

import android.content.Context;
import com.google.inject.Inject;
import eu.vranckaert.worktime.dao.CommentHistoryDao;
import eu.vranckaert.worktime.dao.generic.GenericDaoImpl;
import eu.vranckaert.worktime.model.CommentHistory;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.string.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: DIRK VRANCKAERT
 * Date: 26/04/11
 * Time: 18:30
 */
public class CommentHistoryDaoImpl extends GenericDaoImpl<CommentHistory, Integer> implements CommentHistoryDao {
    private static final String LOG_TAG = CommentHistoryDaoImpl.class.getSimpleName();

    @Inject
    public CommentHistoryDaoImpl(final Context context) {
        super(CommentHistory.class, context);
    }

    /**
     *
     * {@inheritDoc}
     */
    public void save(final String comment) {
        Log.d(getContext(), LOG_TAG, "About to save a new comment: " + comment);
        String optimizedComment = StringUtils.optimizeString(comment);

        this.save(new CommentHistory(comment));
    }

    /**
     *
     * {@inheritDoc}
     */
    public void deleteAll() {
        Log.d(getContext(), LOG_TAG, "Ready to delete all the items in the comment history");
        try {
            List<CommentHistory> comments = findAll();
            Log.d(getContext(), LOG_TAG, "Number of comments found to delete: " + comments.size());
            List<Integer> ids = new ArrayList<Integer>();
            for (CommentHistory comment : comments) {
                ids.add(comment.getId());
            }
            if (ids.size() > 0) {
                dao.deleteIds(ids);
            }
        } catch (SQLException e) {
            Log.d(getContext(), LOG_TAG, "Could not start the query... Returning false");
            return;
        }
        Log.d(getContext(), LOG_TAG, "All comments are deleted!");
    }
}
