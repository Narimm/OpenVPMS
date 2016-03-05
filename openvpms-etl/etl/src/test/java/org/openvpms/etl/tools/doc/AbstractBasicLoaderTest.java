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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Helpers common to {@link IdLoader} and {@link NameLoader}.
 *
 * @author Tim Anderson
 */
public abstract class AbstractBasicLoaderTest extends AbstractLoaderTest {

    /**
     * Helper to load files.
     *
     * @param source    the source directory to load from
     * @param target    the target directory to move processed files to
     * @param overwrite if {@code true} overwrite existing documents
     * @return the loader listener
     */
    protected LoaderListener load(File source, File target, boolean overwrite) {
        return load(source, null, target, overwrite);
    }

    /**
     * Helper to load files.
     *
     * @param source    the source directory to load from
     * @param shortName the document archetype(s) that may be loaded to. May be {@code null}, or contain wildcards
     * @param target    the target directory to move processed files to
     * @param overwrite if {@code true} overwrite existing documents
     * @return the loader listener
     */
    protected LoaderListener load(File source, String shortName, File target, boolean overwrite) {
        String[] shortNames = shortName != null ? new String[]{shortName} : null;
        Loader loader = createLoader(source, shortNames, getArchetypeService(),
                                     new DefaultDocumentFactory(getArchetypeService()),
                                     transactionManager, overwrite);
        LoaderListener listener = new LoggingLoaderListener(DocumentLoader.log, target);
        load(loader, listener);
        return listener;
    }

    /**
     * Creates a loader.
     *
     * @param source             the source directory to load from
     * @param shortNames         the document archetype(s) that may be loaded to. May be {@code null}
     * @param service            the archetype service
     * @param factory            the document factory
     * @param transactionManager the transaction manager
     * @param overwrite          if {@code true} overwrite existing documents
     * @return a new loader
     */
    protected abstract Loader createLoader(File source, String[] shortNames, IArchetypeService service,
                                           DocumentFactory factory, PlatformTransactionManager transactionManager,
                                           boolean overwrite);

    /**
     * Verifies the document versions associated with the version node of document act.
     *
     * @param act      the document act
     * @param versions the expected document versions
     */
    protected void checkVersions(DocumentAct act, Document... versions) {
        act = get(act);
        ActBean bean = new ActBean(act);
        List<DocumentAct> acts = bean.getNodeActs("versions", DocumentAct.class);
        assertEquals(versions.length, acts.size());
        for (DocumentAct childAct : acts) {
            checkAct(childAct);
            boolean found = false;
            for (Document version : versions) {
                if (childAct.getDocument().equals(version.getObjectReference())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
        }
    }
}
