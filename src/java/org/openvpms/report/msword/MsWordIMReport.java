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

import static org.openvpms.report.ReportException.ErrorCode.FailedToGenerateReport;

import java.util.Iterator;
import java.util.List;

import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.ExpressionEvaluator;
import org.openvpms.report.ExpressionEvaluatorFactory;
import org.openvpms.report.ReportException;
import org.openvpms.report.openoffice.OOConnection;
import org.openvpms.report.openoffice.OpenOfficeDocument;
import org.openvpms.report.openoffice.OpenOfficeIMReport;

/**
 * @author tony
 *
 */
public class MsWordIMReport<T> extends OpenOfficeIMReport<T> {

    /**
     * Creates a new <code>MsWordIMReport</code>.
     *
     * @param template the document template
     * @param handlers the document handlers
     * @throws ReportException if the mime-type is invalid
     */
    public MsWordIMReport(Document template,
                              DocumentHandlers handlers) {
    	super(template,handlers);
    }

	@Override
	protected OpenOfficeDocument create(Iterator<T> objects, OOConnection connection) {
        MsWordDocument doc = null;
        T object = null;
        if (objects.hasNext()) {
            object = objects.next();
        }
        if (object == null || objects.hasNext()) {
            throw new ReportException(
                    FailedToGenerateReport,
                    "Can only report on single objects");
        }
        ExpressionEvaluator eval = ExpressionEvaluatorFactory.create(
                object, ArchetypeServiceHelper.getArchetypeService());

        try {
            doc = new MsWordDocument(template, connection, handlers);
            List<String> fieldNames = doc.getUserFieldNames();
            for (String name : fieldNames) {
                String value = doc.getUserField(name);
                if (value != null) {
                    value = eval.getFormattedValue(value);
                    doc.setUserField(name, value);
                }
            }
            // refresh the text fields
            doc.refresh();
        } catch (OpenVPMSException exception) {
            if (doc != null) {
                doc.close();
            }
            throw exception;
        }
        return doc;
	}
}
