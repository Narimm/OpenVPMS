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

// java core
import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * This class defines a relationship between 2 {@link Lookup} instances, namely
 * a source and a target. It also uses the concept names associated with the 
 * source and target lookup instances to generate a type name in the form of
 * sourceConcept.targetConcept.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class LookupRelationship implements Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Uniquely identifies an instance
     */
    @SuppressWarnings("unused")
    private long uid;
    
    /**
     * Used for database control
     */
    @SuppressWarnings("unused")
    private long version;
    
    /**
     * The source lookup instance
     */
    private Lookup source;
    
    /**
     * The target lookup instance
     */
    private Lookup target;
    
    /**
     * The generated relationship type
     */
    private String type;
    
    /**
     * Default constructor
     */
    protected LookupRelationship() {
    }
    
    /**
     * Create a relationship between the source and target lookup instances
     * 
     * @param source
     *            the source of the lookup relationship
     * @param target
     *            the target of the lookup relationship
     * @throws LookupRelationshipException                        
     */
    public LookupRelationship(Lookup source, Lookup target) {
        if ((source == null) ||
            (target == null)) {
            throw new LookupRelationshipException(
                    LookupRelationshipException.ErrorCode.NullTargetOrSource);
        }
        
        if ((StringUtils.isEmpty(source.getArchetypeId().getConcept())) ||
            (StringUtils.isEmpty(target.getArchetypeId().getConcept()))) {
            throw new LookupRelationshipException(
                    LookupRelationshipException.ErrorCode.NullConceptNames);
        }
        
        this.source = source;
        this.target = target;
        this.type = 
            new StringBuffer(source.getArchetypeId().getConcept())
                .append(".")
                .append(target.getArchetypeId().getConcept())
                .toString();
    }

    /**
     * @return Returns the source.
     */
    public Lookup getSource() {
        return source;
    }

    /**
     * @return Returns the target.
     */
    public Lookup getTarget() {
        return target;
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @return Returns the uid.
     */
    public long getUid() {
        return uid;
    }

}
