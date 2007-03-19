package org.openvpms.etl.kettle;

import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;

public class MapValuesPluginData extends BaseStepData
        implements StepDataInterface {

    private DatabaseMeta database;

    public MapValuesPluginData() {
    }

    public DatabaseMeta getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseMeta database) {
        this.database = database;
    }
}
