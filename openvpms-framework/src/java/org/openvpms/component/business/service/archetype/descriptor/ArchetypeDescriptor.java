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


package org.openvpms.component.business.service.archetype.descriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openvpms.component.business.domain.archetype.ArchetypeId;

/**
 * The archetype descriptor is used to describe an archetype.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeDescriptor implements Serializable {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The achetype id
     */
    private ArchetypeId archetypeId;
    
    /**
     * The display name of the archetype. If the displayname is empty then
     * simply return the name
     */
    private String displayName;
    
    /**
     * The full-qualified Java domain class that the archetype constrains
     */
    private String type;
    
    /**
     * Indicates that this is the latest version of the archetype descritpor.
     * Note that an archetype can be qualified by a version number.
     */
    private boolean isLatest;
    
    /**
     * A list of {@link NodeDescriptor} that belong to this archetype
     * descriptor.
     */
    private HashMap nodeDescriptors = new HashMap();
    
    /**
     * Default constructor 
     */
    public ArchetypeDescriptor() {
        // do nothing
    }

    /**
     * @return Returns the archetypeQName.
     */
    public String getArchetypeQName() {
        return archetypeId == null ? null : archetypeId.getQName();
    }

    /**
     * @param archetypeQName The archetypeQName to set.
     */
    public void setArchetypeQName(String archetypeQName) {
        this.archetypeId = new ArchetypeId(archetypeQName);
    }

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return Returns the isLatest.
     */
    public boolean isLatest() {
        return isLatest;
    }

    /**
     * @param isLatest The isLatest to set.
     */
    public void setLatest(boolean isLatest) {
        this.isLatest = isLatest;
    }

    /**
     * @return Returns the nodeDescriptors.
     */
    public HashMap getNodeDescriptors() {
        return nodeDescriptors;
    }

    /**
     * @param nodeDescriptors The nodeDescriptors to set.
     */
    public void setNodeDescriptors(HashMap nodeDescriptors) {
        this.nodeDescriptors = nodeDescriptors;
    }

    /**
     * Return the archetype short name .
     * 
     * @return String
     *            the node name
     */
    public String getName() {
        return archetypeId.getShortName();
    }

    /**
     * Return the display name. If a display name is not specified then
     * return the archtypes short name 
     * 
     * @return String
     *            the display name
     */
    public String getDisplayName() {
        return displayName == null ? getName() : displayName;
    }

    /**
     * Return the {&link ArchetypeId} associated with this descriptor.
     * 
     * @return ArchetypeId
     */
    public ArchetypeId getArchetypeId() {
        return archetypeId;
    }

    /**
     * Return the node descriptor for the specified node name
     * 
     * @param name
     *            the node name
     * @return NodeDescriptor
     */
    public NodeDescriptor getNodeDescriptor(String name) {
        // this will only return the top level name. Do we actually want to
        // flatten out the structure.
        return (NodeDescriptor)nodeDescriptors.get(name);
    }

    /**
     * Return the node descriptors associated with the specified node names
     * 
     * @return Map
     *            a map where the key is node name and the value is the 
     *            INodeDescriptor      
     */
    public List getNodeDescriptors(String[] names) {
        List<NodeDescriptor>  result = new ArrayList<NodeDescriptor>();
        
        // this will only do a top level search.
        for (String nodeName : names) {
            if (nodeDescriptors.containsKey(nodeName)) {
                result.add((NodeDescriptor)nodeDescriptors.get(nodeName));
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return archetypeId.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        
        // is it of the correct type
        if (!(obj instanceof ArchetypeDescriptor)) {
            return false;
        }
        
        // are they the same object
        if (this == obj) {
            return true;
        }
        
        ArchetypeDescriptor desc = (ArchetypeDescriptor)obj;
        return archetypeId.equals(desc.archetypeId);
    }
}

