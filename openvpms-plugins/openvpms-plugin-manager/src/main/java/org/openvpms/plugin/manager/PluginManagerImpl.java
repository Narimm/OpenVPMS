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

package org.openvpms.plugin.manager;

import org.apache.commons.io.IOUtils;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.Logger;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.framework.util.StringMap;
import org.apache.felix.framework.util.manifestparser.ManifestParser;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;
import org.osgi.framework.namespace.PackageNamespace;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRequirement;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.resource.Capability;
import org.osgi.resource.Requirement;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.jar.Manifest;


/**
 * OpenVPMS Plugin manager.
 *
 * @author Tim Anderson
 */
public class PluginManagerImpl implements PluginManager {

    private static final BundleRevision BUNDLE_REVISION = new MockBundleRevision();
    private static final String DELIM_START = "${";
    private static final String DELIM_STOP = "}";

    /**
     * The plugins path.
     */
    private final String path;

    /**
     * The plugin service provider.
     */
    private final PluginServiceProvider provider;

    /**
     * The servlet context.
     */
    private final ServletContext context;

    /**
     * The logger.
     */
    private final Logger logger = new Logger();

    /**
     * Apache Felix.
     */
    private Felix felix;

    /**
     * Constructs a {@link PluginManagerImpl}.
     *
     * @param path     the plugin installation path
     * @param provider the plugin service provider
     * @param context  the servlet context
     */
    public PluginManagerImpl(String path, PluginServiceProvider provider, ServletContext context) {
        this.path = path;
        this.provider = provider;
        this.context = context;
    }

    /**
     * Returns the first service implementing the specified interface.
     *
     * @param type the interface
     * @return the first service implementing the interface, or {@code null} if none was found
     */
    @Override
    public <T> T getService(Class<T> type) {
        T result = null;
        BundleContext context = getBundleContext();
        ServiceReference<T> reference = context.getServiceReference(type);
        if (reference != null) {
            result = getService(reference, context);
        }
        return result;
    }

    /**
     * Returns all services implementing the specified interface.
     *
     * @param type the interface
     * @return the services implementing the interface
     */
    @Override
    public <T> List<T> getServices(Class<T> type) {
        List<T> result = new ArrayList<>();
        BundleContext context = getBundleContext();
        if (context != null) {
            try {
                Collection<ServiceReference<T>> references = context.getServiceReferences(type, null);
                for (ServiceReference<T> reference : references) {
                    T service = getService(reference, context);
                    if (service != null) {
                        result.add(service);
                    }
                }
            } catch (InvalidSyntaxException ignore) {
                // do nothing
            }
        }
        return result;
    }

    /**
     * Returns a list of all installed bundles.
     *
     * @return the installed bundles
     */
    @Override
    public Bundle[] getBundles() {
        BundleContext context = getBundleContext();
        return context != null ? context.getBundles() : new Bundle[0];
    }

    /**
     * Starts the plugin manager.
     *
     * @throws BundleException for any error
     */
    @Override
    public synchronized void start() throws BundleException {
        if (felix == null) {
            File home = getHome();
            File etc = getDir(home, "etc", false);
            File system = getDir(home, "system", false);
            File deploy = getDir(home, "deploy", false);
            File data = getDir(home, "data", true);
            File storage = getDir(data, "cache", true);

            String exports = getExtraSystemPackages();

            Map<String, Object> config = new HashMap<>();
            config.put("plugin.home", home.getAbsolutePath());
            getConfiguration(etc, config);

            List<Object> list = new ArrayList<>();
            list.add(new PluginServiceBundleActivator(provider, context));
            config.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);
            config.put(FelixConstants.FRAMEWORK_STORAGE, storage.getAbsolutePath());
            config.put(AutoProcessor.AUTO_DEPLOY_ACTION_PROPERTY,
                       AutoProcessor.AUTO_DEPLOY_INSTALL_VALUE + ", " + AutoProcessor.AUTO_DEPLOY_START_VALUE);
            config.put(AutoProcessor.AUTO_DEPLOY_STARTLEVEL_PROPERTY, 5);
            config.put(AutoProcessor.AUTO_DEPLOY_DIR_PROPERTY, deploy.getAbsolutePath());
            config.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, exports);
            config.put(FelixConstants.LOG_LOGGER_PROP, logger);
            config.put(FelixConstants.LOG_LEVEL_PROP, "3");

