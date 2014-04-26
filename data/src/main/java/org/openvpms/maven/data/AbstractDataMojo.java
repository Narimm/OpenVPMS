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

package org.openvpms.maven.data;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.maven.archetype.AbstractHibernateMojo;
import org.openvpms.tools.data.loader.StaxArchetypeDataLoader;
import org.springframework.context.ApplicationContext;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;


/**
 * Base class for the OpenVPMS data plugin.
 *
 * @author Tim Anderson
 */
public abstract class AbstractDataMojo extends AbstractHibernateMojo {

    /**
     * The directory to process files from.
     *
     * @parameter
     * @required
     */
    private File dir;

    /**
     * TODO - the JDBC properties are repeated from AbstractHibernateMojo as the maven plugin API cannot pick up
     * parameters otherwise.
     */

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
     * Determines if verbose logging is enabled.
     *
     * @parameter=true
     * @optional
     */
    private boolean verbose = true;

    /**
     * The maven project to interact with.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Sets the directory to process files from.
     *
     * @param dir the directory
     */
    public void setDir(File dir) {
        this.dir = dir;
    }

    /**
     * Returns the director5y to process files from.
     *
     * @return the directory
     */
    public File getDir() {
        return dir;
    }

    /**
     * Determines if verbose logging is enabled.
     *
     * @param verbose if <tt>true</tt> log verbosely
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Determines if verbose logging is enabled.
     *
     * @return <tt>true</tt> if logging verbosely
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets the maven project.
     *
     * @param project the project
     */
    public void setProject(MavenProject project) {
        this.project = project;
    }

    /**
     * Returns the maven project.
     *
     * @return the project
     */
    public MavenProject getProject() {
        return project;
    }

    /**
     * Load data from the specified directory.
     *
     * @throws MojoExecutionException if an unexpected problem occurs
     */
    protected void doExecute() throws MojoExecutionException {
        if (dir == null || !dir.exists()) {
            throw new MojoExecutionException("Directory not found: " + dir);
        }
        if (!dir.isDirectory()) {
            throw new MojoExecutionException("Not a directory: " + dir);
        }
        try {
            ApplicationContext context = getContext();
            IArchetypeService service = (IArchetypeService) context.getBean("archetypeService");
            StaxArchetypeDataLoader loader = new StaxArchetypeDataLoader(service);
            doExecute(loader);
        } catch (Throwable exception) {
            throw new MojoExecutionException("Failed to load data", exception);
        }
    }

    /**
     * Executes the data plugin goal.
     *
     * @param loader the archetype data loader to use
     * @throws XMLStreamException    if a file cannot be read
     * @throws FileNotFoundException if a file cannot be found
     */
    protected abstract void doExecute(StaxArchetypeDataLoader loader) throws XMLStreamException, FileNotFoundException;

    /**
     * Returns the application context paths used to create the Spring application context.
     *
     * @return the context paths
     */
    @Override
    protected String[] getContextPaths() {
        return new String[]{APPLICATION_CONTEXT, "dataloadContext.xml"};
    }
}
