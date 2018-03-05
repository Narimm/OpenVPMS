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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.report;

import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportException;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.component.im.doc.FileNameFormatter;
import org.openvpms.web.system.ServiceHelper;


/**
 * Generates {@link Document}s from a {@link DocumentAct}.
 *
 * @author Tim Anderson
 */
public class DocumentActReporter extends TemplatedReporter<IMObject> {

    /**
     * The report.
     */
    private IMReport<IMObject> report;

    /**
     * Constructs a {@link DocumentActReporter}.
     *
     * @param act       the document act
     * @param formatter the file name formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template cannot be found
     */
    public DocumentActReporter(DocumentAct act, FileNameFormatter formatter, IArchetypeService service,
                               ILookupService lookups) {
        this(act, getTemplate(act, true, service), formatter, service, lookups);
    }

    /**
     * Constructs a {@link DocumentActReporter}.
     *
     * @param act       the document act
     * @param template  the document template
     * @param formatter the file name formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @throws ArchetypeServiceException for any archetype service error
     */
    public DocumentActReporter(DocumentAct act, DocumentTemplate template, FileNameFormatter formatter,
                               IArchetypeService service, ILookupService lookups) {
        super(act, template, formatter, service, lookups);
    }

    /**
     * Constructs a {@link DocumentActReporter}.
     *
     * @param act       the document act
     * @param locator   the template locator, if a template isn't associated with the act
     * @param formatter the file name formatter  @throws ArchetypeServiceException for any archetype service error
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @throws DocumentException if the template cannot be found
     */
    public DocumentActReporter(DocumentAct act, DocumentTemplateLocator locator, FileNameFormatter formatter,
                               IArchetypeService service, ILookupService lookups) {
        super(act, locator, formatter, service, lookups);
        setTemplate(getTemplate(act, false, service));
    }

    /**
     * Returns the report.
     *
     * @return the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template cannot be found
     */
    @Override
    public IMReport<IMObject> getReport() {
        if (report == null) {
            Document doc = getTemplateDocument();
            ReportFactory factory = ServiceHelper.getBean(ReportFactory.class);
            report = factory.createIMObjectReport(doc);
        }
        return report;
    }

    /**
     * Helper to determine if a document act has a template.
     * <p>
     * Document acts must have a template to be able to be used in reporting.
     *
     * @param act     the document act
     * @param service the archetype service
     * @return {@code true} if the act has a template, otherwise {@code false}
     */
    public static boolean hasTemplate(DocumentAct act, IArchetypeService service) {
        ActBean bean = new ActBean(act, service);
        return bean.getParticipantRef(DocumentArchetypes.DOCUMENT_TEMPLATE_PARTICIPATION) != null;
    }

    /**
     * Helper to return the <em>entity.documentTemplate</em> associated with a document act.
     *
     * @param act      the document act
     * @param required if {@code true} the template is required, {@code false} if it is optional
     * @param service  the archetype service
     * @return the associated template, or {@code null} if it is not found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template cannot be found, and the template is required
     */
    private static DocumentTemplate getTemplate(DocumentAct act, boolean required, IArchetypeService service) {
        ActBean bean = new ActBean(act, service);
        Entity template = bean.getParticipant(DocumentArchetypes.DOCUMENT_TEMPLATE_PARTICIPATION);
        if (template == null && required) {
            throw new DocumentException(DocumentException.ErrorCode.DocumentHasNoTemplate);
        }
        return (template != null) ? new DocumentTemplate(template, service) : null;
    }

}
