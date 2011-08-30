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

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Determines if a node is a particular value.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeConditionTask<T> extends NodeEvalTask<Boolean> {

    /**
     * The expected value.
     */
    private final T expected;

    /**
     * If <code>true</code> evaluate <em>node==value<em>
     * otherwise evaluate <em>node != value</em>.
     */
    private final boolean equals;


    /**
     * Constructs a new <code>NodeConditionTask</code> to evaluate the value
     * of an object in the {@link TaskContext}.
     *
     * @param shortName the short name of the object to evaluate the node of
     * @param node      the node name
     * @param value     the expected value
     */
    public NodeConditionTask(String shortName, String node, T value) {
        this(shortName, node, true, value);
    }

    /**
     * Constructs a new <code>NodeConditionTask</code> to evaluate the value
     * of  an object in the {@link TaskContext}.
     *
     * @param shortName the short name of the object to evaluate the node of
     * @param node      the node name
     * @param equals    if <code>true</code> evaluate <em>node==value<em>
     *                  otherwise evaluate <em>node != value</em>
     * @param value     the expected value
     */
    public NodeConditionTask(String shortName, String node, boolean equals,
                             T value) {
        super(shortName, node);
        this.equals = equals;
        this.expected = value;
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
    @Override
    @SuppressWarnings("unchecked")
    public void start(TaskContext context) {
        IMObject object = getObject(context);
        if (object == null) {
            notifyCancelled();
        }
        T value = (T) getValue(object);
        boolean result;
        if (expected instanceof Comparable && value != null) {
            result = ((Comparable) expected).compareTo(value) == 0;
        } else {
            result = ObjectUtils.equals(expected, value);
        }
        if (equals) {
            setValue(result);
        } else {
            setValue(!result);
        }
    }
}
