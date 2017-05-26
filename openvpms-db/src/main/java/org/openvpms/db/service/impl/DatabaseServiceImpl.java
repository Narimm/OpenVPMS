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

package org.openvpms.db.service.impl;

import org.apache.commons.dbcp2.BasicDataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.internal.dbsupport.DbSupport;
import org.flywaydb.core.internal.dbsupport.DbSupportFactory;
import org.flywaydb.core.internal.dbsupport.Schema;
import org.flywaydb.core.internal.dbsupport.SqlScript;
import org.flywaydb.core.internal.dbsupport.Table;
import org.flywaydb.core.internal.util.PlaceholderReplacer;
import org.flywaydb.core.internal.util.scanner.Resource;
import org.flywaydb.core.internal.util.scanner.classpath.ClassPathResource;
import org.openvpms.db.service.DatabaseService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the {@link DatabaseService}.
 *
 * @author Tim Anderson
 */
public class DatabaseServiceImpl implements DatabaseService {

    /**
     * The database driver class name.
     */
    private final String driver;

    /**
     * The root database URL.
     */
    private final String rootURL;

    /**
     * The database schema name.
     */
    private final String schemaName;

    /**
     * Flyway.
     */
    private final Flyway flyway;

    /**
     * The data source.
     */
    private final DataSource dataSource;

    /**
     * Constructs a {@link DatabaseServiceImpl}.
     *
     * @param driver   the driver class name
     * @param url      the driver url
     * @param user     the database user name
     * @param password the database password
     * @param listener the listener to notify of Flyway events. May be {@code null}
     */
    public DatabaseServiceImpl(String driver, String url, String user, String password, FlywayCallback listener) {
        this(driver, url, createDataSource(driver, url, user, password), listener);
    }

    /**
     * Constructs a {@link DatabaseServiceImpl}.
     *
     * @param driver     the driver class name
     * @param url        the driver url
     * @param dataSource the data source
     */
    public DatabaseServiceImpl(String driver, String url, DataSource dataSource) {
        this(driver, url, dataSource, null);
    }

    /**
     * Constructs a {@link DatabaseServiceImpl}.
     *
     * @param driver     the driver class name
     * @param url        the driver url
     * @param dataSource the data source
     * @param listener   the listener to notify of Flyway events. May be {@code null}
     */
    public DatabaseServiceImpl(String driver, String url, DataSource dataSource, final FlywayCallback listener) {
        this.driver = driver;
        int index = url.lastIndexOf('/');
        if (index == -1) {
            throw new IllegalArgumentException("Invalid JDBC URL: " + url);
        }

        rootURL = url.substring(0, index);
        schemaName = url.substring(index + 1);
        this.dataSource = dataSource;
        flyway = new Flyway();
        flyway.setDataSource(dataSource);
        flyway.setLocations("org/openvpms/db/migration");
        if (listener != null) {
            flyway.setCallbacks(listener);
        }
    }

