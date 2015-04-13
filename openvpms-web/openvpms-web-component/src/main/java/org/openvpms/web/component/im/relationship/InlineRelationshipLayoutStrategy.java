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

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObjectRelationship;


/**
 * Layout strategy for {@link IMObjectRelationship}s.
 * This displays the "non-current" object in a relationship.
 * "Non-current" refers the object that is NOT currently being
 * viewed/edited. If the source and target object don't refer to the
 * current object being viewed/edited, then the target object of the
 * relationship is used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class InlineRelationshipLayoutStrategy
    extends RelationshipLayoutStrategy {

    /**
     * Creates a new <tt>InlineRelationshipLayoutStrategy</tt>.
     */
    public InlineRelationshipLayoutStrategy() {
        super(true);
    }

}
