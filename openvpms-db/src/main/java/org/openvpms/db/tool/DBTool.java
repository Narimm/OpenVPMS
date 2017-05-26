
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

package org.openvpms.db.tool;


import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.BaseFlywayCallback;
import org.flywaydb.core.api.callback.FlywayCallback;
import org.flywaydb.core.internal.info.MigrationInfoDumper;
import org.flywaydb.core.internal.util.TimeFormat;
import org.openvpms.db.service.impl.DatabaseServiceImpl;

import java.io.Console;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * Tool to create and migrate OpenVPMS databases.
 *
 * @author Tim Anderson
 */
public class DBTool {

    /**
     * The database service.
     */
    private final DatabaseServiceImpl service;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(DBTool.class);

    /**
     * Constructs an {@link DBTool}.
     *
     * @param driver   the driver class name
     * @param url      the driver url
     * @param user     the database user name
     * @param password the database password
     * @param listener the listener to notify of Flyway events. May be {@code null}
     */
    public DBTool(String driver, String url, String user, String password, FlywayCallback listener) {
        service = new DatabaseServiceImpl(driver, url, user, password, listener);
    }

    /**
     * Returns the schema name.
     *
     * @return the schema name
     */
    public String getSchemaName() {
        return service.getSchemaName();
    }

    /**
     * Creates the database and tables, if it doesn't already exist.
     *
     * @param adminUser     the admin user
     * @param adminPassword the admin password
     * @param createTables  if {@code true}, create the tables, and base-line
     * @throws SQLException for any SQL error
     */
    public void create(String adminUser, String adminPassword, boolean createTables) throws SQLException {
        service.create(adminUser, adminPassword, createTables);
        System.out.println("Created " + service.getSchemaName());
    }

    /**
     * Repair database version meta-data.
     */
    public void repair() {
        service.repair();
    }

    /**
     * Displays the database version.
     */
    public void version() {
        String version = service.getVersion();
        if (version == null) {
            System.out.println("Database '" + service.getSchemaName() + "' has no version information");
        } else {
            System.out.println("Database '" + service.getSchemaName() + "' is at version " + version);
        }
    }

    /**
     * Displays migration info.
     */
    public void info() {
        System.out.println(MigrationInfoDumper.dumpToAsciiTable(service.getInfo().all()));
    }

    /**
     * Determines if the database needs updating.
     *
     * @return {@code true} if the database needs updating
     */
    public boolean needsUpdate() {
        return service.needsUpdate();
    }

    /**
     * Updates the database to the latest version.
     *
     * @throws SQLException for any SQL error
     */
    public void update() throws SQLException {
        String version = service.getVersion();
        String schemaName = service.getSchemaName();
        if (service.needsUpdate()) {
            service.update();
            if (version == null) {
                // no version information
                System.out.println("Database '" + schemaName + "' updated to version " + service.getVersion());
            } else {
                System.out.println("Database '" + schemaName + "' updated from version " + version + " to "
                                   + service.getVersion());
            }
        } else {
            System.out.println("Database '" + schemaName + "' is up to date");
        }
    }

    /**
     * Returns the version that the database needs to be migrated to, if it is out of date.
     *
     * @return the version to migrate to, or {@code null} if the database doesn't need migration
     */
    public String getMigrationVersion() {
        return service.getMigrationVersion();
    }