    /**
     * Returns the schema name.
     *
     * @return the schema name
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * Creates the database.
     *
     * @param adminUser     the database administrator user name
     * @param adminPassword the database administrator password
     * @param createTables  if {@code true}, create the tables, and base-line
     * @throws SQLException for any SQL error
     */
    @Override
    public void create(String adminUser, String adminPassword, boolean createTables) throws SQLException {
        DataSource admin = createDataSource(driver, rootURL, adminUser, adminPassword);
        try (Connection connection = admin.getConnection()) {
            boolean found = false;
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet set = metaData.getCatalogs()) {
                while (set.next()) {
                    String schema = set.getString("TABLE_CAT");
                    if (schemaName.equalsIgnoreCase(schema)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                DbSupport support = DbSupportFactory.createDbSupport(connection, true);
                Resource resource = getResource("org/openvpms/db/schema/database.sql");
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("db.name", schemaName);
                SqlScript script = new SqlScript(support, resource, new PlaceholderReplacer(placeholders, "${", "}"),
                                                 "UTF-8", false);
                script.execute(support.getJdbcTemplate());
            } else if (!createTables) {
                throw new SQLException("Cannot create " + schemaName + " as it already exists");
            }
        }
        if (createTables) {
            try (Connection connection = dataSource.getConnection()) {
                DbSupport support = DbSupportFactory.createDbSupport(connection, true);
                Schema schema = support.getOriginalSchema();
                if (schema.allTables().length == 0) {
                    Resource resource = getResource("org/openvpms/db/schema/schema.sql");
                    SqlScript script = new SqlScript(resource.loadAsString("UTF-8"), support);
                    script.execute(support.getJdbcTemplate());
                    MigrationInfo version = getNewestVersion();
                    if (version != null) {
                        baseline(version.getVersion(), version.getDescription());
                    }
                } else {
                    throw new SQLException("Cannot create " + schemaName + " as there are tables already present");
                }
            }
        }
    }

    /**
     * Determines if the database needs migration.
     *
     * @return {@code true} if the database needs migration
     */
    @Override
    public boolean needsUpdate() {
        return getInfo().pending().length != 0;
    }

    /**
     * Returns the current database version.
     *
     * @return the current database version, or {@code null} if it is not known
     */
    @Override
    public String getVersion() {
        String result = null;
        MigrationInfo current = getInfo().current();
        if (current != null) {
            result = current.getVersion().toString();
        }
        return result;
    }

    /**
     * Returns the version that the database needs to be migrated to, if it is out of date.
     *
     * @return the version to migrate to, or {@code null} if the database doesn't need migration
     */
    public String getMigrationVersion() {
        MigrationInfo version = getNewestVersion();
        return version != null ? version.getVersion().toString() : null;
    }

    /**
     * Migrates the database to the latest version.
     *
     * @throws SQLException for any error
     */
    @Override
    public void update() throws SQLException {
        baseline();
        flyway.migrate();
    }

    /**
     * Repairs the Flyway metadata table. This will perform the following actions:
     * <ul>
     * <li>Remove any failed migrations on databases without DDL transactions (User objects left behind must still be
     * cleaned up manually)</li>
     * <li>Correct wrong checksums</li>
     * </ul>
     */
    public void repair() {
        flyway.repair();
    }

    /**
     * Returns the migration info.
     *
     * @return the migration info
     */
    public MigrationInfoService getInfo() {
        return flyway.info();
    }

    /**
     * Returns the schema version.
     *
     * @param schema the schema
     * @return the schema version, or {@code null} if it cannot be determined
     */
    protected String getExistingVersion(Schema schema) {
        Table acts = schema.getTable("acts");
        if (acts != null && acts.hasColumn("status2")) {
            return "1.9";
        }
        return null;
    }

    /**
     * Creates a new data source.
     *
     * @param driver   the driver class name
     * @param url      the database URL
     * @param user     the user
     * @param password the password
     * @return a new data source
     */
    private static DataSource createDataSource(String driver, String url, String user, String password) {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(driver);
        dataSource.setUrl(url);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        return dataSource;
    }

    /**
     * Helper to create a resource.
     *
     * @param path the resource path
     * @return a new resource
     */
    private Resource getResource(String path) {
        return new ClassPathResource(path, getClass().getClassLoader());
    }

    /**
     * Baselines the database, if there are existing tables.
     *
     * @return {@code true} if the database was baselined
     * @throws SQLException for any error
     */
    private boolean baseline() throws SQLException {
        MigrationInfo current = getInfo().current();
        if (current == null) {
            // no schema version information
            try (Connection connection = dataSource.getConnection()) {
                DbSupport support = DbSupportFactory.createDbSupport(connection, true);
                Schema schema = support.getOriginalSchema();
                if (schema.allTables().length != 0) {
                    // there are tables in the db. Make sure they belong to the most recent version
                    String existing = getExistingVersion(schema);
                    if (existing != null) {
                        // pre-existing schema. Don't want to create it again
                        baseline(MigrationVersion.fromVersion(existing), "Initial schema");
                    } else {
                        throw new SQLException("This database needs to be manually migrated to OpenVPMS 1.9");
                    }
                }
            }
        }
        return false;
    }

    /**
     * <p>Baselines an existing database, excluding all migrations up to and including version.</p>
     *
     * @param version     the version
     * @param description the description
     * @throws FlywayException if the schema baselining failed
     */
    private void baseline(MigrationVersion version, String description) {
        flyway.setBaselineVersion(version);
        flyway.setBaselineDescription(description);
        flyway.baseline();
    }

    /**
     * Returns the most recent version of the database that needs to be updated to.
     *
     * @return the most recent version of the database, or {@code null} if no update is required
     */
    private MigrationInfo getNewestVersion() {
        MigrationInfo[] info = getInfo().pending();
        return info.length > 0 ? info[info.length - 1] : null;
    }

}
