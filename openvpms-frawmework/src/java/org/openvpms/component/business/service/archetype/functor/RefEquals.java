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

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;


/**
 * A <tt>Predicate</tt> that evaluates <tt>true</tt> if an
 * {@link IMObjectReference IMObjectReference} equals the
 * specified value, otherwise evaluates <tt>false</tt>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see RelationshipRef
 */
public class RefEquals implements Predicate {

    /**
     * The reference to compare.
     */
    private final IMObjectReference ref;

    /**
     * The reference accessor.
     */
    private final Transformer transformer;


    /**
     * Creates a new <tt>RefEquals</tt>.
     *
     * @param entity      the entity to compare the reference of.
     *                    May be <tt>null</tt>
     * @param transformer the transform to obtain references to compare
     */
    public RefEquals(Entity entity, Transformer transformer) {
        this((entity != null) ? entity.getObjectReference() : null,
             transformer);
    }

    /**
     * Creates a new <tt>RefEquals</tt>.
     *
     * @param reference   the reference to compare. May be <tt>null</tt>
     * @param transformer the transform to obtain references to compare with
     */
    public RefEquals(IMObjectReference reference, Transformer transformer) {
        this.ref = reference;
        this.transformer = transformer;
    }

    /**
     * Determines if the reference equals that obtained using the transformer
     * on the passed object.
     *
     * @param object the object to evaluate
     * @return <tt>true</tt> if the references are equal,
     *         otherwise <tt>false</tt>
     * @throws ClassCastException       if the input is the wrong class
     * @throws IllegalArgumentException if the input is invalid
     */
    public boolean evaluate(Object object) {
        return ObjectUtils.equals(ref, transformer.transform(object));
    }

    /**
     * Helper to return a predicate that determines if the source of an
     * {@link IMObjectRelationship} is that of the supplied entity.
     *
     * @param entity the entity. May be <tt>null</tt>
     * @return a new predicate
     */
    public static Predicate getSourceEquals(Entity entity) {
        return new RefEquals(entity, RelationshipRef.SOURCE);
    }

    /**
     * Helper to return a predicate that determines if the source of an
     * {@link IMObjectRelationship} is that of the supplied reference.
     *
     * @param reference the reference. May be <tt>null</tt>
     * @return a new predicate
     */
    public static Predicate getSourceEquals(IMObjectReference reference) {
        return new RefEquals(reference, RelationshipRef.SOURCE);
    }

    /**
     * Helper to return a predicate that determines if the target of an
     * {@link IMObjectRelationship} is that of the supplied entity.
     *
     * @param entity the entity. May be <tt>null</tt>
     * @return a new predicate
     */
    public static Predicate getTargetEquals(Entity entity) {
        return new RefEquals(entity, RelationshipRef.TARGET);
    }

    /**
     * Helper to return a predicate that determines if the target of an
     * {@link IMObjectRelationship} is that of the supplied reference.
     *
     * @param reference the reference. May be <tt>null</tt>
     * @return a new predicate
     */
    public static Predicate getTargetEquals(IMObjectReference reference) {
        return new RefEquals(reference, RelationshipRef.TARGET);
    }
}
