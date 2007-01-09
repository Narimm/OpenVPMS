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

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DocumentException;
import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.NotFound;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMObjectReport;
import org.openvpms.report.IMObjectReportException;
import org.openvpms.report.IMObjectReportFactory;
import org.openvpms.report.TemplateHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.system.ServiceHelper;

import java.util.Arrays;


/**
 * Helper to generate reports from a template.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ReportGenerator {

    /**
     * An <em>entity.documentTemplate</em>/
     */
    private final Entity template;


    /**
     * Constructs a new <code>ReportGenerator</code>.
     *
     * @param act the document act. Must have an associated
     *            <em>participation.documentTemplate</em>
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template can't be found
     */
    public ReportGenerator(DocumentAct act) {
        ActBean bean = new ActBean(act);
        template = bean.getParticipant("participation.documentTemplate");
        if (template == null) {
            throw new DocumentException(NotFound);
        }
    }

    /**
     * Constructs a new <code>ReportGenerator</code>.
     *
     * @param template a reference to an <em>entity.documentTemplate</em>
     * @throws DocumentException if the template can't be found
     */
    public ReportGenerator(IMObjectReference template) {
        this.template = (Entity) IMObjectHelper.getObject(template);
        if (template == null) {
            throw new DocumentException(NotFound);
        }
    }

    /**
     * Constructs a new <code>ReportGenerator</code>.
     *
     * @param template the <em>entity.documentTemplate</em>
     */
    public ReportGenerator(Entity template) {
        this.template = template;
    }

    /**
     * Returns the template.
     *
     * @return an <em>entity.documentTemplate</em>
     */
    public Entity getTemplate() {
        return template;
    }

    /**
     * Generates a report.
     *
     * @param object the object to generate the report for
     * @return the generated report
     * @throws DocumentException         if the template document can't be found
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document generate(IMObject object) {
        String[] mimeTypes = {DocFormats.ODT_TYPE, DocFormats.PDF_TYPE};
        return generate(object, mimeTypes);
    }

    /**
     * Generates a report.
     *
     * @param object   the object to generate the report for
     * @param mimeType the mime type of the report.
     * @return the generated report
     * @throws DocumentException         if the template document can't be found
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document generate(IMObject object, String mimeType) {
        return generate(object, new String[]{mimeType});
    }

    /**
     * Creates a report.
     *
     * @return a new report
     * @throws DocumentException         if the template document can't be found
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMObjectReport createReport() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        Document doc = TemplateHelper.getDocumentFromTemplate(template,
                                                              service);
        if (doc == null) {
            throw new DocumentException(NotFound);
        }
        DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
        return IMObjectReportFactory.create(doc, service, handlers);
    }

    /**
     * Generates a report.
     *
     * @param object    the object to generate the report for
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @return the generated report
     * @throws DocumentException         if the template document can't be found
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Document generate(IMObject object, String[] mimeTypes) {
        IMObjectReport report = createReport();
        return report.generate(Arrays.asList(object), mimeTypes);
    }

}
