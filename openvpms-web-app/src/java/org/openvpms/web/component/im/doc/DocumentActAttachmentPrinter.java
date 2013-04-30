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
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.print.PrintException;
import org.openvpms.web.component.im.print.TemplatedIMPrinter;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.im.report.TemplatedReporter;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * A printer for attachments associated with {@link DocumentAct}s.
 * If a document template (<em>entity.documentTemplate</em>) is associated with
 * the act, then this will be used to generate the document to print.
 * Otherwise, any {@link Document} associated with the act will be printed
 * directly.
 *
 * @author Tim Anderson
 */
public class DocumentActAttachmentPrinter extends TemplatedIMPrinter<IMObject> {

    /**
     * Constructs a {@code DocumentActAttachmentPrinter}.
     *
     * @param object  the object to print
     * @param locator the document template locator
     * @param context the context
     * @throws ArchetypeServiceException for any archetype service error
     */
    @SuppressWarnings("unchecked")
    public DocumentActAttachmentPrinter(DocumentAct object, DocumentTemplateLocator locator, Context context) {
        super(ReporterFactory.<IMObject, TemplatedReporter<IMObject>>create(object, locator, TemplatedReporter.class),
              context);
    }

    /**
     * Prints the object.
     *
     * @param printer the printer name. May be {@code null}
     * @throws PrintException    if {@code printer} is null and
     *                           {@link #getDefaultPrinter()} also returns
     *                           {@code null}
     * @throws OpenVPMSException for any error
     */
    @Override
    public void print(String printer) {
        if (printer == null) {
            printer = getDefaultPrinter();
        }
        if (printer == null) {
            throw new PrintException(PrintException.ErrorCode.NoPrinter);
        }
        print(getDocument(), printer);
    }

    /**
     * Returns a document corresponding to that which would be printed.
     * If a document template is associated with the {@link DocumentAct}
     * archetype, then this will be used to generate the document.
     * Otherwise, any {@link Document} associated with the {@link DocumentAct}
     * will be used.
     *
     * @return a document
     * @throws OpenVPMSException for any error
     */
    @Override
    public Document getDocument() {
        return getDocument(null, false);
    }

    /**
     * Returns a document corresponding to that which would be printed.
     *
     * @param mimeType the mime type. If {@code null} the default mime type associated with the report will be used.
     * @param email    if {@code true} indicates that the document will be emailed. Documents generated from templates
     *                 can perform custom formatting
     * @return a document      `
     * @throws OpenVPMSException for any error
     */
    @Override
    public Document getDocument(String mimeType, boolean email) {
        Document template = getReporter().getTemplateDocument();
        Document result = null;
        if (template == null) {
            DocumentAct act = (DocumentAct) getObject();
            result = (Document) IMObjectHelper.getObject(act.getDocument(), getContext());
            if (result != null && mimeType != null && !mimeType.equals(result.getMimeType())) {
                result = DocumentHelper.convert(result, mimeType);
            }
        }
        if (result == null) {
            result = super.getDocument(mimeType, email);
        }
        return result;
    }
}
