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

import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.apache.felix.main.AutoProcessor;
import org.osgi.framework.BundleException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Manifest;


/**
 * OpenVPMS Plugin manager.
 *
 * @author Tim Anderson
 */
public class PluginManager implements InitializingBean, DisposableBean {

    private final String path;
    private final PluginServiceProvider provider;

    private Felix felix;


    public PluginManager(String path, PluginServiceProvider provider) {
        this.path = path;
        this.provider = provider;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        start();
    }


    /**
     * Starts the plugin manager.
     *
     * @throws BundleException for any error
     */
    public void start() throws BundleException {
        File home = getHome();
        File system = getDir(home, "system", false);
        File etc = getDir(home, "etc", false);
        File deploy = getDir(home, "deploy", false);
        File data = getDir(home, "data", true);
        File storage = getDir(data, "cache", true);

        String exports = getExtraSystemPackages();
        // Create a configuration property map.
        Map config = new HashMap();
        config.put("plugin.home", home.getAbsolutePath());
        getConfiguration(etc, config);
        // Create host activator;
        List list = new ArrayList();
        list.add(new PluginServiceBundleActivator(provider));
        config.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, list);
        config.put(FelixConstants.FRAMEWORK_STORAGE, storage.getAbsolutePath());
        config.put(AutoProcessor.AUTO_DEPLOY_DIR_PROPERY, system.getAbsolutePath());
        config.put(FelixConstants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, exports);

        felix = new Felix(config);
        felix.init();
        AutoProcessor.process(config, felix.getBundleContext());
        felix.start();
    }

    /**
     * Stops the plugin manager.
     * <p/>
     * This method will wait until the manager shuts down.
     *
     * @throws BundleException      if the plugin manager cannot be stopped
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    public void destroy() throws BundleException, InterruptedException {
        felix.stop();
        felix.waitForStop(0);
    }

    /**
     * Determines the packages to export via the "org.osgi.framework.system.packages.extra" OSGi property.
     * <p/>
     * This uses the Export-Package attribute of each MANIFEST.MF available to the current class loader.
     * <p/>
     * It discards packages that don't have a version attribute, or begin with:
     * <ul>
     * <li>org.osgi</li>
     * <li>java</li>
     * <li>org.springframework</li>
     * </ul>
     *
     * @return the extra packages to export
     */
    private String getExtraSystemPackages() {
        Set<String> exports = new TreeSet<String>();
        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                InputStream stream = null;
                try {
                    stream = url.openStream();
                    Manifest manifest = new Manifest(stream);
                    String export = manifest.getMainAttributes().getValue("Export-Package");
                    if (export != null) {
                        String[] values = export.split(",");
                        for (String value : values) {
                            value = value.trim();
                            if (!value.isEmpty() && !value.startsWith("org.osgi") && !value.startsWith("java")
                                && !value.startsWith("org.springframework") && value.contains(";version=")) {
                                exports.add(value);
                            }
                        }
                    }
                } catch (IOException ignore) {
                    ignore.printStackTrace();
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        StringBuilder result = new StringBuilder();
        for (String value : exports) {
            if (result.length() != 0) {
                result.append(", ");
            }
            result.append(value);
        }

        return result.toString();
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

    private static final String DELIM_START = "${";
    private static final String DELIM_STOP = "}";

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
            cycleMap = new HashMap<String, String>();
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

}
