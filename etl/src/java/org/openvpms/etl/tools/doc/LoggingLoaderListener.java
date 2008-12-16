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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.logging.Log;

import java.io.File;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class LoggingLoaderListener extends AbstractLoaderListener {

    private final Log log;

    public LoggingLoaderListener(Log log) {
        this.log = log;
    }

    @Override
    public void loaded(File file) {
        if (doLoaded(file)) {
            log.info("Loaded " + file.getPath());
        }
    }

    @Override
    public void alreadyLoaded(File file) {
        super.alreadyLoaded(file);
        log.info("Skipping " + file.getPath());
    }

    @Override
    public void missingAct(File file) {
        super.missingAct(file);
        log.info("Skipping " + file.getPath());
    }

    @Override
    public void error(File file, Throwable exception) {
        super.error(file, exception);
        log.info("Error " + file.getPath(), exception);
    }
}
