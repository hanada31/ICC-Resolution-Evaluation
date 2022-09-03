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

package eu.vranckaert.worktime.utils.file;

import eu.vranckaert.worktime.service.ExportService;

import java.io.File;
import java.io.FilenameFilter;

public class XlsFilenameFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        String lcName = name.toLowerCase();
        if (lcName.endsWith(ExportService.XLS_EXTENSTION)) {
            return true;
        }

        return false;
    }
}
