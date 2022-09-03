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
package eu.vranckaert.worktime.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * User: DIRK VRANCKAERT
 * Date: 26/04/11
 * Time: 18:27
 */
@DatabaseTable
public class CommentHistory {

    /**
     * The default constructor.
     */
    @Deprecated
    public CommentHistory() {}

    /**
     * The construct for a comment. After setting the specified comment the current date will be set in the
     * {@link CommentHistory#entranceDate} field.
     * @param comment The comment to set.
     */
    public CommentHistory(String comment) {
        this.comment = comment;
        this.entranceDate = new Date();
    }

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField
    private String comment;
    @DatabaseField
    private Date entranceDate;
    @DatabaseField
    private String flags;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getEntranceDate() {
        return entranceDate;
    }

    public void setEntranceDate(Date entranceDate) {
        this.entranceDate = entranceDate;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }
}
