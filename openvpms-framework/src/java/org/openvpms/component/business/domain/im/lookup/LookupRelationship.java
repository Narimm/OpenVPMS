/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */


package org.openvpms.component.business.domain.im.lookup;

import org.openvpms.component.business.domain.im.common.IMObjectRelationship;


/**
 * Describes a relationship between two {@link Lookup}s.
 *
 * @author Jim Alateras
 */
public class LookupRelationship extends IMObjectRelationship
        implements org.openvpms.component.model.lookup.LookupRelationship {

    /**
     * Serialisation version identifier.
     */
    private static final long serialVersionUID = 1L;
    

    /**
     * Default constructor.
     */
    public LookupRelationship() {
    }

    /**
     * Convenient constructor to set up a lookup relationship between a source
     * and target lookup.
     *
     * @param source the source lookup
     * @param target the target lookup
     */
    public LookupRelationship(Lookup source, Lookup target) {
        setSource(source.getObjectReference());
        setTarget(target.getObjectReference());
    }
}
