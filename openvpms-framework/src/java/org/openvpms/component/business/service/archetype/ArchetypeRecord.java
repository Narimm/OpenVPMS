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


package org.openvpms.component.business.service.archetype;

// java-core
import java.io.Serializable;

//openevpms-framework
import org.openvpms.component.business.domain.archetype.Archetype;
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * This class maintains a mapping between a short name and an archetype 
 * identity
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeRecord implements Serializable {

    /**
     * SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The short name for this record
     */
    private String shortName;
    
    /**
     * The archetype identity
     */
    private ArchetypeId archetypeId;
    
    /**
     * The archetype details 
     */
    private Archetype archetype;
    
    /**
     * The information model class name
     */
    private String imClass;
   
    
    /**
     * Construct a record using a short name, ans archetype id and the 
     * associated information model class
     * 
     * @param name
     *            the name of the record.
     * @param id
     *            the archetype id
     * @param imClass
     *            the info model class name
     * @param archetype
     *            the archetype details            
     */
    public ArchetypeRecord(String shortName, ArchetypeId id, String imClass,
            Archetype archetype) {
        this.shortName = shortName;
        this.archetypeId = id;
        this.imClass = imClass;
        this.archetype = archetype;
    }

    /**
     * @return Returns the archetypeId.
     */
    protected ArchetypeId getArchetypeId() {
        return archetypeId;
    }

    /**
     * @param archetypeId The archetypeId to set.
     */
    protected void setArchetypeId(ArchetypeId archetypeId) {
        this.archetypeId = archetypeId;
    }

    /**
     * @return Returns the imClass.
     */
    protected String getInfoModelClass() {
        return imClass;
    }

    /**
     * @param imClass The imClass to set.
     */
    protected void setInfoModelClass(String imClass) {
        this.imClass = imClass;
    }

    /**
     * @return Returns the shortName.
     */
    protected String getShortName() {
        return shortName;
    }

    /**
     * @param shortName The shortName to set.
     */
    protected void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * @return Returns the archetype.
     */
    protected Archetype getArchetype() {
        return archetype;
    }

    /**
     * @param archetype The archetype to set.
     */
    protected void setArchetype(Archetype archetype) {
        this.archetype = archetype;
    }
}
