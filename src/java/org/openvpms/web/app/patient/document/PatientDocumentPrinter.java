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

package org.openvpms.web.app.patient.document;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.im.doc.DocumentException;
import static org.openvpms.web.component.im.doc.DocumentException.ErrorCode.NotFound;
import org.openvpms.web.component.im.print.AbstractIMObjectPrinter;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * Patient document printer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientDocumentPrinter extends AbstractIMObjectPrinter {

    /**
     * Constructs a new <code>PatientDocumentPrinter</code>.
     *
     * @param type display name for the types of objects that this may
     *             print
     */
    public PatientDocumentPrinter(String type) {
        super(type);
    }

    /**
     * Returns a document for an object.
     *
     * @param object the object
     * @return a document
     * @throws DocumentException if the document cannot be found
     */
    protected Document getDocument(IMObject object) {
        DocumentAct act = (DocumentAct) object;
        Document doc = (Document) IMObjectHelper.getObject(
                act.getDocReference());
        if (doc == null) {
            throw new DocumentException(NotFound);
        }
        return doc;
    }

}
