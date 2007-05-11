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

package org.openvpms.web.component.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;


/**
 * Property file reader.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class PropertiesReader extends ConfigReader {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PropertiesReader.class);


    /**
     * Parse a property file entry.
     *
     * @param key   the property key
     * @param value the property value
     * @param path  the path the property came from
     */
    protected abstract void parse(String key, String value, String path);

    /**
     * Reads the configuration at the specified URL.
     *
     * @param url the URL to read
     */
    protected void read(URL url) {
        try {
            Properties properties = new Properties();
            properties.load(url.openStream());
            Enumeration keys = properties.propertyNames();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = properties.getProperty(key).trim();
                // trim required as Properties doesn't seem to remove
                // trailing whitespace
                parse(key, value, url.toString());
            }
        } catch (IOException exception) {
            log.error(exception, exception);
        }
    }

}
