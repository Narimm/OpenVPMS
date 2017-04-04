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

package org.openvpms.db.service;

import java.sql.SQLException;

/**
 * The {@link DatabaseService} provides database version information and migration support.
 *
 * @author Tim Anderson
 */
public interface DatabaseService {

    /**
     * Returns the schema name.
     *
     * @return the schema name
     */
    String getSchemaName();

    /**
     * Creates the database.
     *
     * @param adminUser     the database administrator user name
     * @param adminPassword the database administrator password
     * @param createTables  if {@code true}, create the tables, and base-line
     * @throws SQLException for any SQL error
     */
    void create(String adminUser, String adminPassword, boolean createTables) throws SQLException;

    /**
     * Determines if the database needs migration.
     *
     * @return {@code true} if the database needs migration
     */
    boolean needsUpdate();

    /**
     * Returns the current database version.
     *
     * @return the current database version, or {@code null} if it is not known
     */
    String getVersion();

    /**
     * Returns the version that the database needs to be migrated to, if it is out of date.
     *
     * @return the version to migrate to, or {@code null} if the database doesn't need migration
     */
    String getMigrationVersion();

    /**
     * Updates the database to the latest version.
     *
     * @throws SQLException for any error
     */
    void update() throws SQLException;
}