package org.openvpms.etl.kettle;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openvpms.etl.ETLObject;
import org.openvpms.etl.ETLObjectDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Collection;


/**
 * Map values step plugin.
 */
public class MapValuesPlugin extends BaseStep implements StepInterface {

    /**
     * The step data.
     */
    private MapValuesPluginData data;

    /**
     * The meta data.
     */
    private MapValuesPluginMeta metaData;

    /**
     * The DAO.
     */
    private ETLObjectDAO dao;

    /**
     * The row mapper listener.
     */
    private RowMapperListener listener;

    /**
     * The batch save cache.
     */
    private List<ETLObject> batch = new ArrayList<ETLObject>();

    /**
     * The batch size.
     */
    private int batchSize = 100;

    /**
     * The row mapper.
     */
    private RowMapper mapper;


    /**
     * Constructs a new <tt>MapValuesPlugin</tt>.
     *
     * @param stepMeta  the StepMeta object to run
     * @param data      the object to store temporary data, database
     *                  connections, caches, result sets, etc.
     * @param copyNr    The copynumber for this step
     * @param transMeta The TransInfo of which the step stepMeta is part of.
     * @param trans     The (running) transformation to obtain information
     *                  shared among the steps.
     */
    public MapValuesPlugin(StepMeta stepMeta, MapValuesPluginData data,
                           int copyNr, TransMeta transMeta, Trans trans) {
        super(stepMeta, data, copyNr, transMeta, trans);
        Logger.getRootLogger().setLevel(Level.INFO);
        listener = new RowMapperListener() {
            public void output(Collection<ETLObject> objects) throws KettleException {
                batch.addAll(objects);
                if (batch.size() >= batchSize) {
                    flushBatch();
                }
            }
        };
    }

    /**
     * Process one row.
     *
     * @param stepMeta The metadata to work with
     * @param stepData the temporary working data to work with
     * @return <tt>false</tt> if no more rows can be processed or an error
     *         occurred.
     * @throws KettleException for any error
     */
    public boolean processRow(StepMetaInterface stepMeta,
                              StepDataInterface stepData)
            throws KettleException {
        boolean result = false;
        metaData = (MapValuesPluginMeta) stepMeta;
        data = (MapValuesPluginData) stepData;

        Row row = getRow();    // get row, blocks when needed
        if (row != null) {
            try {
                RowMapper mapper = getRowMapper();
                mapper.map(row);
            } catch (Throwable exception) {
                throw new KettleException(exception);
            }
            putRow(row);

            result = true;
            if ((linesRead % Const.ROWS_UPDATE) == 0) {
                // Some basic logging every 5000 rows.
                logBasic("Linenr " + linesRead);
            }
        } else {
            // no more input to be expected...
            setOutputDone();
        }
        return result;
    }

    private ETLObjectDAO getDAO() throws KettleException {
        if (dao == null) {
            DatabaseMeta database = data.getDatabase();
            if (database == null) {
                throw new KettleException("No database selected");
            }
            try {
                Properties properties = new Properties();
                properties.put("hibernate.connection.driver_class",
                               database.getDriverClass());
                properties.put("hibernate.connection.url", database.getURL());
                properties.put("hibernate.connection.username",
                               database.getUsername());
                properties.put("hibernate.connection.password",
                               database.getPassword());
                properties.put("hibernate.show_sql", "false");
                properties.put("hibernate.jdbc.batch_size", "100");
                properties.put("hibernate.c3p0.min_size", "5");
                properties.put("hibernate.c3p0.max_size", "20");
                properties.put("hibernate.c3p0.timeout", "1800");
                properties.put("hibernate.c3p0.max_statements", "50");
                properties.put("hibernate.cache.provider_class",
                               "org.hibernate.cache.EhCacheProvider");
                properties.put("hibernate.cache.use_second_level_cache",
                               "true");
                properties.put("hibernate.cache.use_query_cache", "true");
                properties.put("hibernate.query.factory_class",
                               "org.hibernate.hql.ast.ASTQueryTranslatorFactory");

                dao = new ETLObjectDAO(properties);
            } catch (KettleException exception) {
                throw exception;
            } catch (Throwable exception) {
                throw new KettleException(exception);
            }
        }
        return dao;
    }

    /**
     * Initialises the step.
     *
     * @param stepMeta the metadata to work with
     * @param stepData the data to initialize
     * @return <tt>true</tt> if the step is initialised successfully, otherwise
     *         <tt>false</tt>
     */
    @Override
    public boolean init(StepMetaInterface stepMeta,
                        StepDataInterface stepData) {
        this.metaData = (MapValuesPluginMeta) stepMeta;
        this.data = (MapValuesPluginData) stepData;
        return super.init(stepMeta, stepData);
    }

    /**
     * Dispose of this step.
     *
     * @param stepMeta The metadata to work with
     * @param stepData The data to dispose of
     */
    @Override
    public void dispose(StepMetaInterface stepMeta,
                        StepDataInterface stepData) {
        metaData = (MapValuesPluginMeta) stepMeta;
        data = (MapValuesPluginData) stepData;
        super.dispose(stepMeta, stepData);
    }

    // Run is were the action happens!
    public void run() {
        logBasic("Starting to run...");
        try {
            boolean process = true;
            while (process) {
                process = processRow(metaData, data) && !isStopped();
                if (batch.size() >= batchSize) {
                    flushBatch();
                }
            }
            flushBatch();
        } catch (Exception exception) {
            logError("Unexpected error : " + exception.toString());
            logError(Const.getStackTracker(exception));
            setErrors(1);
            stopAll();
        } finally {
            dispose(metaData, data);
            logBasic("Finished, processing " + linesRead + " rows");
            markStop();
        }
    }

    /**
     * Returns the row mapper.
     *
     * @return the row mapper
     * @throws KettleException for any error
     */
    private RowMapper getRowMapper() throws KettleException {
        if (mapper == null) {
            mapper = new RowMapper(metaData.getMappings(), listener);
        }
        return mapper;
    }

    /**
     * Saves all objects in the batch.
     *
     * @throws KettleException for any error
     */
    private void flushBatch() throws KettleException {
        if (!batch.isEmpty()) {
            try {
                getDAO().save(batch);
                batch.clear();
            } catch (Throwable exception) {
                throw new KettleException(exception);
            }
        }
    }
}
