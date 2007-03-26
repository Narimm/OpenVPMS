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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.etl.kettle;

import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;


/**
 * Data for the {@link MapValuesPlugin}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MapValuesPluginData extends BaseStepData
        implements StepDataInterface {

    /**
     * The database.
     */
    private DatabaseMeta database;

    /**
     * Constructs a new <tt>MapValuesPluginData</tt>.
     */
    public MapValuesPluginData() {
    }

    /**
     * Returns the database.
     *
     * @return the database
     */
    public DatabaseMeta getDatabase() {
        return database;
    }

    /**
     * Sets the database.
     *
     * @param database the database
     */
    public void setDatabase(DatabaseMeta database) {
        this.database = database;
    }
}
