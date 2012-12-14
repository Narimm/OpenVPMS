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
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.print.PrintException;
import org.openvpms.web.component.im.print.TemplatedIMPrinter;
import org.openvpms.web.component.im.report.DocumentTemplateLocator;
import org.openvpms.web.component.im.report.ReporterFactory;
import org.openvpms.web.component.im.report.TemplatedReporter;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * A printer for {@link DocumentAct}s.
 *
 * @author Tim Anderson
 */
public class DocumentActPrinter extends TemplatedIMPrinter<IMObject> {

    /**
     * Constructs a {@code DocumentActPrinter}.
     *
     * @param object  the object to print
     * @param locator the document template locator
     */
    public DocumentActPrinter(DocumentAct object, DocumentTemplateLocator locator) {
        super(ReporterFactory.<IMObject, TemplatedReporter<IMObject>>create(object, locator, TemplatedReporter.class));
    }

    /**
     * Prints the object.
     *
     * @param printer the printer name. May be {@code null}
     * @throws PrintException    if {@code printer} is null and {@link #getDefaultPrinter()} also returns {@code null}
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
        DocumentAct act = (DocumentAct) getObject();
        Document doc = (Document) IMObjectHelper.getObject(act.getDocument());
        if (doc == null) {
            super.print(printer);
        } else {
            print(doc, printer);
        }
    }

    /**
     * Returns a document corresponding to that which would be printed.
     *
     * @return a document
     * @throws org.openvpms.component.system.common.exception.OpenVPMSException
     *          for any error
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
     * @return a document
     * @throws OpenVPMSException for any error
     */
    @Override
    public Document getDocument(String mimeType, boolean email) {
        DocumentAct act = (DocumentAct) getObject();
        Document result = (Document) IMObjectHelper.getObject(act.getDocument());
        if (result != null && mimeType != null && !mimeType.equals(result.getMimeType())) {
            result = DocumentHelper.convert(result, mimeType);
        }
        if (result == null) {
            result = super.getDocument(mimeType, email);
        }
        return result;
    }
}
