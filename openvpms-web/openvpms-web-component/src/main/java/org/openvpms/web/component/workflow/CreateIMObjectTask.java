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
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectCreatorListener;


/**
 * Task to create an {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CreateIMObjectTask extends AbstractTask {

    /**
     * The short names to select from.
     */
    private final String[] shortNames;

    /**
     * Properties to populate the created object with. May be <code>null</code>
     */
    private final TaskProperties properties;


    /**
     * Constructs a new <code>CreateIMObjectTask</code>.
     *
     * @param shortName the short name of the object to create. May contain
     *                  wildcards
     */
    public CreateIMObjectTask(String shortName) {
        this(new String[]{shortName});
    }

    /**
     * Constructs a new <code>CreateIMObjectTask</code>.
     *
     * @param shortName  the short name of the object to create. May contain
     *                   wildcards
     * @param properties properties to populate the created object.
     *                   May be <code>null</code>
     */
    public CreateIMObjectTask(String shortName,
                              TaskProperties properties) {
        this(new String[]{shortName}, properties);
    }

    /**
     * Constructs a new <code>CreateIMObjectTask</code>.
     *
     * @param shortNames the short names to select from. Short names may contain
     *                   wildcards
     */
    public CreateIMObjectTask(String[] shortNames) {
        this(shortNames, null);
    }

    /**
     * Constructs a new <code>CreateIMObjectTask</code>.
     *
     * @param shortNames the short names to select from. Short names may contain
     *                   wildcards
     * @param properties properties to populate the created object.
     *                   May be <code>null</code>
     */
    public CreateIMObjectTask(String[] shortNames, TaskProperties properties) {
        this.shortNames = shortNames;
        this.properties = properties;
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
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated(object, context);
            }

            public void cancelled() {
                notifyCancelled();
            }
        };

        IMObjectCreator.create(getType(shortNames), shortNames, listener, context.getHelpContext());
    }

    /**
     * Returns the short names to select from.
     *
     * @return the short names to select from
     */
    public String[] getShortNames() {
        return shortNames;
    }

    /**
     * Invoked when an object is created.
     * Populates the object with any properties and adds it to the context.
     *
     * @param object  the object
     * @param context the context
     * @throws OpenVPMSException for any error
     */
    protected void created(IMObject object, TaskContext context) {
        if (properties != null) {
            populate(object, properties, context);
        }
        context.addObject(object);
    }

    /**
     * Invoked when an object is created. Populates the object, adds it to
     * the context and notifies the listener.
     *
     * @param object  the new object
     * @param context the task context
     */
    private void onCreated(IMObject object, TaskContext context) {
        try {
            created(object, context);
            notifyCompleted();
        } catch (OpenVPMSException exception) {
            notifyCancelledOnError(exception);
        }
    }

}
