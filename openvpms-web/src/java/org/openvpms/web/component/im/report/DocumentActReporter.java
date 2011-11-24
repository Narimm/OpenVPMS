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

package org.openvpms.web.component.im.report;

import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentException;
import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.NotFound;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.report.IMReport;
import org.openvpms.report.ParameterType;
import org.openvpms.report.ReportException;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.Set;


/**
 * Generates {@link Document}s from a {@link DocumentAct}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActReporter extends TemplatedReporter<IMObject> {

    /**
     * The report.
     */
    private IMReport<IMObject> report;

    /**
     * Constructs a <tt>DocumentActReporter</tt>.
     *
     * @param act the document act
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template cannot be found
     */
    public DocumentActReporter(DocumentAct act) {
        this(act, getTemplate(act, true));
    }

    /**
     * Constructs a <tt>DocumentActReporter</tt>.
     *
     * @param act      the document act
     * @param template the document template
     * @throws ArchetypeServiceException for any archetype service error
     */
    public DocumentActReporter(DocumentAct act, DocumentTemplate template) {
        super(act, template);
    }

    /**
     * Constructs a <tt>DocumentActReporter</tt>.
     *
     * @param act     the document act
     * @param locator the template locator, if a template isn't associated with the act
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template cannot be found
     */
    public DocumentActReporter(DocumentAct act, DocumentTemplateLocator locator) {
        super(act, locator);
        setTemplate(getTemplate(act, false));
    }

    /**
     * Returns the report.
     *
     * @return the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template cannot be found
     */
    public IMReport<IMObject> getReport() {
        if (report == null) {
            Document doc = getTemplateDocument();
            if (doc == null) {
                throw new DocumentException(NotFound);
            }
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
            report = ReportFactory.createIMObjectReport(doc, service, handlers);
        }
        return report;
    }

    /**
     * Returns the set of parameter types that may be supplied to the report.
     *
     * @return the parameter types
     */
    public Set<ParameterType> getParameterTypes() {
        return getReport().getParameterTypes();
    }

    /**
     * Helper to determine if a document act has a template.
     * <p/>
     * Document acts must have a template to be able to be used in reporting.
     *
     * @param act the document act
     * @return <tt>true</tt> if the act has a template, otherwise <tt>false</tt>
     */
    public static boolean hasTemplate(DocumentAct act) {
        ActBean bean = new ActBean(act);
        return bean.getParticipantRef(DocumentArchetypes.DOCUMENT_TEMPLATE_PARTICIPATION) != null;
    }

    /**
     * Helper to return the <em>entity.documentTemplate</em> associated with a document act.
     *
     * @param act      the document act
     * @param required if <tt>true</tt> the template is required, <tt>false</tt> if it is optional
     * @return the associated template, or <tt>null</tt> if it is not found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template cannot be found, and the template is required
     */
    private static DocumentTemplate getTemplate(DocumentAct act, boolean required) {
        ActBean bean = new ActBean(act);
        Entity template = bean.getParticipant(DocumentArchetypes.DOCUMENT_TEMPLATE_PARTICIPATION);
        if (template == null && required) {
            throw new DocumentException(NotFound);
        }
        return (template != null) ? new DocumentTemplate(template, ServiceHelper.getArchetypeService()) : null;
    }

}
