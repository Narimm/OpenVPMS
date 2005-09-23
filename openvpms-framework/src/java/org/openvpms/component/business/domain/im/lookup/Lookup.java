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
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * A lookup represents a piece of static data that is used by 
 * OpenVPMS. It can be used to represent a Species, a Breed, a Country,
 * a PostCode etc.
 * <p> 
 * A lookup can have additional information stored in the details 
 * attribute 
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Lookup extends IMObject {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The value of the lookup. This is mandatory.
     */
    private String value;
    
    /**
     * The code associsted with the lookup. This is an optional.
     */
    private String code;
    
    /**
     * Details holds dynamic attributes for a lookup
     */
    private DynamicAttributeMap details;
    
    
    /**
     * Default constructor
     */
    public Lookup() {
    }

    /**
     * Construct a lookup
     * 
     * @param uid
     *            uniquely identifies this object
     * @param archetypeId
     *            the archetype id constraining this object
     * @param value
     *            the value associated with the lookup
     * @param code 
     *            an optional code 
     */
    public Lookup(String uid, ArchetypeId archetypeId, String value, 
        String code) {
        super(uid, archetypeId);
        
        this.details = new DynamicAttributeMap();
        this.value = value;
        this.code = code;
    }

    /**
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
        this.details = details;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(super.toString())
            .append("value", value)
            .append("code", code)
            .toString();
    }
}
