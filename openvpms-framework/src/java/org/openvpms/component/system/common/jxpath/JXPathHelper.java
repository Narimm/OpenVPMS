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

import org.apache.commons.jxpath.ClassFunctions;
import org.apache.commons.jxpath.FunctionLibrary;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.util.TypeUtils;

import java.util.Map;


/**
 * This helper class is used to instantiate new {@link JXPathContext} objects
 * with the relevant extension functions.
 * <p/>
 * The instance must be initialized correctly, with the extension functions
 * registered before use.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class JXPathHelper {

    /**
     * A list of extension functions. These are cached in a class attribute
     */
    private static FunctionLibrary functions = new FunctionLibrary();

    static {
        System.setProperty(JXPathContextFactory.FACTORY_NAME_PROPERTY,
                           OpenVPMSContextFactoryReferenceImpl.class.getName());

        // set the extended type converter
        TypeUtils.setTypeConverter(new OpenVPMSTypeConverter());

        // now just add the default functions
        functions.addFunctions(JXPathContext.newContext(new Object()).getFunctions());
    }

    /**
     * Default constructor.
     */
    public JXPathHelper() {
    }

    /**
     * Instantiate an instance of this helper using the specified properties.
     * Each property has a key, which is the namespace and the value, which is
     * the function class or function object.
     *
     * @param properties the class function luibraries to include
     */
    public JXPathHelper(Map properties) {
        // add the extension functions
        if (properties != null) {
            for (Object ns : properties.keySet()) {
                String namespace = (String) ns;
                Object value = properties.get(namespace);
                if (value instanceof String) {
                    addFunctions((String) value, namespace);
                } else {
                    addFunctions(value, namespace);
                }

            }
        }
    }

    /**
     * Create a new context for the specified object.
     *
     * @param object the context bean
     * @return JXPathContext the context object
     */
    public static JXPathContext newContext(Object object) {
        JXPathContext context = JXPathContext.newContext(object);
        FunctionLibrary lib = new FunctionLibrary();
        lib.addFunctions(context.getFunctions());
        lib.addFunctions(functions);
        context.setFunctions(lib);
        context.setLenient(true);

        return context;
    }

    /**
     * Adds functions for the specified class name and namespace.
     *
     * @param className the class name
     * @param namespace the namespace
     */
    private void addFunctions(String className, String namespace) {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class clazz = loader.loadClass(className);
            functions.addFunctions(new ClassFunctions(clazz, namespace));
        } catch (Exception exception) {
            throw new JXPathHelperException(
                    JXPathHelperException.ErrorCode.InvalidClassSpecified,
                    new Object[]{className}, exception);
        }
    }

    /**
     * Adds functions for the specified function object and namespace.
     *
     * @param functionObject the function object
     * @param namespace      the namespace
     */
    private void addFunctions(Object functionObject, String namespace) {
        functions.addFunctions(new ObjectFunctions(functionObject, namespace));
    }
}
