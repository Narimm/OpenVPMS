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
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.IMReport;
import org.openvpms.report.ReportException;
import org.openvpms.report.ReportFactory;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.system.ServiceHelper;


/**
 * Generates {@link Document}s from a {@link DocumentAct}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActReporter extends TemplatedReporter<IMObject> {

    /**
     * Creates a new <tt>DocumentActReporter</tt>.
     *
     * @param act the document act
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template cannot be found
     */
    public DocumentActReporter(DocumentAct act) {
        super(act, act.getArchetypeId().getShortName(), getTemplate(act));
    }

    /**
     * Creates a new <tt>DocumentActReporter</tt>.
     *
     * @param act      the document act
     * @param template the document template
     * @throws ArchetypeServiceException for any archetype service error
     */
    public DocumentActReporter(DocumentAct act, DocumentTemplate template) {
        super(act, act.getArchetypeId().getShortName(), template);
    }

    /**
     * Returns the report.
     *
     * @return the report
     * @throws ReportException           for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public IMReport<IMObject> getReport() {
        Document doc = getTemplateDocument();
        if (doc == null) {
            throw new DocumentException(NotFound);
        }
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
        return ReportFactory.createIMObjectReport(doc, service, handlers);
    }

    /**
     * Creates the document.
     *
     * @throws OpenVPMSException for any error
     */
    @Override
    public Document getDocument() {
        DocumentAct object = (DocumentAct) getObject();
        Document doc = (Document) IMObjectHelper.getObject(
                object.getDocument());
        if (doc == null) {
            doc = super.getDocument();
        }
        return doc;
    }

    /**
     * Helper to return the <em>entity.documentTemplate</em> associated with
     * a document act.
     *
     * @param act the document act
     * @return the associated entity
     * @throws ArchetypeServiceException for any archetype service error
     * @throws DocumentException         if the template cannot be found
     */
    private static DocumentTemplate getTemplate(DocumentAct act) {
        ActBean bean = new ActBean(act);
        Entity template = bean.getParticipant("participation.documentTemplate");
        if (template == null) {
            throw new DocumentException(NotFound);
        }
        return new DocumentTemplate(template, ServiceHelper.getArchetypeService());
    }

}
