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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;

import java.util.List;


/**
 * Document template helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TemplateHelper {

    /**
     * Retrieves a document template with matching name.
     *
     * @param name    the document name
     * @param service the archetype service
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Document getDocument(String name, IArchetypeService service) {
        ArchetypeQuery query = new ArchetypeQuery("act.documentTemplate",
                                                  false, true);
        query.add(new NodeConstraint("name", name));
        query.setFirstRow(0);
        query.setNumOfRows(1);
        List<IMObject> rows = service.get(query).getRows();
        if (!rows.isEmpty()) {
            DocumentAct act = (DocumentAct) rows.get(0);
            return (Document) get(act.getDocReference(), service);
        }
        return null;
    }

    /**
     * Retrieves a document template with matching archetype short name.
     *
     * @param shortName the archetype short name
     * @param service   the archetype service
     * @return the template corresponding to <code>shortName</code> or
     *         <code>null</code> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Document getDocumentForArchetype(String shortName,
                                                   IArchetypeService service) {
        Document document = null;
        ArchetypeQuery query = new ArchetypeQuery("entity.documentTemplate",
                                                  false, true);
        query.setFirstRow(0);
        query.setNumOfRows(ArchetypeQuery.ALL_ROWS);
        List<IMObject> rows = service.get(query).getRows();
        for (IMObject object : rows) {
            EntityBean bean = new EntityBean((Entity) object);
            String archetype = bean.getString("archetype");
            if (archetype != null && TypeHelper.matches(shortName, archetype)) {
                DocumentAct act = getDocumentAct(bean.getEntity(), service);
                if (act != null) {
                    document = (Document) get(act.getDocReference(), service);
                    if (document != null) {
                        break;
                    }
                }
            }
        }
        return document;
    }

    /**
     * Returns the document associated with an <em>entity.documentTemplate</em>.
     *
     * @param entity  an <em>entity.documentTemplate</em>
     * @param service the archetype service
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Document getDocumentFromTemplate(Entity entity,
                                                   IArchetypeService service) {
        DocumentAct act = getDocumentAct(entity, service);
        if (act != null) {
            return (Document) get(act.getDocReference(), service);
        }
        return null;
    }

    /**
     * Returns the document act associated with an
     * <em>entity.documentTemplate</em>.
     *
     * @param template the document template entity
     * @param service  the archetype service
     * @return the document act, or <code>null</code> if none exists
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static DocumentAct getDocumentAct(Entity template,
                                             IArchetypeService service) {
        DocumentAct result = null;
        Participation participation = getDocumentParticipation(template,
                                                               service);
        if (participation != null) {
            result = (DocumentAct) get(participation.getAct(), service);
        }
        return result;
    }

    /**
     * Returns the first <em>participation.document</em> associated with an
     * <em>entity.documentTemplate</em>.
     *
     * @param template the document template entity
     * @param service  the archetype service
     * @return the participation, or <code>null</code> if none exists
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Participation getDocumentParticipation(
            Entity template, IArchetypeService service) {
        ArchetypeQuery query = new ArchetypeQuery("participation.document",
                                                  true, true);
        query.add(new ObjectRefNodeConstraint("entity",
                                              template.getObjectReference()));
        query.setFirstRow(0);
        query.setNumOfRows(1);
        List<IMObject> rows = service.get(query).getRows();
        return (!rows.isEmpty()) ? (Participation) rows.get(0) : null;
    }

    /**
     * Helper to return an object given its reference.
     *
     * @param ref     the object reference. May be <code>null</code>
     * @param service the archetype service
     * @return the object corresponding to ref or <code>null</code> if none is
     *         found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private static IMObject get(IMObjectReference ref,
                                IArchetypeService service) {
        if (ref != null) {
            return ArchetypeQueryHelper.getByObjectReference(service, ref);
        }
        return null;
    }
}
