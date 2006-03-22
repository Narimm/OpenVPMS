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

package org.openvpms.component.system.common.jxpath;

import java.util.Properties;

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.util.TypeUtils;
import org.apache.log4j.Logger;

/**
 * This helper class is used to instantiate new {@link JXPathContext} objects
 * with the relevant extension functions.
 * <p>
 * The instance must be initialized correctly, with the extension functions
 * registered before use.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class JXPathHelper {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(JXPathHelper.class);
    
    /**
     * A list of extension functions. These are cached in a class attribute
     */
    private static FunctionLibrary functions = new FunctionLibrary();

    /**
     * Default constructor
     */
    public JXPathHelper() {
        // set the factory name so that the correct version context
        // factory is invoked
        System.setProperty(JXPathContextFactory.FACTORY_NAME_PROPERTY,
                OpenVPMSContextFactoryReferenceImpl.class.getName());
        
        // set the extended type converter
        TypeUtils.setTypeConverter(new OpenVPMSTypeConverter());
    }

    /**
     * Instantiate an instance of this helper using the specified properties.
     * Each property has a key, which is the namespace and the value, which is
     * the function class.
     * 
     * @param properties
     *            the class function luibraries to include
     */
    public JXPathHelper(Properties props) {
        // add the extension functions
        if (props != null) {
            for (Object ns : props.keySet()) {
                String namespace = (String) ns;
    
                try {
                    Class clazz = Thread.currentThread().getContextClassLoader()
                            .loadClass(props.getProperty(namespace));
                    functions.addFunctions(new ClassFunctions(clazz, namespace));
                } catch (Exception exception) {
                    throw new JXPathHelperException(
                            JXPathHelperException.ErrorCode.InvalidClassSpecified,
                            new Object[] { props.getProperty(namespace) },
                            exception);
                }
            }
        }
        
        // set the factory name so that the correct version context
        // factory is invoked
        System.setProperty(JXPathContextFactory.FACTORY_NAME_PROPERTY,
                OpenVPMSContextFactoryReferenceImpl.class.getName());
        
        // set the extended type converter
        TypeUtils.setTypeConverter(new OpenVPMSTypeConverter());
        
    }
    
    /**
     * Create a new context for the specified object
     * 
     * @param object
     *            the context bean
     * @return JXPathContext
     *            the context object            
     */
    public static JXPathContext newContext(Object object) {
        JXPathContext context  = JXPathContext.newContext(object);
        functions.addFunctions(context.getFunctions());
        context.setFunctions(functions);
        
        return context;
    }

}
