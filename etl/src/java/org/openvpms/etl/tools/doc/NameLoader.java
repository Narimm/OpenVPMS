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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
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
     * Creates a new <tt>NameLoader</tt>.
     *
     * @param dir       the source directory
     * @param shortName the shortName. If <tt>null</tt> indicates to query all
     *                  document acts. May contain wildcards
     * @param service   the archetype service
     * @param factory   the document factory
     */
    public NameLoader(File dir, String shortName, IArchetypeService service, DocumentFactory factory) {
        super(service, factory);
        this.dir = dir;
        String[] shortNames = getDocumentActShortNames(shortName);
        if (shortNames.length == 0) {
            throw new IllegalArgumentException("Argument 'shortName' doesn't refer to a valid archetype for loading "
                                               + "documents to: " + shortName);
        }

        List<IMObjectReference> refs = getDocumentActs(shortNames);
        log.info("Found " + refs.size() + " documents");
        iterator = refs.iterator();
    }

    /**
     * Determines if there is a document to load.
     *
     * @return <tt>true</tt> if there is a document to load, otherwise
     *         <tt>false</tt>
     */
    public boolean hasNext() {
        return iterator.hasNext();
    }

    /**
     * Loads the next document.
     *
     * @return <tt>true</tt> if the document was loaded successfully
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
                notifyLoaded(file);
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
     * @return the corresponding act, or <tt>null</tt>
     */
    private DocumentAct getDocumentAct(IMObjectReference reference) {
        return (DocumentAct) getService().get(reference);
    }


}
