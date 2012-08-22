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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.plugin.docload;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.etl.tools.doc.DefaultDocumentFactory;
import org.openvpms.etl.tools.doc.Loader;
import org.openvpms.etl.tools.doc.LoggingLoaderListener;
import org.openvpms.etl.tools.doc.NameLoader;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class NameLoaderJob extends DocumentLoaderJob {

    private static final Log log = LogFactory.getLog(NameLoaderJob.class);

    @Override
    protected Loader createLoader() {
        NameLoader loader = new NameLoader(getSource(), getType(), getArchetypeService(), new DefaultDocumentFactory());
        loader.setListener(new LoggingLoaderListener(log, getTarget()));
        return loader;
    }
}
