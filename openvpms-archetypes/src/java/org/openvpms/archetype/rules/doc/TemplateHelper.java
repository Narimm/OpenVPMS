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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
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
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.isActiveNow;


/**
 * Document template helper.
 *
 * @author Tim Anderson
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
        ArchetypeQuery query = new ArchetypeQuery(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT, false, true);
        query.add(Constraints.eq("name", name));
        query.add(Constraints.sort("id"));
        query.setFirstResult(0);
        query.setMaxResults(1);
        Iterator<DocumentAct> iterator = new IMObjectQueryIterator<DocumentAct>(service, query);
        if (iterator.hasNext()) {
            DocumentAct act = iterator.next();
            result = (Document) get(act.getDocument());
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
        ArchetypeQuery query = new ArchetypeQuery(DocumentArchetypes.DOCUMENT_TEMPLATE, false, true);
        query.add(Constraints.sort("id"));
        Iterator<Entity> iterator = new IMObjectQueryIterator<Entity>(service, query);
        while (iterator.hasNext()) {
            Entity template = iterator.next();
            if (hasArchetype(template, shortName)) {
                result = template;
                break;
            }
        }
        return result;
    }

    /**
     * Returns an <em>entity.documentTemplate</em> for the specified archetype, associated with an organisation
     * location or practice.
     * <p/>
     * If there are multiple relationships, the lowest id will be returned.
     *
     * @param shortName    the archetype short name
     * @param organisation the <em>party.organisationLocation</em> or <em>party.organisationPractice</em>
     * @return the template corresponding to <tt>shortName</tt> or <tt>null</tt> if none can be found
     */
    public Entity getTemplateForArchetype(String shortName, Party organisation) {
        Entity result = null;
        EntityBean bean = new EntityBean(organisation, service);
        List<EntityRelationship> list = bean.getNodeRelationships("templates", isActiveNow());
        Collections.sort(list, new Comparator<EntityRelationship>() {
            public int compare(EntityRelationship o1, EntityRelationship o2) {
                long id1 = (o1.getSource() != null) ? o1.getSource().getId() : -1;
                long id2 = (o2.getSource() != null) ? o2.getSource().getId() : -1;
                return (id1 < id2 ? -1 : (id1 == id2 ? 0 : 1));
            }
        });
        for (EntityRelationship relationship : list) {
            Entity template = (Entity) get(relationship.getSource());
            if (template != null && template.isActive()) {
                if (hasArchetype(template, shortName)) {
                    result = template;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Returns the document template for the specified archetype.
     *
     * @param shortName the archetype short name
     * @return the template corresponding to <tt>shortName</tt> or <tt>null</tt> if none can be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public DocumentTemplate getDocumentTemplate(String shortName) {
        Entity result = getTemplateForArchetype(shortName);
        return (result != null) ? new DocumentTemplate(result, service) : null;
    }

    /**
     * Returns the document template for the specified archetype, associated with an organisation location or practice.
     * <p/>
     * If there are multiple relationships, the lowest id will be returned.
     *
     * @param shortName    the archetype short name
     * @param organisation the <em>party.organisationLocation</em> or <em>party.organisationPractice</em>
     * @return the template corresponding to <tt>shortName</tt> or <tt>null</tt> if none can be found
     */
    public DocumentTemplate getDocumentTemplate(String shortName, Party organisation) {
        Entity result = getTemplateForArchetype(shortName, organisation);
        return (result != null) ? new DocumentTemplate(result, service) : null;
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
                document = (Document) get(act.getDocument());
            }
        }
        return document;
    }

    /**
     * Returns the document associated with an <em>entity.documentTemplate</em>.
     *
     * @param template the template. An <em>entity.documentTemplate</em>
     * @return the corresponding document, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document getDocumentFromTemplate(Entity template) {
        DocumentAct act = getDocumentAct(template);
        if (act != null) {
            return (Document) get(act.getDocument());
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
     * Determine if a template is for a particular archetype.
     *
     * @param template  the template
     * @param shortName the archetype short name
     * @return <tt>true</tt> if the template has the archetype short name, otherwise <tt>false</tt>
     */
    private boolean hasArchetype(Entity template, String shortName) {
        EntityBean bean = new EntityBean(template, service);
        String archetype = bean.getString("archetype");
        return (archetype != null && TypeHelper.matches(shortName, archetype));
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
