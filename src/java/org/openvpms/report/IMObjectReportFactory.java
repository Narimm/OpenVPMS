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

import static org.openvpms.report.IMObjectReportException.ErrorCode.FailedToCreateReport;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.jasper.DynamicJasperIMObjectReport;
import org.openvpms.report.jasper.TemplateHelper;
import org.openvpms.report.jasper.TemplatedJasperIMObjectReport;
import org.openvpms.report.openoffice.OpenOfficeIMObjectReport;

import java.io.ByteArrayInputStream;


/**
 * A factory for {@link IMObjectReport} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectReportFactory {

    /**
     * The logger.
     */
    private static final Log _log
            = LogFactory.getLog(IMObjectReportFactory.class);


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
        try {
            if (doc != null) {
                String name = doc.getName();
                if (name.endsWith(DocFormats.JRXML_EXT)) {
                    ByteArrayInputStream stream
                            = new ByteArrayInputStream(doc.getContents());
                    JasperDesign report = JRXmlLoader.load(stream);
                    return new TemplatedJasperIMObjectReport(report, mimeTypes,
                                                             service);
                } else if (name.endsWith(DocFormats.ODT_EXT)) {
                    return new OpenOfficeIMObjectReport(doc, mimeTypes);
                } else {
                    _log.error("Unrecognised document template': " + name
                            + "'. Falling back to dynamic jasper report");
                }
            }
            return new DynamicJasperIMObjectReport(
                    service.getArchetypeDescriptor(shortName), mimeTypes,
                    service);
        } catch (JRException exception) {
            throw new IMObjectReportException(exception, FailedToCreateReport,
                                              exception.getMessage());
        }
    }
}
