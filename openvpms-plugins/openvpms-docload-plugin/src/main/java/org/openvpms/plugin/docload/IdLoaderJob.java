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
import org.openvpms.etl.tools.doc.IdLoader;
import org.openvpms.etl.tools.doc.Loader;
import org.openvpms.etl.tools.doc.LoggingLoaderListener;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.Resource;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class IdLoaderJob extends DocumentLoaderJob {

    private PlatformTransactionManager transactionManager;

    private static final Log log = LogFactory.getLog(IdLoaderJob.class);

    @Resource
    public void setPlatformTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;

    }

    @Override
    protected Loader createLoader() {
        boolean recurse = getConfiguration().getBoolean("recurse");
        boolean overwrite = getConfiguration().getBoolean("overwrite");
        IdLoader loader = new IdLoader(getSource(), getType(), getArchetypeService(), new DefaultDocumentFactory(),
                                       transactionManager, recurse, overwrite);
        loader.setListener(new LoggingLoaderListener(log, getTarget()));
        return loader;
    }
}
