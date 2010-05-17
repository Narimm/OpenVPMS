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
 * Defines a constraint between objects on their {@link IMObject#getId}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-04-05 06:11:15Z $
 */
public class IdConstraint implements IConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The source name. May be a type alias or fully qualified node name.
     */
    private final String sourceName;

    /**
     * The target name. May be a type alias or fully qualified node name.
     */
    private final String targetName;


    /**
     * Construct a constraint on the specified nodes.
     *
     * @param source the source name. May be a type alias or fully qualified
     *               node name
     * @param target the target name. May be a type alias or fully qualified
     *               node name
     */
    public IdConstraint(String source, String target) {
        this.sourceName = source;
        this.targetName = target;
    }

    /**
     * Returns the source name. This may be a type alias or fully qualified
     * node name.
     *
     * @return the source name
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Returns the target name. This may be a type alias or fully qualified
     * node name.
     *
     * @return the source link name
     */
    public String getTargetName() {
        return targetName;
    }

    public RelationalOp getOperator() {
        return RelationalOp.EQ;
    }

}
