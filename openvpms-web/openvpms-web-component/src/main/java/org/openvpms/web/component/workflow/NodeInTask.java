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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.workflow;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;


/**
 * Determines if a node is in/not in a range of values.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class NodeInTask<T> extends NodeEvalTask<Boolean> {

    /**
     * The range of values.
     */
    private final T[] values;

    /**
     * If {@code true} indicates that the node value must not be in {@link #values}.
     */
    private final boolean not;

    /**
     * Constructs a {@code NodeInTask}.
     *
     * @param shortName the short name of the object to evaluate the node of
     * @param node      the node name
     * @param values    the values to check against
     */
    public NodeInTask(String shortName, String node, T... values) {
        this(shortName, node, false, values);
    }

    /**
     * Constructs a {@code NodeInTask}.
     *
     * @param shortName the short name of the object to evaluate the node of
     * @param node      the node name
     * @param not       if {@code true}, indicates that the node value must no be in {@code values}
     * @param values    the values to check against
     */
    public NodeInTask(String shortName, String node, boolean not, T... values) {
        super(shortName, node);
        this.values = values;
        this.not = not;
    }

    /**
     * Starts the task.
     * <p/>
     * The registered {@link TaskListener} will be notified on completion or failure.
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
        } else {
            T value = (T) getValue(object);
            boolean result = false;
            for (T compare : values) {
                if (compare instanceof Comparable && value != null) {
                    result = ((Comparable) compare).compareTo(value) == 0;
                } else {
                    result = ObjectUtils.equals(compare, value);
                }
                if (result) {
                    break;
                }
            }
            if (not) {
                setValue(!result);
            } else {
                setValue(result);
            }
        }
    }
}
