package org.openvpms.maven.archetype;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.tools.archetype.loader.ArchetypeLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.io.File;


/**
 * Plugin to load archetypes using {@link ArchetypeLoader}.
 *
 * @goal load
 * @requiresDependencyResolution test
 */
public class ArchetypeLoadMojo extends AbstractHibernateMojo {

    /**
     * The directory to load.
     *
     * @parameter
     * @required
     */
    private File dir;

    /**
     * Determines if existing archetypes should be overwritten.
     *
     * @parameter=true
     * @optional
     */
    private boolean overwrite = true;

    /**
     * Determines if verbose logging is enabled.
     *
     * @parameter=true
     * @optional
     */
    private boolean verbose = true;

    /**
     * The hibernate property file.
     *
     * @parameter
     * @required
     */
    private File propertyfile;

    /**
     * The maven project to interact with.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * Sets the directory to load archetypes from.
     *
     * @param dir the directory
     */
    public void setDir(File dir) {
        this.dir = dir;
    }

    /**
     * Returns the directory to load archetypes from.
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

    protected void doExecute() throws MojoExecutionException {
        if (dir == null || !dir.exists()) {
            throw new MojoExecutionException("Directory not found: " + dir);
        }
        if (!dir.isDirectory()) {
            throw new MojoExecutionException("Not a directory: " + dir);
        }
        ApplicationContext context = getContext();
        IArchetypeService service = (IArchetypeService) context.getBean("archetypeService");
        ArchetypeLoader loader = new ArchetypeLoader(service);
        loader.setOverwrite(overwrite);
        loader.setVerbose(verbose);

        PlatformTransactionManager mgr;
        mgr = (PlatformTransactionManager) context.getBean("txnManager");
        TransactionStatus status = mgr.getTransaction(
                new DefaultTransactionDefinition());
        File mappingFile = new File(dir, "assertionTypes.xml");
        try {
            if (mappingFile.exists()) {
                loader.loadAssertions(mappingFile.getPath());
            }
            loader.loadArchetypes(dir.getPath(), true);
            mgr.commit(status);
        } catch (Throwable throwable) {
            mgr.rollback(status);
            throw new MojoExecutionException("Failed to load archetypes", throwable);
        }
    }

    public File getPropertyfile() {
        return propertyfile;
    }

    public void setPropertyfile(File propertyfile) {
        this.propertyfile = propertyfile;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public MavenProject getProject() {
        return project;
    }
}
