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
import eu.vranckaert.worktime.dao.SyncRemovalCacheDao;
import eu.vranckaert.worktime.dao.generic.GenericDaoImpl;
import eu.vranckaert.worktime.model.SyncRemovalCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Dirk Vranckaert
 * Date: 11/01/13
 * Time: 16:08
 */
public class SyncRemovalCacheDaoImpl extends GenericDaoImpl<SyncRemovalCache, String> implements SyncRemovalCacheDao {
    @Inject
    public SyncRemovalCacheDaoImpl(Context context) {
        super(SyncRemovalCache.class, context);
    }

    @Override
    public Map<String, String> findAllSyncKeys() {
        List<SyncRemovalCache> removals = findAll();

        Map<String, String> removalMap = new HashMap<String, String>();
        for (SyncRemovalCache removal : removals) {
            removalMap.put(removal.getSyncKey(), removal.getEntityName());
        }

        return removalMap;
    }
}
