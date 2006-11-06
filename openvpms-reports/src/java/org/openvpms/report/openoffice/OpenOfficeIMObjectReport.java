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

package org.openvpms.report.openoffice;

import static org.openvpms.report.IMObjectReportException.ErrorCode.FailedToGenerateReport;
import org.apache.commons.io.FilenameUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.DocFormats;
import org.openvpms.report.ExpressionEvaluator;
import org.openvpms.report.IMObjectReport;
import org.openvpms.report.IMObjectReportException;
import static org.openvpms.report.IMObjectReportException.ErrorCode.UnsupportedMimeTypes;

import java.util.List;
import java.util.Collection;


/**
 * Generates a report for an <code>IMObject</code>, using an OpenOffice document
 * as the template.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class OpenOfficeIMObjectReport implements IMObjectReport {

    /**
     * The document template.
     */
    private final Document _template;

    /**
     * The mime-type to generate the document as.
     */
    private String _mimeType;


    /**
     * Creates a new <code>OpenOfficeIMObjectReport</code>.
     *
     * @param template  the document template
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @throws IMObjectReportException if the mime-type is invalid
     */
    public OpenOfficeIMObjectReport(Document template, String[] mimeTypes) {
        _template = template;
        for (String mimeType : mimeTypes) {
            if (DocFormats.ODT_TYPE.equals(mimeType)
                    || DocFormats.PDF_TYPE.equals(mimeType)) {
                _mimeType = mimeType;
                break;
            }
        }
        if (_mimeType == null) {
            throw new IMObjectReportException(UnsupportedMimeTypes);
        }
    }

    /**
     * Generates a report for a collection of objects.
     *
     * @param objects the objects to report on
     * @return a document containing the report
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document generate(Collection<IMObject> objects) {
        if (objects.size() != 1) {
          throw new IMObjectReportException(
                  FailedToGenerateReport,
                  "Can only report on single objects");
        }
        IMObject object = objects.toArray(new IMObject[0])[0];
        ExpressionEvaluator eval = new ExpressionEvaluator(
                object, ArchetypeServiceHelper.getArchetypeService());

        OpenOfficeDocument doc = null;
        try {
            doc = new OpenOfficeDocument(_template,
                                         OpenOfficeHelper.getService());
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
            return export(doc);
        } finally {
            if (doc != null) {
                doc.close();
            }
        }
    }

    /**
     * Exports a document, serializing to a {@link Document}.
     *
     * @param doc the document to export
     * @return a new document
     * @throws OpenOfficeException       for any error
     * @throws ArchetypeServiceException for any archetype service error
     */
    private Document export(OpenOfficeDocument doc) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        String name = _template.getName();
        if (!_mimeType.equals(DocFormats.ODT_TYPE)) {
            name = FilenameUtils.removeExtension(name);
        }
        return doc.export(_mimeType, name, service);
    }
}
