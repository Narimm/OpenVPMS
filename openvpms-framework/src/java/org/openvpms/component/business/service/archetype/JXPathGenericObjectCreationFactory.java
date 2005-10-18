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
import org.apache.log4j.Logger;

// openvpms-framework
import org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor;

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
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(JXPathGenericObjectCreationFactory.class);
    
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
            NodeDescriptor node = (NodeDescriptor)context.getVariables().getVariable("node");
            
            if (logger.isDebugEnabled()) {
                logger.debug("node: " + node.getPath() + " for object " + 
                        context.getContextBean().getClass().getName());
            }
            
            ptr.setValue(Thread.currentThread().getContextClassLoader()
                    .loadClass(node.getType()).newInstance());
        } catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
        
        return true;
    }

}
