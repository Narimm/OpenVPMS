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

import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;
import org.springframework.context.ApplicationContext;


/**
 * Data for the {@link LoaderPlugin}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LoaderPluginData extends BaseStepData
        implements StepDataInterface {

    /**
     * The application context.
     */
    private ApplicationContext context;

    /**
     * The row meta data.
     */
    private RowMetaInterface rowMeta;


    /**
     * Constructs a new <tt>LoaderPluginData</tt>.
     */
    public LoaderPluginData() {
    }

    /**
     * Returns the application context.
     *
     * @return the application context
     */
    public ApplicationContext getContext() {
        return context;
    }

    /**
     * Sets the application context.
     *
     * @param context the application context
     */
    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Returns the row meta data.
     *
     * @return the row meta data
     */
    public RowMetaInterface getRowMeta() {
        return rowMeta;
    }

    /**
     * Sets the row meta data.
     *
     * @param rowMeta the row meta data
     */
    public void setRowMeta(RowMetaInterface rowMeta) {
        this.rowMeta = rowMeta;
    }
}
