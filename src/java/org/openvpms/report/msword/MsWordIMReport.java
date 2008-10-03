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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.report.msword;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.report.openoffice.OOConnection;
import org.openvpms.report.openoffice.OpenOfficeDocument;
import org.openvpms.report.openoffice.OpenOfficeException;
import org.openvpms.report.openoffice.OpenOfficeIMReport;


/**
 * Generates a report using an MS Word document as the template.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 */
public class MsWordIMReport<T> extends OpenOfficeIMReport<T> {

    /**
     * Creates a new <tt>MsWordIMReport</tt>.
     *
     * @param template the document template
     * @param handlers the document handlers
     */
    public MsWordIMReport(Document template, DocumentHandlers handlers) {
        super(template, handlers);
    }

    /**
     * Creates a new document.
     *
     * @param template   the document template
     * @param connection the connection to the OpenOffice service
     * @param handlers   the document handlers
     * @return a new document
     * @throws OpenOfficeException for any error
     */
    @Override
    protected OpenOfficeDocument createDocument(Document template,
                                                OOConnection connection,
                                                DocumentHandlers handlers) {
        return new MsWordDocument(template, connection, handlers);
    }

}
