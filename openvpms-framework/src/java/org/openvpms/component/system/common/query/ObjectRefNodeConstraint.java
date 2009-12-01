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

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * A constraint on object reference nodes.
 * When constructed with an {@link IMObjectReference},
 * the {@link IMObjectReference#getId} is used to constrain the node.
 * When constructed with an {@link ArchetypeId}, the
 * {@link ArchetypeId#getShortName()} is used to constrain the node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ObjectRefNodeConstraint extends AbstractNodeConstraint {

    /**
     * Serialisation version identifier.
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
     * Construct a constraint on the specified node and the passed in
     * archetype identifier.
     *
     * @param nodeName the name of the node descriptor
     * @param id       the archetype identifier
     */
    public ObjectRefNodeConstraint(String nodeName, ArchetypeId id) {
        this(nodeName, RelationalOp.EQ, id);
    }

    /**
     * Construct a constraint on the specified node and the passed in
     * archetype identifier.
     *
     * @param nodeName the name of the node descriptor
     * @param operator the operator
     * @param id       the archetype identifier
     */
    public ObjectRefNodeConstraint(String nodeName, RelationalOp operator,
                                   ArchetypeId id) {
        super(nodeName, operator, new Object[]{id});
    }

    /**
     * Returns the object reference.
     *
     * @return the object reference, or <tt>null</tt> if this was constructed
     *         with an archetype id.
     */
    public IMObjectReference getObjectReference() {
        if (getParameters()[0] instanceof IMObjectReference) {
            return (IMObjectReference) getParameters()[0];
        }
        return null;
    }

    /**
     * Returns the archetype id.
     *
     * @return the archetype id, or <tt>null</tt> if this was constructed
     *         with an object reference.
     */
    public ArchetypeId getArchetypeId() {
        if (getParameters()[0] instanceof ArchetypeId) {
            return (ArchetypeId) getParameters()[0];
        }
        return null;
    }

}
