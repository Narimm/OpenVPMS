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

package org.openvpms.maven.report;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.maven.archetype.AbstractHibernateMojo;
import org.openvpms.report.tools.TemplateLoader;
import org.springframework.context.ApplicationContext;

import java.io.File;


/**
 * Loads report templates using the {@link TemplateLoader}.
 *
 * @goal load
 */
public class ReportLoadMojo extends AbstractHibernateMojo {

    /**
     * The templates file.
     *
     * @parameter
     * @required
     */
    private File file;

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
     * The maven project to interact with.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Sets the templates file.
     *
     * @param file the file
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Returns the templates file.
     *
     * @return the templates file
     */
    public File getFile() {
        return file;
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
     * Loads report templates from the specified directory.
     *
     * @throws MojoExecutionException if an unexpected problem occurs
     */
    protected void doExecute() throws MojoExecutionException {
        if (file == null || !file.exists()) {
            throw new MojoExecutionException("File not found: " + file);
        }
        try {
            ApplicationContext context = getContext();
            IArchetypeService service = (IArchetypeService) context.getBean("archetypeService");
            DocumentHandlers handlers = (DocumentHandlers) context.getBean("documentHandlers");
            TemplateLoader loader = new TemplateLoader(service, handlers);
            loader.load(file.getPath());
        } catch (Throwable exception) {
            throw new MojoExecutionException("Failed to load report templates", exception);
        }
    }

    /**
     * Returns the application context paths used to create the Spring application context.
     *
     * @return the context paths
     */
    @Override
    protected String[] getContextPaths() {
        return new String[]{APPLICATION_CONTEXT, "reportContext.xml"};
    }

}