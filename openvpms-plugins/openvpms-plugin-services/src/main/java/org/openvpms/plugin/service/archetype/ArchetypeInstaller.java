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

package org.openvpms.plugin.service.archetype;

import java.io.InputStream;

/**
 * Installs archetypes from streams, the classpath or filesystem.
 *
 * @author Tim Anderson
 */
public interface ArchetypeInstaller {

    /**
     * Installs archetypes from a stream.
     *
     * @param stream the stream
     */
    void install(InputStream stream);

    /**
     * Installs archetypes at the specified path.
     *
     * @param path the path
     */
    void install(String path);

    /**
     * Installs archetypes at the specified paths.
     *
     * @param paths the paths
     */
    void install(String[] paths);
}
