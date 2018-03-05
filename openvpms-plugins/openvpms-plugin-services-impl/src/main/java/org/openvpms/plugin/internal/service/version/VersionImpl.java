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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.plugin.internal.service.version;

import org.openvpms.plugin.service.version.Version;

/**
 * Default implementation of {@link Version}.
 *
 * @author Tim Anderson
 */
public class VersionImpl implements Version {

    /**
     * Returns the application version.
     *
     * @return the application version
     */
    @Override
    public String getVersion() {
        return org.openvpms.version.Version.VERSION;
    }

    /**
     * Returns the version control revision.
     *
     * @return the version control revision
     */
    @Override
    public String getRevision() {
        return org.openvpms.version.Version.REVISION;
    }
}
