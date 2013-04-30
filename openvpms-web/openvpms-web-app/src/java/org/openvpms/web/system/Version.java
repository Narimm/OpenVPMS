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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.system;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.Properties;


/**
 * Provides application version information, read from the manifest and
 * <em>META-INF/org.openvpms.revision.properties</em> resource.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Version {

    /**
     * The application version.
     */
    public static final String VERSION;

    /**
     * The version control revision.
     */
    public static final String REVISION;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(Version.class);


    static {
        String version = "";
        String revision = "";
        try {
            InputStream in = Version.class.getResourceAsStream(
                "/META-INF/org.openvpms.version.properties");
            if (in != null) {
                try {
                    Properties properties = new Properties();
                    properties.load(in);
                    version = properties.getProperty("version");
                    revision = properties.getProperty("revision");
                } finally {
                    in.close();
                }
            }
        } catch (Exception exception) {
            log.warn("Failed to load version information", exception);
        }
        VERSION = version;
        REVISION = revision;
    }
}
