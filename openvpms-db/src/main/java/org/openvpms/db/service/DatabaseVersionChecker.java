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
 * Checks that the database has been updated to the latest version.
 *
 * @author Tim Anderson
 */
public class DatabaseVersionChecker {

    /**
     * Constructs a {@link DatabaseVersionChecker}.
     *
     * @param service the database service
     * @throws SQLException if the database has not be updated to the latest version
     */
    public DatabaseVersionChecker(DatabaseService service) throws SQLException {
        if (service.needsUpdate()) {
            throw new SQLException("The database needs to be updated to version " + service.getMigrationVersion());
        }
    }

}
