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


package org.openvpms.component.business.domain.im.datatype;


// openehr java kernel
import java.util.List;
import java.util.ArrayList;

import org.openehr.rm.datastructure.itemstructure.ItemList;
import org.openehr.rm.datastructure.itemstructure.ItemStructure;
import org.openehr.rm.datastructure.itemstructure.representation.Element;
import org.openehr.rm.datastructure.itemstructure.representation.Item;
import org.openehr.rm.datatypes.text.DvText;

// openvpms-framework
import org.openvpms.component.business.domain.im.support.ItemStructureUtil;
import org.openvpms.component.system.common.test.BaseTestCase;

import com.thoughtworks.xstream.XStream;

/**
 * Test the abilty to use ItemStructure with XStream
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class ItemStructureTestCase extends BaseTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ItemStructureTestCase.class);
    }

    /**
     * Constructor for ItemStructureTestCase.
     * 
     * @param name
     */
    public ItemStructureTestCase(String name) {
        super(name);
    }

    /*
     * @see BaseTestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see BaseTestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test the streaming of a simple {@link ItemStructure} 
     */
    public void testSimpleItemListStreaming()
    throws Exception {
        ItemList il = ItemStructureUtil.createItemList("at002", new DvText("details"));
        assert(il.itemCount() == 1);
        
        // test the streaming
        XStream xs = new XStream();
        String xmls = xs.toXML(il);
        assertTrue(xmls.length() > 0);
        this.debug(xmls);
        
        // test back to object
        ItemList newil = (ItemList)xs.fromXML(xmls);
        assert(newil.itemCount() == 1);
    }

    
    /**
     * Test the streaming of a largwr ItemList {@link ItemStructure} 
     */
    public void testLargeItemListStreaming()
    throws Exception {
        List<Item> items = new ArrayList<Item>();
        for (int index = 0; index < 100; index++) {
            items.add(new Element("element" + index, new DvText("name"+ index),
                    new DvText("name" + index)));
        }
        ItemList il = ItemStructureUtil.createItemList("at002", new DvText("details"),
                items);
        assertTrue(il.itemCount() == 100);
        
        // test the streaming
        XStream xs = new XStream();
        String xmls = xs.toXML(il);
        assertTrue(xmls.length() > 0);
        this.debug(xmls);
        
        // test back to object
        ItemList newil = (ItemList)xs.fromXML(xmls);
        assert(newil.itemCount() == 100);
    }

    
    /* (non-Javadoc)
     * @see org.openvpms.component.system.common.test.BaseTestCase#setUpTestData()
     */
    @Override
    protected void setUpTestData() throws Exception {
        // no test data
    }
}
