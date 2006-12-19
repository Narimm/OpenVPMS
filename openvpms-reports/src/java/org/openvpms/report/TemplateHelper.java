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

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.QueryIterator;
import org.openvpms.archetype.rules.doc.MediaHelper;

import javax.print.attribute.standard.MediaTray;
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
        query.setFirstResult(0);
        query.setMaxResults(1);
        List<IMObject> rows = service.get(query).getResults();
        if (!rows.isEmpty()) {
            DocumentAct act = (DocumentAct) rows.get(0);
            return (Document) get(act.getDocReference(), service);
        }
        return null;
    }

    /**
     * Returns an <em>entity.documentTemplate</em> with matching archetype
     * short name.
     *
     * @param shortName the archetype short name
     * @param service   the archetype service
     * @return the template corresponding to <code>shortName</code> or
     *         <code>null</code> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Entity getTemplateForArchetype(String shortName,
                                                 IArchetypeService service) {
        Entity result = null;
        ArchetypeQuery query = new ArchetypeQuery("entity.documentTemplate",
                                                  false, true);
        QueryIterator<Entity> iterator
                = new IMObjectQueryIterator<Entity>(service, query);
        while (iterator.hasNext()) {
            EntityBean bean = new EntityBean(iterator.next(), service);
            String archetype = bean.getString("archetype");
            if (archetype != null && TypeHelper.matches(shortName, archetype)) {
                result = bean.getEntity();
                break;
            }
        }
        return result;
    }

    /**
     * Returns the default printer name for a template.
     *
     * @param template the document template
     * @param practice the practice (an <em>party.organisationPractice</em>)
     * @param service  the archetype service
     * @return the printer name for the template, or <code>null</code> if
     *         none is defined
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static String getPrinter(Entity template, Party practice,
                                    IArchetypeService service) {
        String printer = null;
        IMObjectBean templateBean = new IMObjectBean(template, service);
        IMObjectReference practiceRef = practice.getObjectReference();
        for (IMObject object : templateBean.getValues("printers")) {
            EntityRelationship relationship = (EntityRelationship) object;
            if (relationship.getTarget() != null &&
                    practiceRef.equals(relationship.getTarget())) {
                IMObjectBean bean = new IMObjectBean(object, service);
                printer = bean.getString("printerName");
                if (!StringUtils.isEmpty(printer)) {
                    break;
                }
            }
        }
        return printer;
    }

    /**
     * Returns the media tray for a template.
     *
     * @param template the document template
     * @param practice the practice (an <em>party.organisationPractice</em>)
     * @param printer  the printer
     * @param service  the archetype service
     * @return the media tray for the template, or <code>null</code> if
     *         none is defined
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static MediaTray getMediaTray(Entity template, Party practice,
                                         String printer,
                                         IArchetypeService service) {
        MediaTray tray = null;
        IMObjectBean templateBean = new IMObjectBean(template, service);
        IMObjectReference practiceRef = practice.getObjectReference();
        for (IMObject object : templateBean.getValues("printers")) {
            EntityRelationship relationship = (EntityRelationship) object;
            if (relationship.getTarget() != null &&
                    practiceRef.equals(relationship.getTarget())) {
                IMObjectBean bean = new IMObjectBean(object, service);
                String name = bean.getString("printerName");
                if (StringUtils.equals(printer, name)) {
                    tray = MediaHelper.getTray(bean.getString("paperTray"));
                    break;
                }
            }
        }
        return tray;
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
        Entity template = getTemplateForArchetype(shortName, service);
        if (template != null) {
            DocumentAct act = getDocumentAct(template, service);
            if (act != null) {
                document = (Document) get(act.getDocReference(), service);
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
        query.setFirstResult(0);
        query.setMaxResults(1);
        List<IMObject> rows = service.get(query).getResults();
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
