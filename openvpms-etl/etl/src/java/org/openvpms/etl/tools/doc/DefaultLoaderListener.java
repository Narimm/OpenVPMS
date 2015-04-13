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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import java.io.File;


/**
 * Default implementation of the {@link LoaderListener} interface.
 *
 * @author Tim Anderson
 */
public class DefaultLoaderListener extends AbstractLoaderListener {

    /**
     * Constructs a {@link DefaultLoaderListener}.
     *
     * @param dir if non-null, files will be moved here on successful load
     */
    public DefaultLoaderListener(File dir) {
        super(dir);
    }
}
