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


package org.openvpms.component.business.domain.im.lookup;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValue;
import org.openvpms.component.business.domain.im.datatypes.basic.TypedValueMap;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines a relationship between 2 {@link Lookup} instances, namely
 * a source and a target. It also uses the concept names associated with the 
 * source and target lookup instances to generate a type name in the form of
 * sourceConcept.targetConcept.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class LookupRelationship extends IMObject {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Reference to the source {@link Lookup} reference
     */
    private IMObjectReference source;

    /**
     * Reference to the target {@link Lookup} reference
     */
    private IMObjectReference target;

    /**
     * Details holds dynamic attributes for a lookup relationship.
     */
    private Map<String, TypedValue> details = new HashMap<String, TypedValue>();

    /**
     * Default constructor
     */
    public LookupRelationship() {
    }

    /**
     * Convenient constructor to set up a lookup relationship between a source
     * and target lookup
     * 
     * @param source
     *            the source lookup
     * @param target
     *            the target lookup
     */
    public LookupRelationship(Lookup source, Lookup target ) {
        this.source = source.getObjectReference();
        this.target = target.getObjectReference();
    }

    /**
     * @return Returns the source.
     */
    public IMObjectReference getSource() {
        return source;
    }

    /**
     * @return Returns the target.
     */
    public IMObjectReference getTarget() {
        return target;
    }

    /**
     * @param source The source to set.
     */
    public void setSource(IMObjectReference source) {
        this.source = source;
    }

    /**
     * @param target The target to set.
     */
    public void setTarget(IMObjectReference target) {
        this.target = target;
    }

    /**
     * @return Returns the details.
     */
    public Map<String, Object> getDetails() {
        return new TypedValueMap(details);
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(Map<String, Object> details) {
        this.details = TypedValueMap.create(details);
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        LookupRelationship copy = (LookupRelationship)super.clone();
        copy.source = (IMObjectReference)this.source.clone();
        copy.target = (IMObjectReference)this.target.clone();

        // details
        copy.details = (details == null) ? null : new HashMap<String, TypedValue>(details);

        return copy;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
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
