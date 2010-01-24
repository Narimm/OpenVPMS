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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.etl.load.ETLLogDAO;
import org.openvpms.etl.load.ErrorListener;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepStatus;
import org.springframework.context.ApplicationContext;

import java.util.List;


/**
 * Kettle plugin to load rows from legacy databases to OpenVPMS.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LoaderPlugin extends BaseStep implements StepInterface {

    /**
     * The step data.
     */
    private LoaderPluginData data;

    /**
     * The meta data.
     */
    private LoaderPluginMeta metaData;

    /**
     * The loader.
     */
    private LoaderAdapter loader;

    /**
     * The count of generated objects.
     */
    private int count;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(LoaderPlugin.class);


    /**
     * Constructs a new <tt>LoaderPlugin</tt>.
     *
     * @param stepMeta  the StepMeta object to run
     * @param data      the object to store temporary data, database
     *                  connections, caches, result sets, etc.
     * @param copyNr    The copynumber for this step
     * @param transMeta The TransInfo of which the step stepMeta is part of.
     * @param trans     The (running) transformation to obtain information
     *                  shared among the steps.
     */
    public LoaderPlugin(StepMeta stepMeta, LoaderPluginData data,
                        int copyNr, TransMeta transMeta, Trans trans) {
        super(stepMeta, data, copyNr, transMeta, trans);
        Logger.getRootLogger().setLevel(Level.INFO);
    }

    /**
     * Process one row.
     *
     * @param stepMeta The metadata to work with
     * @param stepData the temporary working data to work with
     * @return <tt>false</tt> if no more rows can be processed or an error occurred.
     * @throws KettleException for any error
     */
    public boolean processRow(StepMetaInterface stepMeta, StepDataInterface stepData) throws KettleException {
        boolean result = false;
        metaData = (LoaderPluginMeta) stepMeta;
        data = (LoaderPluginData) stepData;

        Object[] row = getRow();    // get row, blocks when needed
        if (row != null) {
            if (first) {
                first = false;
                data.setRowMeta(getInputRowMeta().clone());
            }

            ClassLoader prior = setClassLoader();

            try {
                List<IMObject> loaded = loader.load(data.getRowMeta(), row);
                if (loaded.isEmpty()) {
                    incrementLinesSkipped();
                } else {
                    count += loaded.size();
                }
            } finally {
                setClassLoader(prior);
            }
            putRow(data.getRowMeta(), row);

            result = true;
            long read = getLinesRead();
            if (checkFeedback(read)) {
                // log progress
                logBasic(Messages.get("LoaderPlugin.Processed", read));
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
     * @return <tt>true</tt> if the step is initialised successfully, otherwise <tt>false</tt>
     */
    @Override
    public boolean init(StepMetaInterface stepMeta, StepDataInterface stepData) {
        this.metaData = (LoaderPluginMeta) stepMeta;
        this.data = (LoaderPluginData) stepData;
        return super.init(stepMeta, stepData);
    }

    /**
     * Dispose of this step.
     *
     * @param stepMeta The metadata to work with
     * @param stepData The data to dispose of
     */
    @Override
    public void dispose(StepMetaInterface stepMeta, StepDataInterface stepData) {
        metaData = (LoaderPluginMeta) stepMeta;
        data = (LoaderPluginData) stepData;

        try {
            closeLoader();
        } catch (Throwable exception) {
            logError(Messages.get("LoaderPlugin.UnexpectedError", exception.getMessage()));
            logError(Const.getStackTracker(exception));
        }

        super.dispose(stepMeta, stepData);
    }

    /**
     * Run the step.
     */
    public void run() {
        logBasic(Messages.get("LoaderPlugin.Start"));
        try {
            getLoader();
            boolean process = true;
            count = 0;
            while (process) {
                process = processRow(metaData, data) && !isStopped();
            }
            closeLoader();
        } catch (Throwable exception) {
            logError(Messages.get("LoaderPlugin.UnexpectedError", exception.getMessage()));
            logError(Const.getStackTracker(exception));
            setErrors(1);
            stopAll();
        } finally {
            dispose(metaData, data);
            StepStatus status = new StepStatus(this);
            float lapsed = ((float) getRuntime()) / 1000;
            double speed = 0.0;
            if (lapsed != 0) {
                speed = Math.floor(10 * (count / lapsed)) / 10;
            }

            logBasic(Messages.get("LoaderPlugin.Finished", status.getLinesRead(), status.getSeconds(),
                                  status.getSpeed(), count, speed));
            markStop();
        }
    }

    /**
     * Returns the loader, creating it if required
     *
     * @return a new loader
     * @throws KettleException for any error
     */
    private LoaderAdapter getLoader() throws KettleException {
        if (loader == null) {
            ApplicationContext context = data.getContext();
            if (context == null) {
                throw new KettleException(Messages.get("LoaderPlugin.NoContext"));
            }
            ETLLogDAO dao = (ETLLogDAO) context.getBean("ETLLogDAO"); // NON-NLS
            IArchetypeService service = (IArchetypeService) context.getBean("archetypeService"); // NON-NLS
            loader = new LoaderAdapter(getStepname(), metaData.getMappings(), dao, service);
            loader.setErrorListener(new ErrorListener() {
                public void error(String legacyId, String message, Throwable exception) {
                    String msg = Messages.get("LoaderPlugin.FailedToProcessRow", legacyId, message);
                    logBasic(msg);
                    log.error(msg, exception);
                }

                public void error(String message, Throwable exception) {
                    String msg = Messages.get("LoaderPlugin.FailedToProcess", message);
                    logBasic(msg);
                    log.error(msg, exception);
                }
            });
        }
        return loader;
    }

    /**
     * Closes the loader.
     */
    private void closeLoader() {
        if (loader != null) {
            ClassLoader prior = setClassLoader();
            try {
                loader.close();
            } finally {
                setClassLoader(prior);
            }
        }
    }

    /**
     * Helper to set the context class loader to this instances' class loader.
     *
     * @return the prior context class loader
     */
    private ClassLoader setClassLoader() {
        return setClassLoader(LoaderPlugin.class.getClassLoader());
    }

    /**
     * Helper to set the context class loader.
     *
     * @param loader the loader to set
     * @return the prior context class loader
     */
    private ClassLoader setClassLoader(ClassLoader loader) {
        Thread thread = Thread.currentThread();
        ClassLoader current = thread.getContextClassLoader();
        thread.setContextClassLoader(loader);
        return current;
    }
}
