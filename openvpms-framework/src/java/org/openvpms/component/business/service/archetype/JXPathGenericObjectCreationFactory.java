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

// commons-jxpath
import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.Node;

/**
 * This class is used to by JXPath during the object construction phase. It 
 * uses reflection and the commons-beanutils library to create a node on an
 * object.
 * <p>
 * Objects created through this factory must have a default constructor.
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class JXPathGenericObjectCreationFactory extends AbstractFactory {
    /**
     * The default constructor
     */
    public JXPathGenericObjectCreationFactory() {
    }

    /* (non-Javadoc)
     * @see org.apache.commons.jxpath.AbstractFactory#createObject(org.apache.commons.jxpath.JXPathContext, org.apache.commons.jxpath.Pointer, java.lang.Object, java.lang.String, int)
     */
    @Override
    public boolean createObject(JXPathContext context, Pointer ptr, 
            Object parent, String name, int index) {
        try {
            Node node = (Node)context.getVariables().getVariable("node");
            ptr.setValue(Class.forName(node.getType()).newInstance());
        } catch (Exception exception) {
            return false;
        }
        
        return true;
    }

}