    /**
     * The main line
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            JSAP parser = createParser();
            JSAPResult config = parser.parse(args);
            if (!config.success() || config.getBoolean("help")) {
                displayUsage(config);
            } else {
                String path = config.getString("properties");
                Properties properties = new Properties();
                properties.load(new FileInputStream(path));
                String driver = getRequired("hibernate.connection.driver_class", properties);
                String url = getRequired("hibernate.connection.url", properties);
                String user = getRequired("hibernate.connection.username", properties);
                String password = getRequired("hibernate.connection.password", properties);

                DBTool tool = new DBTool(driver, url, user, password, new FlywayLogger());
                String create = config.getString("create");
                if (create != null && (user = config.getString("user")) != null) {
                    boolean install = "install".equals(create);
                    boolean restore = "restore".equals(create);
                    if (install || restore) {
                        password = config.getString("password");
                        if (StringUtils.isEmpty(password)) {
                            Console console = System.console();
                            if (console == null) {
                                System.err.println("This command must be executed in a console");
                                System.exit(1);
                            }
                            console.printf("Enter password: ");
                            char[] pass;
                            while ((pass = console.readPassword()) == null || pass.length == 0) {
                                console.printf("Enter password: ");
                            }
                            password = new String(pass);
                            Arrays.fill(pass, ' ');
                        }
                        tool.create(user, password, install);
                    } else {
                        displayUsage(config);
                    }
                } else if (config.getBoolean("info")) {
                    tool.info();
                } else if (config.getBoolean("update")) {
                    if (tool.needsUpdate()) {
                        if (!config.getBoolean("backedup")) {
                            Console console = System.console();
                            if (console == null) {
                                System.err.println("This command must be executed in a console");
                                System.exit(1);
                            }
                            boolean done = false;
                            while (!done) {
                                console.printf("Has the database been backed up? [Y/n] ");
                                String input = console.readLine();
                                if ("Y".equals(input)) {
                                    done = true;
                                } else if ("n".equals(input)) {
                                    System.err.println("The database update can only be reverted by restoring a " +
                                                       "backup.\nBack up the database before re-running this command "
                                                       + "again");
                                    System.exit(1);
                                }
                            }
                        }
                        tool.update();
                    } else {
                        System.out.println("Database is up to date");
                    }
                } else if (config.getBoolean("version")) {
                    tool.version();
                } else if (config.getBoolean("repair")) {
                    tool.repair();
                } else {
                    displayUsage(config);
                }
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
        }
    }

    /**
     * Returns the named property.
     *
     * @param name       the property name
     * @param properties the properties
     * @return the property value
     * @throws IllegalStateException if the property does not exist
     */
    private static String getRequired(String name, Properties properties) {
        String result = properties.getProperty(name);
        if (result == null) {
            throw new IllegalStateException("Property not found: " + name);
        }
        return result;
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("create").setLongFlag("create").setDefault("false").setHelp(
                "Create the OpenVPMS database."));
        parser.registerParameter(new FlaggedOption("user").setShortFlag('u').setHelp("Admin user to create database"));
        parser.registerParameter(new FlaggedOption("password").setShortFlag('p')
                                         .setHelp("Admin password to create database"));
        parser.registerParameter(new Switch("info").setShortFlag('i')
                                         .setLongFlag("info").setDefault("false").setHelp(
                        "Displays migration info."));
        parser.registerParameter(new Switch("update").setLongFlag("update").setDefault("false").setHelp(
                "Updates the database to the latest version."));
        parser.registerParameter(new Switch("version").setShortFlag('v')
                                         .setLongFlag("version").setDefault("false").setHelp(
                        "Displays the database version."));
        parser.registerParameter(new Switch("help").setLongFlag("help").setDefault("false")
                                         .setHelp("Displays this help."));
        parser.registerParameter(new Switch("backedup").setLongFlag("database-is-backed-up").setDefault("false")
                                         .setHelp("If specified, disables backup prompting when updating the database"));
        parser.registerParameter(new FlaggedOption("properties").setLongFlag("properties")
                                         .setHelp("Database connection properties"));
        parser.registerParameter(new Switch("repair").setLongFlag("repair").setDefault("false").setHelp(
                "Repair version meta data."));
        return parser;
    }

    /**
     * Prints usage information and exits.
     *
     * @param result the parse result
     */
    private static void displayUsage(JSAPResult result) {
        Iterator iter = result.getErrorMessageIterator();
        while (iter.hasNext()) {
            System.err.println(iter.next());
        }
        System.err.println();
        System.err.println("Usage: dbtool [options]");
        System.err.println();
        System.err.println("  --create <install | restore> -u <user> [-p <password>]");
        System.err.println("    Creates the database. When:");
        System.err.println("    . install is specified, the database and tables will be created and version " +
                           "information added.");
        System.err.println("      Use this for new installations.");
        System.err.println("    . restore is specified, an empty database will be created.");
        System.err.println("      Use this when restoring backups to a new server");
        System.err.println();
        System.err.println("  --update");
        System.err.println("    Updates the database to the latest version");
        System.err.println();
        System.err.println("  --version");
        System.err.println("    Displays database version");
        System.err.println();
        System.err.println("  --info");
        System.err.println("    Displays database migration information");
        System.err.println();
        System.err.println("  --properties <path>");
        System.err.println("    Specifies the path to the database connection properties");
        System.err.println();
        System.err.println("  --help");
        System.err.println("    Displays this help");
        System.exit(1);
    }

    private static class FlywayLogger extends BaseFlywayCallback {

        private final Map<MigrationInfo, StopWatch> state = new HashMap<>();

        @Override
        public void beforeEachMigrate(Connection connection, MigrationInfo info) {
            StopWatch watch = new StopWatch();
            state.put(info, watch);
            System.out.print("Updating to " + info.getVersion() + " - " + info.getDescription() + " ... ");
            watch.start();
        }

        @Override
        public void afterEachMigrate(Connection connection, MigrationInfo info) {
            String time = "";
            StopWatch watch = state.get(info);
            if (watch != null) {
                watch.stop();
                time = TimeFormat.format(watch.getTime());
                System.out.println(time);
            }
        }
    }
}
