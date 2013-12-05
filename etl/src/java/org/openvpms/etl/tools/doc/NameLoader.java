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
 *  Copyright 2008-2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.etl.tools.doc;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSet;
import org.openvpms.component.system.common.query.NodeSetQueryIterator;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Document loader that:
 * <ul>
 * <li>locates all <em>DocumentActs</em> with a non-null filename and no
 * associated <em>Document</em>
 * <li>for each <em>DocumentAct</em> tries to find the named file from the
 * source directory and attach it to the act.
 * </ul>
 *
 * @author Tim Anderson
 */
class NameLoader extends AbstractLoader {

    /**
     * The document act reference iterator.
     */
    private Iterator<IMObjectReference> iterator;

    /**
     * The source directory.
     */
    private final File dir;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(NameLoader.class);

    /**
     * Creates a new {@code NameLoader}.
     *
     * @param dir        the source directory
     * @param shortNames the document archetype(s) that may be loaded to. May be {@code null}, or contain
     *                   wildcards
     * @param service    the archetype service
     * @param factory    the document factory
     */
    public NameLoader(File dir, String[] shortNames, IArchetypeService service, DocumentFactory factory) {
        super(service, factory);
        this.dir = dir;
        String[] expanded = getDocumentActShortNames(shortNames);
        if (expanded.length == 0) {
            throw new IllegalArgumentException("Argument 'shortNames' doesn't refer to any valid archetype for loading "
                                               + "documents to: " + ArrayUtils.toString(shortNames));
        }
        if (log.isInfoEnabled()) {
            log.info("Loading documents for archetypes=" + StringUtils.join(expanded, ", "));
        }

        List<IMObjectReference> refs = getDocumentActs(expanded);
        log.info("Found " + refs.size() + " documents");
        iterator = refs.iterator();
    }

    /**
     * Determines if there is a document to load.
     *
     * @return {@code true} if there is a document to load, otherwise {@code false}
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Loads the next document.
     *
     * @return {@code true} if the document was loaded successfully
     */
    public boolean loadNext() {
        boolean result = false;
        IMObjectReference ref = iterator.next();
        DocumentAct act = getDocumentAct(ref);
        if (act != null) {
            File file = new File(dir, act.getFileName());
            try {
                Document doc = createDocument(file);
                addDocument(act, doc);
                notifyLoaded(file, act.getId());
            } catch (Throwable exception) {
                notifyError(file, exception);
            }
            result = true;
        }
        return result;
    }


    /**
     * Returns all document act references for the specified short names.
     *
     * @param shortNames the shortNames
     * @return the matching document act references
     */
    private List<IMObjectReference> getDocumentActs(String[] shortNames) {
        ArchetypeQuery query = new ArchetypeQuery(new ShortNameConstraint(shortNames, false, true));
        if (log.isInfoEnabled()) {
            StringBuilder buff = new StringBuilder();
            for (String s : shortNames) {
                if (buff.length() != 0) {
                    buff.append(", ");
                }
                buff.append(s);
            }
            log.info("Querying archetypes: " + buff);
        }
        List<IMObjectReference> refs = new ArrayList<IMObjectReference>();
        query.add(new NodeConstraint("document", RelationalOp.IS_NULL));
        query.setMaxResults(1000);
        List<String> nodes = Arrays.asList("fileName");
        Iterator<NodeSet> iter = new NodeSetQueryIterator(query, nodes);

        // need to build up a list of matching references first, as updates
        // to the document reference will affect paging
        while (iter.hasNext()) {
            NodeSet set = iter.next();
            String fileName = (String) set.get("fileName");
            if (!StringUtils.isEmpty(fileName)) {
                refs.add(set.getObjectReference());
            }
        }
        return refs;
    }

    /**
     * Helper to return a document act given its reference.
     *
     * @param reference the document act reference
     * @return the corresponding act, or {@code null}
     */
    private DocumentAct getDocumentAct(IMObjectReference reference) {
        return (DocumentAct) getService().get(reference);
    }

}
