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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;

import java.io.ByteArrayInputStream;
import java.util.List;


/**
 * Document template helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TemplateHelper {

    /**
     * Hibernate session factory. Only provided as a workaround for OBF-105
     */
    private static SessionFactory _sessionFactory;

    /**
     * The logger.
     */
    private static Log _log = LogFactory.getLog(TemplateHelper.class);


    /**
     * Initialise the template helper.
     * todo - Constructor only provided as a workaround for OBF-105
     *
     * @param factory the session factory
     */
    public TemplateHelper(SessionFactory factory) {
        _sessionFactory = factory;
    }

    /**
     * Returns a jasper report template given its name.
     *
     * @param name the report name
     * @return the jasper report template or <code>null</code> if none can be
     *         found
     * @throws JRException if the report can't be deserialized
     */
    public static JasperDesign getReport(String name, IArchetypeService service)
            throws JRException {
        Document document = getDocument(name, service);
        if (document != null) {
            return getReport(document);
        }
        return null;
    }

    /**
     * Returns a jasper report template corresponding to an archetype short
     * name.
     *
     * @param shortName the archetype short name
     * @param service   the archetype service
     * @return the jasper report template corresponding to <code>shortName</code>
     *         or <code>null</code> if none can be found.
     * @throws JRException if the report can't be deserialized
     */
    public static JasperDesign getReportForArchetype(String shortName,
                                                     IArchetypeService service)
            throws JRException {
        Document document = getDocumentForArchetype(shortName, service);
        if (document != null) {
            return getReport(document);
        }
        return null;
    }

    /**
     * Deserializes a jasper report from a {@link Document}.
     *
     * @param document the document
     * @return a new jasper report
     * @throws JRException if the report can't be deserialized
     */
    public static JasperDesign getReport(Document document)
            throws JRException {
        ByteArrayInputStream stream
                = new ByteArrayInputStream(document.getContents());
        return JRXmlLoader.load(stream);
    }

    /**
     * Retrieves a document template with matching name.
     *
     * @param name    the document name
     * @param service the archetype service
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
                DocumentAct act = getDocumentAct(bean);
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
     * Helper to refresh an entity within the context of a hibernate session.
     * todo this is a workaround for OBF-105
     *
     * @param entity the entity to refresh
     */
    public static void refresh(Entity entity) {
        if (!entity.isNew()) {
            Session session = _sessionFactory.openSession();
            try {
                session.refresh(entity);
                Hibernate.initialize(entity.getParticipations());
            } catch (Throwable throwable) {
                _log.error(throwable, throwable);
            } finally {
                session.close();
            }
        }
    }

    private static DocumentAct getDocumentAct(EntityBean bean) {
        refresh(bean.getEntity());
        return (DocumentAct) bean.getParticipant(
                "participation.documentTemplate");
    }

    /**
     * Helper to return an object given its reference.
     *
     * @param ref     the object reference. May be <code>null</code>
     * @param service the archetype service
     * @return the object corresponding to ref or <code>null</code> if none is
     *         found
     */
    private static IMObject get(IMObjectReference ref,
                                IArchetypeService service) {
        if (ref != null) {
            return ArchetypeQueryHelper.getByObjectReference(service, ref);
        }
        return null;
    }
}
