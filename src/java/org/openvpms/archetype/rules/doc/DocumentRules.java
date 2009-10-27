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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.archetype.rules.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IPage;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Document rules.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentRules {

    /**
     * The versions node.
     */
    public static final String VERSIONS = "versions";

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Creates a new <tt>DocumentRules</tt>.
     */
    public DocumentRules() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Creates a new <tt>DocumentRules</tt>.
     *
     * @param service the archetype service
     */
    public DocumentRules(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Determines if a document act support multiple document versions.
     *
     * @param act the document act
     * @return <tt>true</tt> if the act supports versioning
     */
    public boolean supportsVersions(DocumentAct act) {
        ActBean bean = new ActBean(act, service);
        return bean.hasNode(VERSIONS);
    }

    /**
     * Adds a document to a document act.
     * <p/>
     * If the act is currently has a document, and the act supports versioning, the original document will be saved
     * as a version using {@link #createVersion}.
     *
     * @param act      the act to add the document to
     * @param document the document to add
     * @return a list of objects to save
     */
    public List<IMObject> addDocument(DocumentAct act, Document document) {
        return addDocument(act, document, true);
    }

    /**
     * Adds a document to a document act.
     * <p/>
     * If <tt>version</tt> is <tt>true</tt>, the act is currently has a document, and the act supports versioning,
     * the original document will be saved as a version using {@link #createVersion}.
     *
     * @param act      the act to add the document to
     * @param document the document to add
     * @param version  if <tt>true</tt> version any old document if the act supports it
     * @return a list of objects to save
     */
    public List<IMObject> addDocument(DocumentAct act, Document document, boolean version) {
        List<IMObject> objects = new ArrayList<IMObject>();
        objects.add(act);

        if (version && act.getDocument() != null) {
            DocumentAct oldVersion = createVersion(act);
            if (oldVersion != null) {
                ActBean bean = new ActBean(act, service);
                bean.addNodeRelationship(VERSIONS, oldVersion);
                objects.add(oldVersion);
            }
        }
        act.setDocument(document.getObjectReference());
        act.setFileName(document.getName());
        act.setMimeType(document.getMimeType());
        act.setName(document.getName());

        if (document.isNew()) {
            objects.add(document);
        }
        return objects;
    }

    /**
     * Creates a version of a document act.
     * <p/>
     * For this to occur, the source act must have a <em>versions</em> node. An instance of the referred
     * to <em>version</em> act will be created, and a shallow copy of the source act's nodes copied to it.
     * This includes simple nodes and participations.
     *
     * @param source the act to version
     * @return a shallow copy of the act, or <tt>null</tt> if it can't be versioned
     */
    public DocumentAct createVersion(DocumentAct source) {
        DocumentAct result = null;
        IMObjectReference existing = source.getDocument();
        if (existing != null) {
            if (supportsVersions(source)) {
                IMObjectCopier copier = new IMObjectCopier(new VersioningCopyHandler(source, service));
                List<IMObject> objects = copier.apply(source);
                result = (DocumentAct) objects.get(0);
            }
        }
        return result;
    }

    /**
     * Determines if a document associated with an act duplicates that specified.
     * <p/>
     * The documents are considered duplicates if they both have the same size and checksum.
     * <p/>
     * If any of the sizes or checksums are zero, they are not considered duplicates.
     *
     * @param act      the document act
     * @param document the document to compare
     * @return <tt>true</tt> if the documents have the same length and checksum
     */
    public boolean isDuplicate(DocumentAct act, Document document) {
        boolean result = false;
        if (act.getDocument() != null && document.getDocSize() != 0 && document.getChecksum() != 0) {
            ArchetypeQuery query = new ArchetypeQuery(new ObjectRefConstraint("doc", act.getDocument()));
            query.add(new NodeSelectConstraint("doc.size"));
            query.add(new NodeSelectConstraint("doc.checksum"));
            IPage<ObjectSet> page = service.getObjects(query);
            if (!page.getResults().isEmpty()) {
                ObjectSet set = page.getResults().get(0);
                long size = set.getLong("doc.size");
                long checksum = set.getLong("doc.checksum");
                if (size != 0 && checksum != 0
                    && (size == document.getDocSize() && checksum == document.getChecksum())) {
                    result = true;
                }
            }
        }
        return result;
    }

    /**
     * Returns the versions of a document.
     *
     * @param act the document act
     * @return the versions of the document, or an empty list if the document has no versions
     */
    public List<DocumentAct> getVersions(DocumentAct act) {
        ActBean bean = new ActBean(act, service);
        if (bean.hasNode(VERSIONS)) {
            return bean.getNodeActs(VERSIONS, DocumentAct.class);
        }
        return Collections.emptyList();
    }

}
