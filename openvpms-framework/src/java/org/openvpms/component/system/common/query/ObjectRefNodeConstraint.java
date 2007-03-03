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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */


package org.openvpms.component.system.common.query;

import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * This is applicable to node descriptors that point to an object reference.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ObjectRefNodeConstraint extends AbstractNodeConstraint {

    /**
     * Default SUID.
     */
    private static final long serialVersionUID = 1L;


    /**
     * Construct a constraint on the specified node and the passed in
     * object reference.
     *
     * @param nodeName  the name of the node descriptor
     * @param reference the object reference
     */
    public ObjectRefNodeConstraint(String nodeName,
                                   IMObjectReference reference) {
        this(nodeName, RelationalOp.EQ, reference);
    }

    /**
     * Construct a constraint on the specified node, operator and the passed in
     * object reference.
     *
     * @param nodeName  the name of the node descriptor
     * @param operator  the operator
     * @param reference the object reference
     */
    public ObjectRefNodeConstraint(String nodeName, RelationalOp operator,
                                   IMObjectReference reference) {
        super(nodeName, operator, new Object[]{reference});
    }

    /**
     * Returns the object reference.
     *
     * @return the object reference
     */
    public IMObjectReference getObjectReference() {
        return (IMObjectReference) getParameters()[0];
    }

}
