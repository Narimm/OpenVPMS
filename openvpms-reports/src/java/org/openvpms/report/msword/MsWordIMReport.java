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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.report.msword;

import org.apache.commons.jxpath.Functions;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.report.openoffice.OOConnection;
import org.openvpms.report.openoffice.OpenOfficeDocument;
import org.openvpms.report.openoffice.OpenOfficeException;
import org.openvpms.report.openoffice.OpenOfficeIMReport;


/**
 * Generates a report using an MS Word document as the template.
 *
 * @author Tim Anderson
 */
public class MsWordIMReport<T> extends OpenOfficeIMReport<T> {

    /**
     * Constructs an {@link MsWordIMReport}.
     *
     * @param template  the document template
     * @param service   the archetype service
     * @param lookups   the lookup service
     * @param handlers  the document handlers
     * @param functions the JXPath extension functions
     */
    public MsWordIMReport(Document template, IArchetypeService service, ILookupService lookups,
                          DocumentHandlers handlers, Functions functions) {
        super(template, service, lookups, handlers, functions);
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
    protected OpenOfficeDocument createDocument(Document template, OOConnection connection, DocumentHandlers handlers) {
        return new MsWordDocument(template, connection, handlers);
    }

}
