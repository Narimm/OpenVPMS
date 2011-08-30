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

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.CollectionNodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;


/**
 * Collection node constraint for participants.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ParticipantConstraint extends CollectionNodeConstraint {

    /**
     * Constructs a new <code>ParticipantConstraint</code>.
     * The constraint alias will be set to the node name.
     *
     * @param nodeName  the the participant node name
     * @param shortName the participation short name
     * @param entity    the participant
     */
    public ParticipantConstraint(String nodeName, String shortName,
                                 Entity entity) {
        this(nodeName, shortName, entity.getObjectReference());
    }

    /**
     * Constructs a new <code>ParticipantConstraint</code>.
     * The constraint alias will be set to the node name.
     *
     * @param nodeName  the the participant node name
     * @param shortName the participation short name
     * @param entity    the participant
     */
    public ParticipantConstraint(String nodeName, String shortName,
                                 IMObjectReference entity) {
        super(nodeName, shortName, true, true);
        add(new ObjectRefNodeConstraint("entity", entity));
        getArchetypeConstraint().setAlias(nodeName);
    }
}
