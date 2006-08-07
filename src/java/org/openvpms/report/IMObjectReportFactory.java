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

package org.openvpms.report;

import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import static org.openvpms.report.IMObjectReportException.ErrorCode.FailedToCreateReport;
import org.openvpms.report.jasper.TemplatedJasperIMObjectReport;
import org.openvpms.report.openoffice.OpenOfficeIMObjectReport;


/**
 * A factory for {@link IMObjectReport} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectReportFactory {

    /**
     * Creates a new report for a template.
     *
     * @param template  the document template
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @param service   the archetype service
     * @return a new report
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMObjectReport create(Document template, String[] mimeTypes,
                                        IArchetypeService service) {
        String name = template.getName();
        IMObjectReport report;
        if (name.endsWith(DocFormats.JRXML_EXT)) {
            report = new TemplatedJasperIMObjectReport(template, mimeTypes,
                                                       service);
        } else if (name.endsWith(DocFormats.ODT_EXT)) {
            report = new OpenOfficeIMObjectReport(template, mimeTypes);
        } else {
            throw new IMObjectReportException(
                    FailedToCreateReport,
                    "Unrecognised document template: '" + name + "'");
        }
        return report;
    }

    /**
     * Creates a new report.
     *
     * @param shortName the archetype short name
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @param service   the archetype service
     * @return a new report
     * @throws IMObjectReportException   for any report error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMObjectReport create(String shortName,
                                        String[] mimeTypes,
                                        IArchetypeService service) {
        Document doc = TemplateHelper.getDocumentForArchetype(shortName,
                                                              service);
        return create(doc, mimeTypes, service);
    }
}
