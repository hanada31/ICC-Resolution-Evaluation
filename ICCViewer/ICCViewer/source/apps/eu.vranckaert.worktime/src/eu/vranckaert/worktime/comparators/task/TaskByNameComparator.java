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
package eu.vranckaert.worktime.comparators.task;

import eu.vranckaert.worktime.model.Task;

import java.util.Comparator;

/**
 * User: DIRK VRANCKAERT
 * Date: 30/03/11
 * Time: 19:11
 */
public class TaskByNameComparator implements Comparator<Task> {
    public int compare(Task task1, Task task2) {
        return task1.getName().compareTo(task2.getName());
    }
}
