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

package org.openvpms.plugin.internal.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.util.StringMap;
import org.apache.felix.framework.util.manifestparser.ManifestParser;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Manifest;

/**
 * Parses OSGi manifest entries to determine the packages that need to be added to the
 * <em>org.osgi.framework.system.packages.extra</em> property to expose them to plugins.
 *
 * @author Tim Anderson
 */
class ExportPackages {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ExportPackages.class);

    /**
     * Helper to supply to {@code ManifestParser}.
     */
    private static final BundleRevision BUNDLE_REVISION = new MockBundleRevision();

    /**
     * Parses OSGi manifest entries to determine the packages that need to be added to the
     * <em>org.osgi.framework.system.packages.extra</em> property to expose them to plugins.
     *
     * @param logger the Felix logger
     * @return the extra system packages
     */
    public String getPackages(Logger logger) {
        Map<String, String> packages = new TreeMap<>();
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                Map<String, Object> manifest = getManifest(resources.nextElement());
                if (manifest != null) {
                    addPackages(packages, manifest, logger);
                }
            }
        } catch (IOException ignore) {
            log.debug(ignore.getMessage(), ignore);
        }
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : packages.entrySet()) {
            if (result.length() != 0) {
                result.append(",");
            }
            result.append(entry.getKey());
            result.append(";version=\"");
            result.append(entry.getValue());
            result.append('"');
        }

        return result.toString();
    }

    /**
     * Adds the packages that a manifest exports, if any.
     *
     * @param packages the packages, and their corresponding versions
     * @param manifest the manifest
     * @param logger   the Felix logger
     */
    private void addPackages(Map<String, String> packages, Map<String, Object> manifest, Logger logger) {
        try {
            Map<String, Object> configMap = Collections.emptyMap();
            ManifestParser parser = new ManifestParser(logger, configMap, BUNDLE_REVISION, manifest);
            for (BundleCapability capability : parser.getCapabilities()) {
                if (BundleRevision.PACKAGE_NAMESPACE.equals(capability.getNamespace())) {
                    Map<String, Object> attributes = capability.getAttributes();
                    String pkg = getString(PackageNamespace.PACKAGE_NAMESPACE, attributes);
                    if (pkg != null && !pkg.startsWith("org.osgi.") && !pkg.startsWith("java.")) {
                        String version = getString(PackageNamespace.CAPABILITY_BUNDLE_VERSION_ATTRIBUTE, attributes);
                        if (version != null) {
                            packages.put(pkg, version);
                        }
                    }
                }
            }
        } catch (BundleException ignore) {
            log.debug(ignore.getMessage(), ignore);
        }
    }

    /**
     * Returns the value of an attribute.
     *
     * @param name       the attribute name
     * @param attributes the attributes
     * @return the corresponding value, or {@code null} if none is found
     */
    private String getString(String name, Map<String, Object> attributes) {
        Object result = attributes.get(name);
        return (result != null) ? result.toString() : null;
    }

    /**
     * Parses the manifest at the specified URL, returning the main attributes if it exports packages.
     *
     * @param url the manifest URL
     * @return the manifest's main attributes, or {@code null} if it doesn't export packages
     */
    private Map<String, Object> getManifest(URL url) {
        Map<String, Object> result = null;
        try (InputStream stream = url.openStream()) {
            Manifest manifest = new Manifest(stream);
            if (manifest.getMainAttributes().getValue(Constants.EXPORT_PACKAGE) != null) {
                result = new StringMap(manifest.getMainAttributes());
            }
        } catch (IOException ignore) {
            log.debug(ignore.getMessage(), ignore);
        }
        return result;
    }

    /**
     * Helper to enable the use of {@code ManifestParser}.
     */
    private static class MockBundleRevision implements BundleRevision {
        public String getSymbolicName() {
            return null;
        }

        public Version getVersion() {
            return null;
        }

        public List<BundleCapability> getDeclaredCapabilities(String namespace) {
            return null;
        }

        public List<BundleRequirement> getDeclaredRequirements(String namespace) {
            return null;
        }

        public int getTypes() {
            return 0;
        }

        public BundleWiring getWiring() {
            return null;
        }

        public List<Capability> getCapabilities(String namespace) {
            return null;
        }

        public List<Requirement> getRequirements(String namespace) {
            return null;
        }

        public Bundle getBundle() {
            return null;
        }
    }
}
