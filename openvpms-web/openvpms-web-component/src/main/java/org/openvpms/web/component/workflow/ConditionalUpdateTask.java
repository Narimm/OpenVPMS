/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workflow;


/**
 * A {@link ConditionalTask} that updates an object if it exists in the context.
 *
 * @author Tim Anderson
 */
public class ConditionalUpdateTask extends ExistsTask {

    /**
     * Constructs a {@code ConditionalUpdateTask}.
     *
     * @param shortName  the short name of the object to update
     * @param properties properties to populate the object with
     */
    public ConditionalUpdateTask(String shortName, TaskProperties properties) {
        this(shortName, new UpdateIMObjectTask(shortName, properties));
    }

    /**
     * Constructs a {@code ConditionalUpdateTask}.
     *
     * @param shortName the short name of the object to update
     */
    public ConditionalUpdateTask(String shortName, UpdateIMObjectTask task) {
        super(shortName, true, task);
    }
}
