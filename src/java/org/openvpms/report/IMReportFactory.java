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

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.query.ObjectSet;
import static org.openvpms.report.IMReportException.ErrorCode.FailedToCreateReport;
import org.openvpms.report.jasper.DynamicJasperReport;
import org.openvpms.report.jasper.TemplatedJasperIMObjectReport;
import org.openvpms.report.jasper.TemplatedJasperObjectSetReport;
import org.openvpms.report.openoffice.OpenOfficeIMReport;


/**
 * A factory for {@link IMReport} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMReportFactory {

    /**
     * Creates a new report for a collection of {@link IMObject}s.
     *
     * @param template the document template
     * @param service  the archetype service
     * @param handlers the document handlers
     * @return a new report
     * @throws IMReportException         for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMReport<IMObject> createIMObjectReport(
            Document template, IArchetypeService service,
            DocumentHandlers handlers) {
        String name = template.getName();
        IMReport<IMObject> report;
        if (name.endsWith(DocFormats.JRXML_EXT)) {
            report = new TemplatedJasperIMObjectReport(template, service,
                                                       handlers);
        } else if (name.endsWith(DocFormats.ODT_EXT)) {
            report = new OpenOfficeIMReport<IMObject>(template, handlers);
        } else {
            throw new IMReportException(
                    FailedToCreateReport,
                    "Unrecognised document template: '" + name + "'");
        }
        return report;
    }

    /**
     * Creates a new report for a collection of {@link IMObject}s.
     *
     * @param shortName the archetype short name
     * @param service   the archetype service
     * @param handlers  the document handlers
     * @return a new report
     * @throws IMReportException         for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMReport<IMObject> createIMObjectReport(
            String shortName, IArchetypeService service,
            DocumentHandlers handlers) {
        Document doc = TemplateHelper.getDocumentForArchetype(shortName,
                                                              service);
        if (doc == null) {
            return new DynamicJasperReport(
                    service.getArchetypeDescriptor(shortName), service,
                    handlers);
        }
        return createIMObjectReport(doc, service, handlers);
    }

    /**
     * Creates a new report for a collection of {@link ObjectSet}s.
     *
     * @param template the document template
     * @param service  the archetype service
     * @param handlers the document handlers
     * @return a new report
     * @throws IMReportException         for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMReport<ObjectSet> createObjectSetReport(
            Document template, IArchetypeService service,
            DocumentHandlers handlers) {
        String name = template.getName();
        IMReport<ObjectSet> report;
        if (name.endsWith(DocFormats.JRXML_EXT)) {
            report = new TemplatedJasperObjectSetReport(template, service,
                                                        handlers);
        } else if (name.endsWith(DocFormats.ODT_EXT)) {
            report = new OpenOfficeIMReport<ObjectSet>(template, handlers);
        } else {
            throw new IMReportException(
                    FailedToCreateReport,
                    "Unrecognised document template: '" + name + "'");
        }
        return report;
    }

}
