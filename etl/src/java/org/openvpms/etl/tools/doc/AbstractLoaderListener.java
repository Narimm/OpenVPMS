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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
abstract class AbstractLoaderListener implements LoaderListener {

    private final File dir;

    private int loaded = 0;

    private int skipped = 0;

    private int missing = 0;

    private int error = 0;

    public AbstractLoaderListener() {
        this(null);
    }

    public AbstractLoaderListener(File dir) {
        this.dir = dir;
    }

    public void loaded(File file) {
        doLoaded(file);
    }

    protected boolean doLoaded(File file) {
        boolean result = true;
        if (dir != null) {
            try {
                FileUtils.copyFileToDirectory(file, dir);
                file.delete();
                ++loaded;
            } catch (IOException exception) {
                result = false;
                error(file, exception);
            }
        } else {
            ++loaded;
        }
        return result;
    }

    public int getLoaded() {
        return loaded;
    }

    public int getErrors() {
        return error;
    }

    public int getProcessed() {
        return loaded + error;
    }

    public void alreadyLoaded(File file) {
        ++skipped;
        ++error;
    }

    public void missingAct(File file) {
        ++missing;
        ++error;
    }

    public void error(File file, Throwable exception) {
        ++error;
    }
}
