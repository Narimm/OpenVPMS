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
import org.apache.maven.plugin.MojoFailureException;
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
     * The assertion types file. If not specified, defaults to {@code dir/assertionTypes.xml}
     *
     * @parameter
     * @optional
     */
    private File assertionTypes;

    /**
     * Determines if existing archetypes should be overwritten.
     *
     * @parameter expression="true"
     * @optional
     */
    private boolean overwrite = true;

    /**
     * Determines if verbose logging is enabled.
     *
     * @parameter expression="true"
     * @optional
     */
    private boolean verbose = true;

    /**
     * If {@code true}, skips execution.
     *
     * @parameter expression="false"
     * @optional
     */
    private boolean skip;

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
     * Sets the assertion types file.
     * <p/>
     * Defaults to {@link #getDir() dir}/assertionTypes.xml.
     *
     * @param assertionTypes the assertion types file
     */
    public void setAssertionTypes(File assertionTypes) {
        this.assertionTypes = assertionTypes;
    }

    /**
     * Returns the assertion types file.
     *
     * @return the assertion types file. May be {@code null}
     */
    public File getAssertionTypes() {
        return assertionTypes;
    }

    /**
     * Determines if verbose logging is enabled.
     *
     * @param verbose if {@code true} log verbosely
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Determines if verbose logging is enabled.
     *
     * @return {@code true} if logging verbosely
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Determines if execution should be skipped.
     *
     * @param skip if {@code true}, skip execution
     */
    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    /**
     * Determines if execution is skipped.
     *
     * @return {@code true} if execution is skipped
     */
    public boolean isSkip() {
        return skip;
    }

    /**
     * Executes the archetype load, unless, execution is skipped.
     *
     * @throws MojoExecutionException if an unexpected problem occurs
     * @throws MojoFailureException   if an expected problem (such as a compilation failure) occurs
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            super.execute();
        } else {
            getLog().info("Archetype load is skipped");
        }
    }

    /**
     * Execute the plugin.
     *
     * @throws MojoExecutionException if an unexpected problem occurs
     * @throws MojoFailureException   if an expected problem (such as a compilation failure) occurs
     */
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
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
        TransactionStatus status = mgr.getTransaction(new DefaultTransactionDefinition());
        File mappingFile = (assertionTypes != null) ? assertionTypes : new File(dir, "assertionTypes.xml");
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

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public MavenProject getProject() {
        return project;
    }
}
