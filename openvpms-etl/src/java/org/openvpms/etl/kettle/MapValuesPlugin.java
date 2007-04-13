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

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Row;
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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.etl.load.ETLLogDAO;
import org.openvpms.etl.load.ErrorListener;
import org.springframework.context.ApplicationContext;

import java.util.List;


/**
 * 'Map values' kettle plugin.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * The loader.
     */
    private MapValuesPluginLoader loader;


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
            Thread thread = Thread.currentThread();
            ClassLoader current = thread.getContextClassLoader();
            try {
                ClassLoader classLoader
                        = MapValuesPlugin.class.getClassLoader();
                thread.setContextClassLoader(classLoader);
                List<IMObject> loaded = loader.load(row);
                if (loaded.isEmpty()) {
                    ++linesSkipped;
                }
            } finally {
                thread.setContextClassLoader(current);
            }
            putRow(row);

            result = true;
            if ((linesRead % Const.ROWS_UPDATE) == 0) {
                // log progress
                logBasic(Messages.get("MapValuesPlugin.Processed", linesRead));
            }
        } else {
            // no more input to be expected...
            setOutputDone();
        }
        return result;
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

    /**
     * Run the step.
     */
    public void run() {
        logBasic(Messages.get("MapValuesPlugin.Start"));
        try {
            getLoader();
            boolean process = true;
            while (process) {
                process = processRow(metaData, data) && !isStopped();
            }
        } catch (Throwable exception) {
            logError(Messages.get("MapValuesPlugin.UnexpectedError",
                                  exception.getMessage()));
            logError(Const.getStackTracker(exception));
            setErrors(1);
            stopAll();
        } finally {
            dispose(metaData, data);
            logBasic(Messages.get("MapValuesPlugin.Finished", linesRead));
            markStop();
        }
    }

    /**
     * Returns the loader, creating it if required
     *
     * @return a new loader
     * @throws KettleException for any error
     */
    private MapValuesPluginLoader getLoader() throws KettleException {
        if (loader == null) {
            ApplicationContext context = data.getContext();
            if (context == null) {
                throw new KettleException(
                        Messages.get("MapValuesPlugin.NoContext"));
            }
            ETLLogDAO dao = (ETLLogDAO) context.getBean("ETLLogDAO"); // NON-NLS
            IArchetypeService service
                    = (IArchetypeService) context.getBean(
                    "archetypeService"); // NON-NLS
            loader = new MapValuesPluginLoader(getStepname(),
                                               metaData.getMappings(),
                                               dao, service);
            loader.setErrorListener(new ErrorListener() {
                public void error(String legacyId, Throwable exception) {
                    logBasic(Messages.get("MapValuesPlugin.FailedToProcessRow",
                                          legacyId, exception));
                }

                public void error(Throwable exception) {
                    logBasic(Messages.get("MapValuesPlugin.FailedToProcess",
                                          exception));
                }
            });
        }
        return loader;
    }

}
