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

// openehr-java-kernel
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.support.identification.ArchetypeID;

/**
 * This class maintains a mapping between an archetype name and the information
 * model verison number and class name. 
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
     * The record name
     */
    private String name;
    
    /**
     * The archetype identity
     */
    private String archetypeId;
    
    /**
     * The information model version number
     */
    private String infoModelVersion;
    
    /**
     * The information model class name
     */
    private String infoModelClass;
   
    
    /**
     * Construct a record using a name and the associated id, version and 
     * class names.
     * 
     * @param name
     *            the name of the record.
     * @param id
     *            the archetype id
     * @param clazz
     *            the info model class name
     * @param version
     *            the inform model version
     */
    public ArchetypeRecord(String name, String id, String clazz, String version) {
        this.name = name;
        this.archetypeId = id;
        this.infoModelClass = clazz;
        this.infoModelVersion = version;
    }

    /**
     * @return Returns the archetypeId.
     */
    public String getArchetypeId() {
        return archetypeId;
    }

    /**
     * @return Returns the infoModelClass.
     */
    public String getInfoModelClass() {
        return infoModelClass;
    }

    /**
     * @return Returns the infoModelVersion.
     */
    public String getInfoModelVersion() {
        return infoModelVersion;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Construct and return and instance of {@link Archetyped}
     * 
     * @return Archetyped
     */
    public Archetyped getArchetypeInfo() {
        return new Archetyped(new ArchetypeID(archetypeId), null, 
                infoModelVersion);
    }
}
