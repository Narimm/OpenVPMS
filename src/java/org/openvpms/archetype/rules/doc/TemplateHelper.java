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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;

import javax.print.attribute.standard.MediaTray;
import java.util.Iterator;
import java.util.List;


/**
 * Document template helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TemplateHelper {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a new <tt>TemplateHelper</tt>.
     */
    public TemplateHelper() {
        this(ArchetypeServiceHelper.getArchetypeService());
    }

    /**
     * Constructs a new <tt>TemplateHelper</tt>.
     *
     * @param service the archetype service
     */
    public TemplateHelper(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Retrieves a document template with matching name.
     *
     * @param name the document name
     * @return the document associated with an <em>act.documentTemplate</em>
     *         having the specified name, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document getDocument(String name) {
        Document result = null;
        ArchetypeQuery query = new ArchetypeQuery("act.documentTemplate",
                                                  false, true);
        query.add(new NodeConstraint("name", name));
        query.setFirstResult(0);
        query.setMaxResults(1);
        Iterator<DocumentAct> iterator
                = new IMObjectQueryIterator<DocumentAct>(service, query);
        if (iterator.hasNext()) {
            DocumentAct act = iterator.next();
            result = (Document) get(act.getDocReference());
        }
        return result;
    }

    /**
     * Returns an <em>entity.documentTemplate</em> with matching archetype
     * short name.
     *
     * @param shortName the archetype short name
     * @return the template corresponding to <tt>shortName</tt> or <tt>null</tt>
     *         if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Entity getTemplateForArchetype(String shortName) {
        Entity result = null;
        ArchetypeQuery query = new ArchetypeQuery("entity.documentTemplate",
                                                  false, true);
        Iterator<Entity> iterator
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
     * Returns the <em>entityRelationship.documentTemplatePrinter</em>
     * associated with an <em>entity.documentTemplate</em> for the given
     * <em>party.organisationPractice</em> or
     * <em>party.organisationLocation</em>.
     *
     * @param template     the template. An <em>entity.documentTemplate</em>
     * @param organisation the practice. An <em>party.organisationPractice</em>
     *                     or <em>party.organisationLocation</em>
     * @return the corresponding document template printer relationship, or
     *         <tt>null</tt> if none is found
     */
    public EntityRelationship getDocumentTemplatePrinter(Entity template,
                                                         Party organisation) {
        IMObjectBean templateBean = new IMObjectBean(template, service);
        IMObjectReference practiceRef = organisation.getObjectReference();
        for (EntityRelationship relationship :
                templateBean.getValues("printers", EntityRelationship.class)) {
            if (relationship.getTarget() != null &&
                    practiceRef.equals(relationship.getTarget())) {
                return relationship;
            }
        }
        return null;
    }

    /**
     * Returns the printer name from a document template printer relationship.
     *
     * @param printer the printer.
     *                An <em>entityRelationship.documentTemplatePrinter</em>
     * @return the printer name, or <tt>null</tt> if  none is defined
     */
    public String getPrinter(EntityRelationship printer) {
        IMObjectBean bean = new IMObjectBean(printer, service);
        return bean.getString("printerName");
    }

    /**
     * Returns the media tray from a document template printer relationship.
     *
     * @param printer the printer.
     *                An <em>entityRelationship.documentTemplatePrinter</em>
     * @return the media tray, or <tt>null</tt> if none is defined
     * @throws ArchetypeServiceException for any archetype service error
     */
    public MediaTray getMediaTray(EntityRelationship printer) {
        MediaTray tray = null;
        IMObjectBean bean = new IMObjectBean(printer, service);
        String trayName = bean.getString("paperTray");
        if (trayName != null) {
            tray = MediaHelper.getTray(trayName);
        }
        return tray;
    }

    /**
     * Determines if printing for a particular document template printer
     * relationship should be interactive or not.
     *
     * @param printer the printer.
     *                An <em>entityRelationship.documentTemplatePrinter</em>
     * @return <tt>true</tt> if printing should occur interactively
     */
    public boolean getInteractive(EntityRelationship printer) {
        IMObjectBean bean = new IMObjectBean(printer);
        return bean.getBoolean("interactive");
    }

    /**
     * Retrieves a document template with matching archetype short name.
     *
     * @param shortName the archetype short name
     * @return the template corresponding to <tt>shortName</tt> or <tt>null</tt>
     *         if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document getDocumentForArchetype(String shortName) {
        Document document = null;
        Entity template = getTemplateForArchetype(shortName);
        if (template != null) {
            DocumentAct act = getDocumentAct(template);
            if (act != null) {
                document = (Document) get(act.getDocReference());
            }
        }
        return document;
    }

    /**
     * Returns the document associated with an <em>entity.documentTemplate</em>.
     *
     * @param template the template. An <em>entity.documentTemplate</em>
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document getDocumentFromTemplate(Entity template) {
        DocumentAct act = getDocumentAct(template);
        if (act != null) {
            return (Document) get(act.getDocReference());
        }
        return null;
    }

    /**
     * Returns the document act associated with an
     * <em>entity.documentTemplate</em>.
     *
     * @param template the template. An <em>entity.documentTemplate</em>
     * @return the document act, or <code>null</code> if none exists
     * @throws ArchetypeServiceException for any archetype service error
     */
    public DocumentAct getDocumentAct(Entity template) {
        DocumentAct result = null;
        Participation participation = getDocumentParticipation(template);
        if (participation != null) {
            result = (DocumentAct) get(participation.getAct());
        }
        return result;
    }

    /**
     * Returns the first <em>participation.document</em> associated with an
     * <em>entity.documentTemplate</em>.
     *
     * @param template the document template entity
     * @return the participation, or <code>null</code> if none exists
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Participation getDocumentParticipation(Entity template) {
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
     * @param ref the object reference. May be <code>null</code>
     * @return the object corresponding to ref or <code>null</code> if none is
     *         found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private IMObject get(IMObjectReference ref) {
        if (ref != null) {
            return service.get(ref);
        }
        return null;
    }
}
