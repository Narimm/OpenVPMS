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


package org.openvpms.component.business.domain.im.support;

// java core
import java.util.ArrayList;
import java.util.List;

//openehr java kernel
import org.openehr.rm.datastructure.itemstructure.ItemList;
import org.openehr.rm.datastructure.itemstructure.representation.Cluster;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datastructure.itemstructure.representation.Item;
import org.openehr.rm.datatypes.text.DvText;

/**
 * This class is provides support methods for the {@link ItemStructure}
 * class.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ItemStructureUtil {
    /**
     * Return an empty {@link ItemList}. For the moment, due to the preconditions
     * in ItemList we need to create a dummy element. 
     * 
     * TODO Have a look at modifying te openehr code base
     * 
     * @param archetypeNodeId
     *            the archetype node
     * @param name 
     *            the name of the node            
     * @return ItemList
     */
    public static ItemList createItemList(String archetypeNodeId, DvText name) {
        List<Item> items = new ArrayList<Item>();
        Element elem = new Element("dummy1", new DvText("dummy"), new DvText("dummy"));
        items.add(elem);
        Cluster cluster = new Cluster("cluster", new DvText("cluster"), items);
        
        return new ItemList(archetypeNodeId, name, cluster);
    }
    
    /**
     * Create an {@link ItemList} instance using the specified nodeId, name
     * and items
     * 
     * @param archetypeNodeId
     *            the node identity
     * @param name 
     *            the name of the node
     * @param items
     *            the items that are part of the list                                           
     */
    public static ItemList createItemList(String archetypeNodeId, DvText name,
            List<Item> items) {
        Cluster cluster = new Cluster("cluster", new DvText("cluster"), items);
        return new ItemList(archetypeNodeId, name, cluster);
    }
    
}
