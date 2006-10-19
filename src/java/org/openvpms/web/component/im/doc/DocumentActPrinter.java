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

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.report.DocFormats;
import org.openvpms.report.IMObjectReportException;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.im.print.AbstractIMObjectPrinter;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * {@link DocumentAct} printer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActPrinter extends AbstractIMObjectPrinter {

    /**
     * Initiates printing of an object.
     * If printing interactively, pops up a {@link ConfirmationDialog} prompting
     * if printing of an object should proceed, invoking {@link #doPrint}
     * if 'OK' is selected.
     * If printing in the background, invokes {@link #doPrint}.
     *
     * @param object the object to print
     */
    @Override
    public void print(final IMObject object) {
        if (isInteractive()) {
            String title = Messages.get("imobject.print.title",
                                        DescriptorHelper.getDisplayName(
                                                object));
            final ConfirmationDialog dialog = new ConfirmationDialog(title, "");
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    String action = dialog.getAction();
                    if (ConfirmationDialog.OK_ID.equals(action)) {
                        doPrint(object, null);
                    }
                }
            });
            dialog.show();
        } else {
            doPrint(object, null);
        }
    }

    /**
     * Returns a document for an object.
     *
     * @param object the object
     * @return a document
     * @throws DocumentException         if the document cannot be found
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected Document getDocument(IMObject object) {
        DocumentAct act = (DocumentAct) object;
        Document doc = (Document) IMObjectHelper.getObject(
                act.getDocReference());
        if (doc == null) {
            ReportGenerator gen = new ReportGenerator(act);
            doc = gen.generate(act, DocFormats.PDF_TYPE);
        }
        return doc;
    }

}
