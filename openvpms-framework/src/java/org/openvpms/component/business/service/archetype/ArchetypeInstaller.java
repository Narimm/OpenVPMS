package org.openvpms.component.business.service.archetype;

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
     * Installs archetypes at the specified paths.
     *
     * @param paths the paths
     */
    void install(String... paths);
}