            felix = new Felix(config);
            felix.init();
            AutoProcessor.process(config, felix.getBundleContext());
            felix.start();
        }
    }

    /**
     * Determines if the plugin manager is started.
     *
     * @return {@code true} if the plugin manager is started
     */
    @Override
    public synchronized boolean isStarted() {
        return felix != null;
    }

    /**
     * Stops the plugin manager.
     * <p>
     * This method will wait until the manager shuts down.
     */
    @Override
    public synchronized void stop() throws BundleException, InterruptedException {
        if (felix != null) {
            felix.stop();
            felix.waitForStop(0);
            felix = null;
        }
    }

    /**
     * Returns the bundle context, or {@code null} if Felix is not running.
     *
     * @return the bundle context. May be {@code null}
     */
    private synchronized BundleContext getBundleContext() {
        return (felix != null) ? felix.getBundleContext() : null;
    }

    private <T> T getService(ServiceReference<T> reference, BundleContext context) {
        T result = null;
        try {
            result = context.getService(reference);
        } catch (Exception exception) {

        }
        return result;
    }

    /**
     * Parses OSGi manifest entries to determine the packages that need to be added to the
     * <em>org.osgi.framework.system.packages.extra</em> property to expose them to plugins.
     *
     * @return the extra system packages
     */
    private String getExtraSystemPackages() {
        Map<String, String> packages = new TreeMap<>();
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                Map manifest = getManifest(resources.nextElement());
                if (manifest != null) {
                    addExports(packages, manifest);
                }
            }
        } catch (IOException ignore) {
            ignore.printStackTrace();
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

    private void addExports(Map<String, String> packages, Map<String, Object> manifest) {
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
            ignore.printStackTrace();
        }
    }

    private String getString(String key, Map<String, Object> attributes) {
        Object result = attributes.get(key);
        return (result != null) ? result.toString() : null;
    }

    /**
     * Parses the manifest at the specified URL, returning the main attributes if it exports packages.
     *
     * @param url the manifest URL
     * @return the manifest's main attributes, or {@code null} if it doesn't export packages
     */
    private Map getManifest(URL url) {
        Map result = null;
        InputStream stream = null;
        try {
            stream = url.openStream();
            Manifest manifest = new Manifest(stream);
            if (manifest.getMainAttributes().getValue(Constants.EXPORT_PACKAGE) != null) {
                result = new StringMap(manifest.getMainAttributes());
            }
        } catch (IOException ignore) {
            ignore.printStackTrace();
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return result;
    }

    private File getHome() {
        File file;
        try {
            file = new File(path).getCanonicalFile();
            if (!file.exists() || !file.isDirectory()) {
                throw new IllegalArgumentException("Invalid plugin directory: " + path);
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid plugin directory: " + path);
        }
        return file;
    }

    private void getConfiguration(File dir, Map config) {
        getConfig(dir, "config.properties", config);
        getConfig(dir, "startup.properties", config);
    }

    private void getConfig(File dir, String path, Map config) {
        Properties properties = new Properties();
        File file = new File(dir, path);
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load configuration: " + file);
        }
        for (String key : properties.stringPropertyNames()) {
            String value = substVars(properties.getProperty(key), key, null, config);
            properties.setProperty(key, value);
        }
        config.putAll(properties);
    }

    private File getDir(File parent, String path, boolean create) {
        File dir = new File(parent, path);
        if (!dir.exists()) {
            if (!create) {
                throw new IllegalArgumentException("Directory doesn't exist: " + dir);
            }
            if (!dir.mkdirs()) {
                throw new IllegalArgumentException("Failed to create directory: " + dir);
            }
        } else if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Invalid directory: " + dir);
        }

        return dir;
    }

    /**
     * <p>
     * This method performs property variable substitution on the
     * specified value. If the specified value contains the syntax
     * <tt>${&lt;prop-name&gt;}</tt>, where <tt>&lt;prop-name&gt;</tt>
     * refers to either a configuration property or a system property,
     * then the corresponding property value is substituted for the variable
     * placeholder. Multiple variable placeholders may exist in the
     * specified value as well as nested variable placeholders, which
     * are substituted from inner most to outer most. Configuration
     * properties override system properties.
     * </p>
     *
     * @param val         The string on which to perform property substitution.
     * @param currentKey  The key of the property being evaluated used to
     *                    detect cycles.
     * @param cycleMap    Map of variable references used to detect nested cycles.
     * @param configProps Set of configuration properties.
     * @return The value of the specified string after system property substitution.
     * @throws IllegalArgumentException If there was a syntax error in the
     *                                  property placeholder syntax or a recursive variable reference.
     */
    private String substVars(String val, String currentKey,
                             Map<String, String> cycleMap, Map configProps)
            throws IllegalArgumentException {
        // If there is currently no cycle map, then create
        // one for detecting cycles for this invocation.
        if (cycleMap == null) {
            cycleMap = new HashMap<>();
        }

        // Put the current key in the cycle map.
        cycleMap.put(currentKey, currentKey);

        // Assume we have a value that is something like:
        // "leading ${foo.${bar}} middle ${baz} trailing"

        // Find the first ending '}' variable delimiter, which
        // will correspond to the first deepest nested variable
        // placeholder.
        int stopDelim = val.indexOf(DELIM_STOP);

        // Find the matching starting "${" variable delimiter
        // by looping until we find a start delimiter that is
        // greater than the stop delimiter we have found.
        int startDelim = val.indexOf(DELIM_START);
        while (stopDelim >= 0) {
            int idx = val.indexOf(DELIM_START, startDelim + DELIM_START.length());
            if ((idx < 0) || (idx > stopDelim)) {
                break;
            } else if (idx < stopDelim) {
                startDelim = idx;
            }
        }

        // If we do not have a start or stop delimiter, then just
        // return the existing value.
        if ((startDelim < 0) && (stopDelim < 0)) {
            return val;
        }
        // At this point, we found a stop delimiter without a start,
        // so throw an exception.
        else if (((startDelim < 0) || (startDelim > stopDelim))
                 && (stopDelim >= 0)) {
            throw new IllegalArgumentException(
                    "stop delimiter with no start delimiter: "
                    + val);
        }

        // At this point, we have found a variable placeholder so
        // we must perform a variable substitution on it.
        // Using the start and stop delimiter indices, extract
        // the first, deepest nested variable placeholder.
        String variable =
                val.substring(startDelim + DELIM_START.length(), stopDelim);

        // Verify that this is not a recursive variable reference.
        if (cycleMap.get(variable) != null) {
            throw new IllegalArgumentException(
                    "recursive variable reference: " + variable);
        }

        // Get the value of the deepest nested variable placeholder.
        // Try to configuration properties first.
        String substValue = (String) configProps.get(variable);
        if (substValue == null) {
            // Ignore unknown property values.
            substValue = System.getProperty(variable, "");
        }

        // Remove the found variable from the cycle map, since
        // it may appear more than once in the value and we don't
        // want such situations to appear as a recursive reference.
        cycleMap.remove(variable);

        // Append the leading characters, the substituted value of
        // the variable, and the trailing characters to get the new
        // value.
        val = val.substring(0, startDelim)
              + substValue
              + val.substring(stopDelim + DELIM_STOP.length(), val.length());

        // Now perform substitution again, since there could still
        // be substitutions to make.
        val = substVars(val, currentKey, cycleMap, configProps);

        // Return the value.
        return val;
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
