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
import org.apache.commons.io.IOUtils;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.io.InputStream;
import java.math.BigDecimal;


/**
 * Jasper Report helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class JasperReportHelper {

    /**
     * Returns the value class of a node.
     *
     * @param descriptor the node descriptor
     */
    public static Class getValueClass(NodeDescriptor descriptor) {
        if (descriptor.isMoney()) {
            return BigDecimal.class;
        } else if (descriptor.isCollection()
                || descriptor.isObjectReference()) {
            return String.class;
        }
        return descriptor.getClazz();
    }

    /**
     * Loads a report resource.
     *
     * @param path the resource path
     * @return the design corresponding to <code>path</code>
     * @throws JRException if the resource can't be loaded
     */
    public static JasperDesign getReportResource(String path)
            throws JRException {
        InputStream stream = JasperReportHelper.class.getResourceAsStream(path);
        if (stream == null) {
            throw new JRException("Report resource not found: " + path);
        }
        return JRXmlLoader.load(stream);
    }

    /**
     * Returns a jasper report template given its name.
     *
     * @param name     the report name
     * @param handlers the document handlers
     * @return the jasper report template or <code>null</code> if none can be
     *         found
     * @throws DocumentException for any document error
     * @throws JRException       if the report can't be deserialized
     */
    public static JasperDesign getReport(String name, IArchetypeService service,
                                         DocumentHandlers handlers)
            throws JRException {
        TemplateHelper helper = new TemplateHelper(service);
        Document document = helper.getDocument(name);
        if (document != null) {
            return getReport(document, handlers);
        }
        return null;
    }

    /**
     * Returns a jasper report template corresponding to an archetype short
     * name.
     *
     * @param shortName the archetype short name
     * @param service   the archetype service
     * @param handlers  the document handlers
     * @return the jasper report template corresponding to
     *         <code>shortName</code> or <code>null</code> if none can be found.
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         for any document error
     * @throws JRException               if the report can't be deserialized
     */
    public static JasperDesign getReportForArchetype(String shortName,
                                                     IArchetypeService service,
                                                     DocumentHandlers handlers)
            throws JRException {
        TemplateHelper helper = new TemplateHelper(service);
        Document document = helper.getDocumentForArchetype(shortName);
        if (document != null) {
            return getReport(document, handlers);
        }
        return null;
    }

    /**
     * Deserializes a jasper report from a {@link Document}.
     *
     * @param document the document
     * @param handlers the document handlers
     * @return a new jasper report
     * @throws DocumentException for any document error
     * @throws JRException       if the report can't be deserialized
     */
    public static JasperDesign getReport(Document document,
                                         DocumentHandlers handlers)
            throws JRException {

        InputStream stream = null;
        try {
            DocumentHandler handler = handlers.get(document);
            stream = handler.getContent(document);
            return JRXmlLoader.load(stream);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }
}
