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

// java openehr kernel
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.support.identification.ArchetypeID;

/**
 * This is the base class for information model objects. It extends the
 * {@link Locatable} class and adds some convenience functions and also some
 * methods to support persistence..
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class InfoModelObject extends Locatable {

    /**
     * SUID.
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Indicates the version of this object
     */
    private long version;

    
    /**
     * Default constructor
     */
    public InfoModelObject() {
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
    public InfoModelObject(String uid, String archetypeId, String imVersion, 
            String archetypeNodeId, DvText name) {
        super(archetypeNodeId, name);
        this.setArchetypeDetails(new Archetyped(new ArchetypeID(archetypeId),
                null, imVersion));
        this.setUid(new IMObjectID(uid));
    }
    /*
     * (non-Javadoc)
     * 
     * @see org.openehr.rm.common.archetyped.Locatable#pathOfItem(org.openehr.rm.common.archetyped.Locatable)
     */
    @Override
    public String pathOfItem(Locatable item) {
        // TODO Auto-generated method stub
        return null;
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
     * Return the identity of this object
     * 
     * @return String
     */
    public String getId() {
        return getUid().getValue();
    }

    /**
     * Set the identity of this object
     * 
     * @param id 
     *            the object identity
     */
    public void setId(String id) {
        setUid(new IMObjectID(id));
    }
}
