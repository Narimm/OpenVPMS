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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.component.util.PropertiesParser;


/**
 * Property file parser for archetype handlers.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see ArchetypeHandlers
 * @see ShortNamePairArchetypeHandlers
 */
public abstract class ArchetypePropertiesParser extends PropertiesParser {

    /**
     * The type all handler classes must implement.
     */
    private Class type;

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(ArchetypePropertiesParser.class);


    /**
     * Constructs a new <code>ArchetypePropertiesParser</code>.
     *
     * @param type the type all handler classes must implement
     */
    public ArchetypePropertiesParser(Class type) {
        this.type = type;
    }

    /**
     * Returns the handler class type.
     *
     * @return the handler class type
     */
    protected Class getType() {
        return type;
    }

    /**
     * Loads a class.
     *
     * @param name the class name
     * @return the class, or <code>null</code> if it can't be found
     */
    protected Class getClass(String name) {
        for (ClassLoader loader : getClassLoaders()) {
            if (loader != null) {
                try {
                    Class clazz = loader.loadClass(name);
                    if (type.isAssignableFrom(clazz)) {
                        return clazz;
                    } else {
                        log.error("Failed to load class: " + name
                                + ", specified in " + getPath()
                                + ": does not extend" + type.getName());
                        return null;

                    }
                } catch (ClassNotFoundException ignore) {
                    // no-op
                }
            }
        }
        log.error("Failed to load class: " + name + ", specified in "
                + getPath());
        return null;
    }
}
