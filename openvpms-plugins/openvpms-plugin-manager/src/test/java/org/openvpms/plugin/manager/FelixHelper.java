/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.plugin.manager;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Felix helper methods.
 *
 * @author Tim Anderson
 */
public class FelixHelper {

    /**
     * Returns the path to the Felix install.
     *
     * @return the path
     */
    public static String getFelixDir() {
        String relPath = FelixHelper.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        File dir = new File(relPath + "../../target/felix");
        assertTrue("Felix installation at " + dir.getPath() + " not found ", dir.exists());
        return dir.getPath();
    }
}
