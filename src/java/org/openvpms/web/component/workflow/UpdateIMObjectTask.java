/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.workflow;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;

import java.util.Map;


/**
 * Task to update an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class UpdateIMObjectTask extends AbstractTask {

    /**
     * The object short name.
     */
    private String shortName;

    /**
     * The object to update.
     */
    private IMObject object;

    /**
     * Properties to populate the object with.
     */
    private final Map<String, Object> properties;

    /**
     * Determines if the object should be saved.
     */
    private final boolean save;


    /**
     * Creates a new <code>UpdateIMObjectTask</code>.
     * The object is saved on update.
     *
     * @param shortName  the short name of the object to update
     * @param properties properties to populate the object with
     */
    public UpdateIMObjectTask(String shortName,
                              Map<String, Object> properties) {
        this(shortName, properties, true);
    }

    /**
     * Creates a new <code>UpdateIMObjectTask</code>.
     *
     * @param shortName  the short name of the object to update
     * @param properties properties to populate the object with
     * @param save       determines if the object should be saved
     */
    public UpdateIMObjectTask(String shortName,
                              Map<String, Object> properties,
                              boolean save) {
        this.shortName = shortName;
        this.properties = properties;
        this.save = save;
    }

    /**
     * Creates a new <code>UpdateIMObjectTask</code>.
     * The object is saved on update.
     *
     * @param object     the object to update
     * @param properties properties to populate the object with
     */
    public UpdateIMObjectTask(IMObject object,
                              Map<String, Object> properties) {
        this(object, properties, true);
    }

    /**
     * Creates a new <code>UpdateIMObjectTask</code>.
     *
     * @param object     the object to update
     * @param properties properties to populate the object with
     * @param save       determines if the object should be saved
     */
    public UpdateIMObjectTask(IMObject object,
                              Map<String, Object> properties, boolean save) {
        this.object = object;
        this.properties = properties;
        this.save = save;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     */
    public void start(final TaskContext context) {
        if (object == null) {
            object = context.getObject(shortName);
        }
        if (object != null) {
            populate(object, properties, context);
            if (save) {
                ArchetypeServiceHelper.getArchetypeService().save(object);
            }
            notifyCompleted();
        } else {
            notifyCancelled();
        }
    }
}
