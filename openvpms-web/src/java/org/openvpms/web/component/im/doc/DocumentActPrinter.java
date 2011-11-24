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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActPrinter extends TemplatedIMPrinter<IMObject> {

    /**
     * Constructs a <tt>DocumentActPrinter</tt>.
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
     * @param printer the printer name. May be <tt>null</tt>
     * @throws PrintException    if <tt>printer</tt> is null and {@link #getDefaultPrinter()} also returns <tt>null</tt>
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

}
