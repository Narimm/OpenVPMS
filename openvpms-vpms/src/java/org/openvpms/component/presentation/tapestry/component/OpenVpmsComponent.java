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

package org.openvpms.component.presentation.tapestry.component;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.tapestry.BaseComponent;
import org.apache.tapestry.components.Block;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */

public abstract class OpenVpmsComponent extends BaseComponent {
    public static String DEFAULT = "Default";
    
    /**
     * This method evaluates a JXPath expression against and object and 
     * returns the resulting object
     * 
     * @param root
     *            the root object
     * @param expr  
     *            the JXPath expression
     * @return Object
     *            the resolved object
     * @throws Exception
     *            propagate exception                                
     */
    public Object getValue(Object root, String expr)
    throws Exception {
        JXPathContext context = JXPathContext.newContext(root);
        return context.getValue(expr);
    }
    
    /**
     * @param propertyName
     * @return
     */
    public boolean hasBlock(String propertyName) {
        if (getPage().getComponents().containsKey(propertyName))
            return true;
        else
            return false;
    }

    /**
     * @param propertyName
     * @return
     */
    public Block getBlock(String propertyName) {
        if (getPage().getComponents().containsKey(propertyName))
            return (Block) getPage().getComponent(propertyName);
        else
            return null;
    }


}
