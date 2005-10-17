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

// java core
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// commons-lang
import org.apache.commons.lang.StringUtils;

// log4j
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;


/**
 * The archetype descriptor is used to describe an archetype.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ArchetypeDescriptor implements Serializable {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(ArchetypeDescriptor.class);

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
    private Map<String,NodeDescriptor> nodeDescriptors = 
        new LinkedHashMap<String, NodeDescriptor>();
    
    /**
     * Default constructor 
     */
    public ArchetypeDescriptor() {
        // TODO Evaluate this since we are encoding path names etc
        // best to place this in the archetype.
        NodeDescriptor node = new NodeDescriptor();
        node.setName(NodeDescriptor.IDENTIFIER_NODE_NAME);
        node.setType("java.lang.Long");
        node.setDisplayName("id");
        node.setPath("uid");
        nodeDescriptors.put(node.getName(), node);
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
     * Return the top level  node descriptors. The caller must be aware that 
     * a {@link NodeDescriptor can contain other node descriptors.
     * 
     *  TODO Inconsistent return type...change to List
     *  
     * @return NodeDescriptor[]
     */
    public NodeDescriptor[] getNodeDescriptors() {
        return (NodeDescriptor[])nodeDescriptors.values().toArray(
                new NodeDescriptor[nodeDescriptors.size()]);
    }
    
    /**
     * Return all the {@link NodeDescriptors} for this archetype. This
     * will basically flatten out the hierarchical node descriptor 
     * structure
     * 
     * @return List<NodeDescriptor>
     */
    public List<NodeDescriptor> getAllNodeDescriptors() {
        List<NodeDescriptor> nodes = new ArrayList<NodeDescriptor>();
        getAllNodeDescriptors(getNodeDescriptors(), nodes);
        
        return nodes;
    }

    /**
     * Return the {@link NodeDescriptor} instances as a map of name and 
     * descriptor
     * @return Returns the nodeDescriptors.
     */
    public Map<String, NodeDescriptor> getNodeDescriptorsAsMap() {
        return this.nodeDescriptors;
    }

    /**
     * @param nodeDescriptors The nodeDescriptors to set.
     */
    public void setNodeDescriptors(NodeDescriptor[] nodeDescriptors) {
        this.nodeDescriptors = new LinkedHashMap<String, NodeDescriptor>();
        for (NodeDescriptor descriptor : nodeDescriptors) {
            this.nodeDescriptors.put(descriptor.getName(), descriptor);
        }
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
        return StringUtils.isEmpty(displayName) ? getName() : displayName;
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
        return findNodeDescriptorWithName(getNodeDescriptors(), name);
    }

    /**
     * Return the node descriptors associated with the specified node names. 
     * This will do a search right down the NodeDescriptor hierarchy.
     * 
     * @return List<NodeDescriptor>
     *            a list of matching NodeDescriptors      
     */
    public List<NodeDescriptor> getNodeDescriptors(String[] names) {
        List<NodeDescriptor>  result = new ArrayList<NodeDescriptor>();
        
        // TODO his is an inefficient way of doing it. We need to determine
        // the access paths and optimise our classes for them.
        for (String nodeName : names) {
            NodeDescriptor descriptor = getNodeDescriptor(nodeName);
            if (descriptor != null) {
                result.add(descriptor);
            } else {
                logger.warn("Could not find a node with name " + nodeName);
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
    
    /**
     * Search the node descriptors recursively searching for the 
     * specified name
     * 
     * @param nodes
     *            the list of NodeDescriptors to search
     * @param name
     *            the name to search for
     * @return NodeDescriptor
     *            the node descriptor or null                              
     */
    private NodeDescriptor findNodeDescriptorWithName(NodeDescriptor[] nodes, 
            String name) {
        for (NodeDescriptor node : nodes ) {
            if (node.getName().equals(name)) {
                return node;
            }
            
            if (node.getNodeDescriptors().length > 0) {
                NodeDescriptor result = findNodeDescriptorWithName(
                        node.getNodeDescriptors(), name);
                if (result != null) {
                    return result;
                }
            }
        }
        
        return null;
    }
    
    /**
     * This is a recursive function that returns all the nodes in this archetype
     * descriptor.
     * 
     * @param descriptors 
     *            the node descriptors to process
     * @param nodes
     *            the resultant node array
     */
    private void getAllNodeDescriptors(NodeDescriptor[] descriptors, 
            List<NodeDescriptor> nodes) {
        for (NodeDescriptor descriptor : descriptors) {
            nodes.add(descriptor);
            if (descriptor.getNodeDescriptors().length > 0) {
                getAllNodeDescriptors(descriptor.getNodeDescriptors(), nodes);
            }
        }
    }
}

