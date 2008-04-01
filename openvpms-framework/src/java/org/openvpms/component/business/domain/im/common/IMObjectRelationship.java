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

package org.openvpms.component.business.domain.im.common;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValue;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValueMap;

import java.util.HashMap;
import java.util.Map;


/**
 * Describes a relationship between two {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class IMObjectRelationship extends IMObject {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The source object reference in the relationship.
     */
    private IMObjectReference source;

    /**
     * The target object reference in the relationship.
     */
    private IMObjectReference target;

    /**
     * Dynamic details of the relationship between the objects.
     */
    private Map<String, TypedValue> details
            = new HashMap<String, TypedValue>();


    /**
     * Default constructor.
     */
    protected IMObjectRelationship() {
        // do nothing
    }

    /**
     * Creates a new <tt>IMObjectRelationship</tt>.
     *
     * @param archetypeId the archetype identifier
     */
    public IMObjectRelationship(ArchetypeId archetypeId) {
        super(archetypeId);
    }

    /**
     * Returns a reference to the source object.
     *
     * @return the source object reference
     */
    public IMObjectReference getSource() {
        return source;
    }

    /**
     * Sets the source object reference.
     *
     * @param source the source object reference
     */
    public void setSource(IMObjectReference source) {
        this.source = source;
    }

    /**
     * Returns a reference to the target object.
     *
     * @return the target object reference
     */
    public IMObjectReference getTarget() {
        return target;
    }

    /**
     * Sets the target object reference.
     *
     * @param target the target object reference
     */
    public void setTarget(IMObjectReference target) {
        this.target = target;
    }

    /**
     * Returns the details.
     *
     * @return the details
     */
    public Map<String, Object> getDetails() {
        return new TypedValueMap(details);
    }

    /**
     * Sets the details.
     *
     * @param details the details
     */
    public void setDetails(Map<String, Object> details) {
        this.details = TypedValueMap.create(details);
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        IMObjectRelationship copy = (IMObjectRelationship) super.clone();
        copy.details = (details == null) ? null
                : new HashMap<String, TypedValue>(details);
        copy.source = (IMObjectReference) this.source.clone();
        copy.target = (IMObjectReference) this.target.clone();
        return copy;
    }

    /**
     * (non-Javadoc)
     *
     * @see Object#toString()
     */
    @Override
    @SuppressWarnings("HardCodedStringLiteral")
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(null)
                .append("source", source)
                .append("target", target)
                .toString();
    }
}
