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

package org.openvpms.component.business.domain.im;

// java core
import java.io.Serializable;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.support.IMObjectReference;

/**
 * This is the base class for information model objects. An {@link IMObject} 
 * object is very generic and is constrained at runtime by applying constriants
 * on the object. These constraints are the foundation of archetypes and 
 * archetype languages such as ADL
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class IMObject implements Serializable {

    /**
     * SUID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Indicates the version of this object
     */
    private long version;
    
    /**
     * Uniquely identifies an instance of this class 
     */
    private String uid;
    
    /**
     * The name of this object
     */
    private String  name;
    
    /**
     * The archetype that is attached to this object
     */
    private ArchetypeId archetypeId;
    
    /**
     * Default constructor
     */
    public IMObject() {
        // do nothing
    }

    /**
     * Construct an instance of an info model object given the specified 
     * data.
     * 
     * @param uid
     *            the object's unique identity.
     * @param archetypeId
     *            the id of the archetype to associated with this object.
     * @param imVersion
     *            the version of the information model.           
     * @param archetypeNodeId
     *            the node identity that this archetype.
     * @param name
     *            the name of the object (or is it the type).
     */
    public IMObject(String uid, ArchetypeId archetypeId, String name) {
        this.uid = uid;
        this.archetypeId = archetypeId;
        this.name = name;
    }
    /**
     * @return Returns the version.
     */
    public long getVersion() {
        return version;
    }

    /**
     * @param version The version to set.
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * @return Returns the id.
     */
    public String getUid() {
        return this.uid;
    }

    /**
     * @param id The id to set.
     */
    public void setUid(String id) {
        this.uid = id;
    }
    
    /**
     * @return Returns the archetypeId.
     */
    public ArchetypeId getArchetypeId() {
        return archetypeId;
    }

    /**
     * @param archetypeId The archetypeId to set.
     */
    public void setArchetypeId(ArchetypeId archetypeId) {
        this.archetypeId = archetypeId;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return an {@link IMObjectReference} to this object
     * 
     * @return IMObjectReference
     */
    public IMObjectReference getObjectReference() {
        return new IMObjectReference(this.getClass().getName(), getUid());
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        
        boolean result = false;
        if (obj instanceof IMObject) {
            IMObject imobj = (IMObject)obj;
            if (imobj.getUid().equals(this.getUid())) {
                result = true;
            }
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.getUid().hashCode();
    }
}

