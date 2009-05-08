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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Properties;


/**
 * Abstract class for Maven plugins based on Spring and Hibernate. This injects the properties from a
 * <em>hibernate.properties</tt> file into the Spring application context.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractHibernateMojo extends AbstractMojo {

    /**
     * The hibernate properties.
     */
    private Properties properties;

    /**
     * The application context resource path.
     */
    protected final static String APPLICATION_CONTEXT = "mavenPluginApplicationContext.xml";


    /**
     * Returns the hibernate property file.
     *
     * @return the hibernate property file
     */
    public abstract File getPropertyfile();

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
        File file = getPropertyfile();
        if (file == null || !file.exists()) {
            throw new MojoExecutionException("Hibernate properties not found: " + file);
        }

        properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException exception) {
            throw new MojoExecutionException("Failed to load properties from: " + file, exception);
        }

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
        }
        catch (Exception e) {
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
