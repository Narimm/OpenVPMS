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

import java.util.ArrayList;
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
        List<IMObject> objects = new ArrayList<IMObject>();
        objects.add(act);

        if (act.getDocument() != null) {
            DocumentAct version = createVersion(act);
            if (version != null) {
                ActBean bean = new ActBean(act, service);
                bean.addNodeRelationship(VERSIONS, version);
                objects.add(version);
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
            ActBean bean = new ActBean(source, service);
            if (bean.hasNode(VERSIONS)) {
                IMObjectCopier copier = new IMObjectCopier(new VersioningCopyHandler(source, service));
                List<IMObject> objects = copier.apply(source);
                result = (DocumentAct) objects.get(0);
            }
        }
        return result;
    }

}
