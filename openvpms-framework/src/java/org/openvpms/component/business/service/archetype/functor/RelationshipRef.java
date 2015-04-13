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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.archetype.functor;

import org.apache.commons.collections.Transformer;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;


/**
 * A transformer that returns the
 * {@link IMObjectRelationship#getSource() source} or
 * {@link IMObjectRelationship#getTarget() target} reference of an
 * {@link IMObjectRelationship IMObjectRelationship}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipRef implements Transformer {

    /**
     * A transformer that returns the source reference.
     */
    public static RelationshipRef SOURCE = new RelationshipRef(true);

    /**
     * A transformer that returns the target reference.
     */
    public static RelationshipRef TARGET = new RelationshipRef(false);

    /**
     * Determines if the transformer will return the source or target reference.
     */
    private final boolean source;


    /**
     * Creates a new <tt>RelationshipRef</tt>.
     *
     * @param source if <tt>true</tt>, return the source, otherwise return the
     *               target.
     */
    private RelationshipRef(boolean source) {
        this.source = source;
    }

    /**
     * Returns the source or target of a relationship.
     *
     * @param relationship the relationship
     * @return the source, if <tt>true</tt> was specified at construction,
     *         otherwise the target
     */
    public IMObjectReference transform(IMObjectRelationship relationship) {
        return (source) ? relationship.getSource() : relationship.getTarget();
    }

    /**
     * Returns the source or target of a relationship.
     *
     * @param input the object to be transformed. Must be an
     *              {@link IMObjectRelationship IMObjectRelationship}.
     * @return the source, if <tt>true</tt> was specified at construction,
     *         otherwise the target
     * @throws ClassCastException if the input is the wrong class
     */
    public Object transform(Object input) {
        return transform((IMObjectRelationship) input);
    }
}
