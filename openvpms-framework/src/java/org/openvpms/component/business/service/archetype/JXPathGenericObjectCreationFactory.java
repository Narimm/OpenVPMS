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
import java.math.BigDecimal;

import org.apache.commons.jxpath.AbstractFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;

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
                logger.debug("root: " + context.getContextBean().toString() +
                        " parent: " + parent.toString() +
                        " name: " + name +
                        " index: " + index +
                        " type: " + node.getType());
            }
            
            Class clazz = Thread.currentThread().getContextClassLoader()
                .loadClass(node.getType());
            if (clazz == Boolean.class) {
                ptr.setValue(new Boolean(false));
            } else if (clazz == Integer.class) {
                ptr.setValue(new Integer(0));
            } else if (clazz == Long.class) {
                ptr.setValue(new Long(0L));
            } else if (clazz == Double.class) {
                ptr.setValue(new Double(0.0));
            } else if (clazz == Float.class) {
                ptr.setValue(new Float(0.0));
            } else if (clazz == Short.class) {
                ptr.setValue(new Short((short)0));
            } else if (clazz == Byte.class) {
                ptr.setValue(new Byte((byte)0));
            } else if (clazz == Money.class) {
                    ptr.setValue(new Money("0.0"));
            } else if (clazz == BigDecimal.class) {
                ptr.setValue(BigDecimal.valueOf(0.0));
            } else {
                ptr.setValue(clazz.newInstance());
            }
        } catch (Exception exception) {
            logger.error("root: " + context.getContextBean().toString() +
                    " parent: " + parent.toString() +
                    " name: " + name +
                    " index: " + index, exception);
            return false;
        }
        
        return true;
    }

}
