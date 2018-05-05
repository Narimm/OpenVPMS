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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.maven.db;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.openvpms.db.tool.DBTool;
import org.openvpms.maven.archetype.AbstractHibernateMojo;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Mojo to create the OpenVPMS database.
 *
 * @author Tim Anderson
 * @goal create
 * @requiresDependencyResolution test
 */
public class DatabaseCreateMojo extends AbstractHibernateMojo {

    /**
     * TODO - the JDBC properties are repeated from AbstractHibernateMojo as the maven plugin API cannot pick up
     * parameters otherwise.
     */

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
     * The JDBC admin user name.
     *
     * @parameter
     * @required
     */
    private String adminUsername;

    /**
     * The JDBC admin password.
     *
     * @parameter
     * @required
     */
    private String adminPassword;

    /**
     * The maven project to interact with.
     *
     * @parameter expression="${project}"
     * @required
     */
    private MavenProject project;

    /**
     * If {@code true}, skips execution.
     *
     * @parameter expression="false"
     * @optional
     */
    private boolean skip;

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
     * Returns the maven project.
     *
     * @return the project
     */
    @Override
    public MavenProject getProject() {
        return project;
    }

    /**
     * Returns the administrator user name.
     *
     * @return the administrator user name
     */
    public String getAdminUsername() {
        return adminUsername;
    }

    /**
     * Sets the administrator user name.
     *
     * @param adminUsername the administrator user name
     */
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    /**
     * Returns the administrator password.
     *
     * @return the administrator password
     */
    public String getAdminPassword() {
        return adminPassword;
    }

    /**
     * Sets the administrator password.
     *
     * @param adminPassword the administrator password
     */
    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    /**
     * Perform whatever build-process behavior this <code>Mojo</code> implements.
     * <br/>
     * This implementation sets the context class loader to be that of the project's test class path,
     * before invoking {@link #doExecute()}.
     *
     * @throws MojoExecutionException if an unexpected problem occurs.
     * @throws MojoFailureException   an expected problem (such as a compilation failure) occurs.
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!skip) {
            super.setDialect(""); // can't be null, but otherwise not required.
            super.execute();
        } else {
            getLog().info("Plugin is skipped");
        }
    }

    /**
     * Execute the plugin.
     *
     * @throws MojoExecutionException if an unexpected problem occurs.
     * @throws MojoFailureException   an expected problem (such as a compilation failure) occurs.
     */
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(getDriver());
        dataSource.setUsername(getAdminUsername());
        dataSource.setPassword(getAdminPassword());
        dataSource.setUrl(getUrl());
        DBTool tool = new DBTool(getDriver(), getUrl(), getUsername(), getPassword(), null);

        try (Connection connection = dataSource.getConnection()) {
            DbSupport dbSupport = DbSupportFactory.createDbSupport(connection, true);
            Schema schema = dbSupport.getSchema(tool.getSchemaName());
            if (schema.exists()) {
                getLog().info("Dropping database " + tool.getSchemaName());
                schema.drop();
            }
        } catch (SQLException exception) {
            throw new MojoExecutionException("Failed to drop database " + tool.getSchemaName(), exception);
        }
        try {
            getLog().info("Creating database " + tool.getSchemaName());
            tool.create(getAdminUsername(), getAdminPassword(), true);
        } catch (SQLException exception) {
            throw new MojoExecutionException("Failed to create database " + tool.getSchemaName(), exception);
        }
    }

}
