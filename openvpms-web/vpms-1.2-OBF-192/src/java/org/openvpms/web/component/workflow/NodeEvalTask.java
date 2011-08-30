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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.workflow;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * Task to evaluate the value of a node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeEvalTask<T> extends EvalTask<T> {

    /**
     * The short name of the object to evaluate the node for.
     */
    private String shortName;

    /**
     * The node name.
     */
    private String node;


    /**
     * Constructs a new <code>NodeEvalTask</code> to evaluate the value
     * of  an object in the {@link TaskContext}.
     *
     * @param shortName the short name of the object to evaluate the node of
     * @param node      the node name
     */
    public NodeEvalTask(String shortName, String node) {
        this.shortName = shortName;
        this.node = node;
    }


    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or
     * failure.
     *
     * @param context the task context
     * @throws OpenVPMSException for any error
     */
    @SuppressWarnings("unchecked")
    public void start(TaskContext context) {
        IMObject object = getObject(context);
        if (object == null) {
            notifyCancelled();
        }
        setValue((T) getValue(object));
    }

    /**
     * Returns the object.
     *
     * @param context the task context
     * @return the object,or <code>null</code> if none is found
     */
    protected IMObject getObject(TaskContext context) {
        return context.getObject(shortName);
    }

    /**
     * Returns the node value.
     *
     * @param object the object
     * @return the node value
     */
    protected Object getValue(IMObject object) {
        return IMObjectHelper.getValue(object, node);
    }
}
