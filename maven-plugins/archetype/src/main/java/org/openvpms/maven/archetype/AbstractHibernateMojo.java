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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.maven.archetype;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyResourceConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;


/**
 * Abstract class for Maven plugins based on Spring and Hibernate. This injects the properties from a
 * <em>hibernate.properties</tt> file into the Spring application context.
 *
 * @author Tim Anderson
 */
public abstract class AbstractHibernateMojo extends AbstractMojo {

    /**
     * The Hibernate dialect;
     *
     * @parameter
     * @required
     */
    private String dialect;

    /**
     * The JDBC driver class name.
     *
     * @parameter
     * @required
     */
    private String driver;

    /**
     * The JDBC URL.
     *
     * @parameter
     * @required
     */
    private String url;

    /**
     * The JDBC user name.
     *
     * @parameter
     * @required
     */
    private String username;

    /**
     * The JDBC password.
     *
     * @parameter
     * @required
     */
    private String password;

    /**
     * The hibernate properties.
     */
    private Properties properties;

    /**
     * The application context resource path.
     */
    protected final static String APPLICATION_CONTEXT = "mavenPluginApplicationContext.xml";

    /**
     * Returns the Hibernate dialect.
     *
     * @return the Hibernate dialect
     */
    public String getDialect() {
        return dialect;
    }

    /**
     * Sets the Hibernate dialect.
     *
     * @param dialect the hibernate dialect
     */
    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

    /**
     * Returns the JDBC driver class name.
     *
     * @return the JDBC driver class name
     */
    public String getDriver() {
        return driver;
    }

    /**
     * Sets the JDBC driver class name.
     *
     * @param driver the JDBC driver class name
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    /**
     * Returns the JDBC URL.
     *
     * @return the JDBC URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * RSets the JDBC URL.
     *
     * @param url the JDBC URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Returns the JDBC username.
     *
     * @return the JDBC username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the JDBC username.
     *
     * @param username the JDBC username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the JDBC password.
     *
     * @return the JDBC password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the JDBC password.
     *
     * @param password the JDBC password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the maven project.
     *
     * @return the project
     */
    public abstract MavenProject getProject();

    /**
     * Perform whatever build-process behavior this <code>Mojo</code> implements.
     * <br/>
     * This implementation sets the context class loader to be that of the project's test class path,
     * before invoking {@link #doExecute()}.
     *
     * @throws MojoExecutionException if an unexpected problem occurs.
     * @throws MojoFailureException   an expected problem (such as a compilation failure) occurs.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        properties = new Properties();
        properties.setProperty("hibernate.dialect", dialect);
        properties.setProperty("jdbc.driverClassName", driver);
        properties.setProperty("jdbc.url", url);
        properties.setProperty("jdbc.username", username);
        properties.setProperty("jdbc.password", password);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClassLoader());
            doExecute();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Execute the plugin.
     *
     * @throws MojoExecutionException if an unexpected problem occurs.
     * @throws MojoFailureException   an expected problem (such as a compilation failure) occurs.
     */
    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    /**
     * Returns the Spring application context.
     *
     * @return the application context
     * @throws BeansException for any error
     */
    protected ApplicationContext getContext() throws BeansException {
        return new Context();
    }

    /**
     * Returns the application context paths used to create the Spring application context.
     *
     * @return the context paths
     */
    protected String[] getContextPaths() {
        return new String[]{APPLICATION_CONTEXT};
    }

    /**
     * Helper to create a class loader using the project's test class path.
     *
     * @return a new classloader
     */
    private ClassLoader getClassLoader() {
        try {
            List classpathElements = getProject().getTestClasspathElements();

            URL urls[] = new URL[classpathElements.size()];
            for (int i = 0; i < classpathElements.size(); ++i) {
                urls[i] = new File((String) classpathElements.get(i)).toURL();
            }

            return new URLClassLoader(urls, this.getClass().getClassLoader());
        } catch (Exception e) {
            getLog().debug("Couldn't get the classloader.", e);
            return this.getClass().getClassLoader();
        }
    }

    /**
     * Helper to populate the Spring application context with hibernate properties.
     */
    private class Context extends ClassPathXmlApplicationContext {

        /**
         * Creates a new <tt>Context</tt>/
         *
         * @throws BeansException for any error
         */
        public Context() throws BeansException {
            super(getContextPaths());
        }

        /**
         * Modify the application context's internal bean factory after its standard
         * initialization. All bean definitions will have been loaded, but no beans
         * will have been instantiated yet. This allows for registering special
         * BeanPostProcessors etc in certain ApplicationContext implementations.
         *
         * @param beanFactory the bean factory used by the application context
         * @throws BeansException in case of errors
         */
        @Override
        protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            PropertyResourceConfigurer props = (PropertyResourceConfigurer) beanFactory.getBean("props");
            props.setProperties(properties);
        }
    }
}
