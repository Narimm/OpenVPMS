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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.ActBean;


/**
 * Task to add an act relationship.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AddActRelationshipTask extends AbstractTask {

    /**
     * The parent act short name.
     */
    private final String parentShortName;

    /**
     * The child act short name.
     */
    private final String childShortName;

    /**
     * The relationship short name
     */
    private final String relationshipShortName;

    /**
     * Constructs a new <code>AddActRelationshipTask</code>.
     *
     * @param parentShortName       the parent act short name
     * @param childShortName        the child act short name
     * @param relationshipShortName the relationship short name
     */
    public AddActRelationshipTask(String parentShortName, String childShortName,
                                  String relationshipShortName) {
        this.parentShortName = parentShortName;
        this.childShortName = childShortName;
        this.relationshipShortName = relationshipShortName;
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
        Act parent = (Act) context.getObject(parentShortName);
        Act child = (Act) context.getObject(childShortName);
        if (parent != null && child != null) {
            ActBean bean = new ActBean(parent);
            bean.addRelationship(relationshipShortName, child);
            bean.save();
            notifyCompleted();
        } else {
            notifyCancelled();
        }
    }

}
