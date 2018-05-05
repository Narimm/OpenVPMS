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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;

/**
 * Document loader that loads documents to acts with the same file name.
 * <p/>
 * Note that when overwrite is {@code false}, it will report acts as missing if an act already has content.
 * <p/>
 * This is to avoid full table scans as there is no index on the file_name column.
 *
 * @author Tim Anderson
 */
class NameLoader extends AbstractLoader {

    /**
     * The load context.
     */
    private final LoadContext context;

    /**
     * Constructs a {@link NameLoader}.
     *
     * @param dir                the source directory
     * @param shortNames         the document archetype(s) that may be loaded to. May be {@code null}, or contain
     *                           wildcards
     * @param service            the archetype service
     * @param factory            the document factory
     * @param transactionManager the transaction manager
     * @param recurse            if {@code true} recursively scan the source dir
     * @param overwrite          if {@code true} overwrite existing documents
     * @param context            the load context
     */
    public NameLoader(File dir, String[] shortNames, IArchetypeService service, DocumentFactory factory,
                      PlatformTransactionManager transactionManager, boolean recurse, boolean overwrite,
                      LoadContext context) {
        super(dir, shortNames, service, factory, transactionManager, recurse, overwrite);
        this.context = context;
    }

    /**
     * Loads a document.
     *
     * @param file      the document
     * @param overwrite if {@code true} overwrite existing documents
     * @return {@code true} if the document was loaded successfully
     */
    @Override
    protected boolean load(File file, boolean overwrite) {
        boolean result = false;
        try {
            DocumentAct act = getAct(file, overwrite);
            if (act != null) {
                result = load(act, file, overwrite, context);
            } else {
                context.missingAct(file);
            }
        } catch (Exception exception) {
            context.error(file, exception);
        }
        return result;
    }

    /**
     * Returns the act associated with a file.
     *
     * @param file      the file
     * @param overwrite if {@code true} overwrite existing documents
     * @return the corresponding act, or {@code null}
     */
    private DocumentAct getAct(File file, boolean overwrite) {
        DocumentAct result = null;
        ArchetypeQuery query = new ArchetypeQuery(getShortNames(), true, true);
        query.add(Constraints.eq("fileName", file.getName()));
        if (!overwrite) {
            // there is no index on file name, so this should speed things up
            query.add(Constraints.isNull("document"));
        }
        IMObjectQueryIterator<DocumentAct> iterator = new IMObjectQueryIterator<>(getService(), query);
        if (iterator.hasNext()) {
            result = iterator.next();
            if (iterator.hasNext()) {
                throw new DocumentLoaderException(DocumentLoaderException.ErrorCode.DuplicateAct, file.getName());
            }
        }
        return result;
    }

}
