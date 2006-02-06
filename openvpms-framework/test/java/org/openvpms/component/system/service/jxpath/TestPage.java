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


package org.openvpms.component.system.service.jxpath;

// openvpms-framework
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;

/**
 * This is the test page, which is used to emulate the use of JXPath
 * in Tapestry
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class TestPage {
    /**
     * The model object
     */
    private Object model;
    
    /**
     * The descriptor object
     */
    private ArchetypeDescriptor node;
    
    /**
     * Instantiate using node and mode
     */
    public TestPage(Object model, ArchetypeDescriptor node) {
        this.model = model;
        this.node = node;
    }

    /**
     * @return Returns the model.
     */
    public Object getModel() {
        return model;
    }

    /**
     * @param model The model to set.
     */
    public void setModel(Object model) {
        this.model = model;
    }

    /**
     * @return Returns the node.
     */
    public ArchetypeDescriptor getNode() {
        return node;
    }

    /**
     * @param node The node to set.
     */
    public void setNode(ArchetypeDescriptor node) {
        this.node = node;
    }

}
