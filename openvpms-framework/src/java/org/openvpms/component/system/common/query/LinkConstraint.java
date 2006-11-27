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

package org.openvpms.component.system.common.query;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Defines a constraint between objects on their {@link IMObject#getLinkId()}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-04-05 06:11:15Z $
 */
public class LinkConstraint implements IConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    private final String sourceLink;

    private final String targetLink;


    /**
     * Construct a constraint on the specified nodes.
     *
     * @param sourceLink the source link name. May be a type alias or fully
     *                   qualified node name
     * @param targetLink the target link name. May be a type alias or fully
     *                   qualified node name
     */
    public LinkConstraint(String sourceLink, String targetLink) {
        this.sourceLink = sourceLink;
        this.targetLink = targetLink;
    }

    /**
     * Returns the source link name. This may be a type alias or fully qualified
     * node name.
     *
     * @return the source link name
     */
    public String getSourceLink() {
        return sourceLink;
    }

    /**
     * Returns the target link name. This may be a type alias or fully qualified
     * node name.
     *
     * @return the source link name
     */
    public String getTargetLink() {
        return targetLink;
    }

    public RelationalOp getOperator() {
        return RelationalOp.EQ;
    }

}
