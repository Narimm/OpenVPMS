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
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ObjectSet;
import static org.openvpms.report.ReportException.ErrorCode.NoTemplateForArchetype;
import static org.openvpms.report.ReportException.ErrorCode.UnsupportedTemplate;
import org.openvpms.report.jasper.TemplatedJasperIMObjectReport;
import org.openvpms.report.jasper.TemplatedJasperObjectSetReport;
import org.openvpms.report.msword.MsWordIMReport;
import org.openvpms.report.openoffice.OpenOfficeIMReport;


/**
 * A factory for {@link Report} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportFactory {

    /**
     * Creates a new report.
     *
     * @param template the document template
     * @param service  the archetype service
     * @param handlers the document handlers
     * @return a new report
     * @throws ReportException for any report error
     */
    public static Report createReport(Document template, IArchetypeService service, DocumentHandlers handlers) {
        String name = template.getName();
        Report report;
        if (name.endsWith(DocFormats.JRXML_EXT)) {
            report = new TemplatedJasperIMObjectReport(template, service, handlers);
        } else {
            throw new ReportException(UnsupportedTemplate, name);
        }
        return report;
    }

    /**
     * Creates a new report for a collection of {@link IMObject}s.
     *
     * @param template the document template
     * @param service  the archetype service
     * @param handlers the document handlers
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMReport<IMObject> createIMObjectReport(Document template, IArchetypeService service,
                                                          DocumentHandlers handlers) {
        String name = template.getName();
        IMReport<IMObject> report;
        if (name.endsWith(DocFormats.JRXML_EXT)) {
            report = new TemplatedJasperIMObjectReport(template, service, handlers);
        } else if (name.endsWith(DocFormats.ODT_EXT)) {
            report = new OpenOfficeIMReport<IMObject>(template, handlers);
        } else if (name.endsWith(DocFormats.DOC_EXT)) {
            report = new MsWordIMReport<IMObject>(template, handlers);
        } else {
            throw new ReportException(UnsupportedTemplate, name);
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
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMReport<IMObject> createIMObjectReport(String shortName, IArchetypeService service,
                                                          DocumentHandlers handlers) {
        TemplateHelper helper = new TemplateHelper(service);
        Document doc = helper.getDocumentForArchetype(shortName);
        if (doc == null) {
            String displayName = DescriptorHelper.getDisplayName(shortName, service);
            throw new ReportException(NoTemplateForArchetype, displayName);
        }
        return createIMObjectReport(doc, service, handlers);
    }

    /**
     * Creates a new report for a collection of {@link ObjectSet}s.
     *
     * @param shortName the archetype short name
     * @param service   the archetype service
     * @param handlers  the document handlers
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMReport<ObjectSet> createObjectSetReport(String shortName, IArchetypeService service,
                                                            DocumentHandlers handlers) {
        TemplateHelper helper = new TemplateHelper(service);
        Document doc = helper.getDocumentForArchetype(shortName);
        if (doc == null) {
            String displayName = DescriptorHelper.getDisplayName(shortName, service);
            throw new ReportException(NoTemplateForArchetype, displayName);
        }
        return createObjectSetReport(doc, service, handlers);
    }

    /**
     * Creates a new report for a collection of {@link ObjectSet}s.
     *
     * @param template the document template
     * @param service  the archetype service
     * @param handlers the document handlers
     * @return a new report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMReport<ObjectSet> createObjectSetReport(
            Document template, IArchetypeService service,
            DocumentHandlers handlers) {
        String name = template.getName();
        IMReport<ObjectSet> report;
        if (name.endsWith(DocFormats.JRXML_EXT)) {
            report = new TemplatedJasperObjectSetReport(template, service, handlers);
        } else if (name.endsWith(DocFormats.ODT_EXT)) {
            report = new OpenOfficeIMReport<ObjectSet>(template, handlers);
        } else {
            throw new ReportException(UnsupportedTemplate, name);
        }
        return report;
    }

}
