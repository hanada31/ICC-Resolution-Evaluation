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

package eu.vranckaert.worktime.guice;

import android.content.Context;
import com.google.inject.AbstractModule;
import eu.vranckaert.worktime.dao.*;
import eu.vranckaert.worktime.dao.impl.*;
import eu.vranckaert.worktime.dao.web.WorkTimeWebDao;
import eu.vranckaert.worktime.dao.web.impl.WorkTimeWebDaoImpl;
import eu.vranckaert.worktime.service.*;
import eu.vranckaert.worktime.service.impl.*;
import eu.vranckaert.worktime.service.ui.StatusBarNotificationService;
import eu.vranckaert.worktime.service.ui.WidgetService;
import eu.vranckaert.worktime.service.ui.impl.StatusBarNotificationServiceImpl;
import eu.vranckaert.worktime.service.ui.impl.WidgetServiceImpl;

public class Module extends AbstractModule {
    private static final String LOG_TAG = Module.class.getSimpleName();

    public Module(Context ctx) {
    }

    @Override
    protected void configure() {
        bindDaos();
        bindWebDaos();
        bindServices();
    }

    private void bindDaos() {
        bind(TimeRegistrationDao.class).to(TimeRegistrationDaoImpl.class).asEagerSingleton();
        bind(ProjectDao.class).to(ProjectDaoImpl.class).asEagerSingleton();
        bind(TaskDao.class).to(TaskDaoImpl.class).asEagerSingleton();
        bind(CommentHistoryDao.class).to(CommentHistoryDaoImpl.class).asEagerSingleton();
        bind(WidgetConfigurationDao.class).to(WidgetConfigurationDaoImpl.class).asEagerSingleton();
        bind(AccountDao.class).to(AccountDaoImpl.class).asEagerSingleton();
        bind(SyncHistoryDao.class).to(SyncHistoryDaoImpl.class).asEagerSingleton();
        bind(SyncRemovalCacheDao.class).to(SyncRemovalCacheDaoImpl.class).asEagerSingleton();
    }

    private void bindWebDaos() {
        bind(WorkTimeWebDao.class).to(WorkTimeWebDaoImpl.class).asEagerSingleton();
    }

    private void bindServices() {
        bind(ProjectService.class).to(ProjectServiceImpl.class).asEagerSingleton();
        bind(TimeRegistrationService.class).to(TimeRegistrationServiceImpl.class).asEagerSingleton();
        bind(TaskService.class).to(TaskServiceImpl.class).asEagerSingleton();
        bind(CommentHistoryService.class).to(CommentHistoryServiceImpl.class).asEagerSingleton();
        bind(BackupService.class).to(DatabaseFileBackupServiceImpl.class).asEagerSingleton();
        bind(ExportService.class).to(ExportServiceImpl.class).asEagerSingleton();
        bind(AccountService.class).to(AccountServiceImpl.class).asEagerSingleton();
        //UI services
        bind(WidgetService.class).to(WidgetServiceImpl.class).asEagerSingleton();
        bind(StatusBarNotificationService.class).to(StatusBarNotificationServiceImpl.class).asEagerSingleton();
    }
}
