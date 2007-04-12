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

import be.ibridge.kettle.trans.step.BaseStepData;
import be.ibridge.kettle.trans.step.StepDataInterface;
import org.springframework.context.ApplicationContext;


/**
 * Data for the {@link MapValuesPlugin}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MapValuesPluginData extends BaseStepData
        implements StepDataInterface {

    /**
     * The application context.
     */
    private ApplicationContext context;


    /**
     * Constructs a new <tt>MapValuesPluginData</tt>.
     */
    public MapValuesPluginData() {
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
}
