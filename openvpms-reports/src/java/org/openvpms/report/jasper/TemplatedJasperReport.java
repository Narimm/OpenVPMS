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

package org.openvpms.report.jasper;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.IMObjectReportException;
import static org.openvpms.report.IMObjectReportException.ErrorCode.FailedToGenerateReport;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


/**
 * A {@link JasperIMObjectReport} that uses pre-defined templates.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TemplatedJasperReport extends AbstractJasperReport
        implements JasperIMObjectReport {

    /**
     * The template loader.
     */
    private TemplateLoader template;


    /**
     * Constructs a new <code>TemplatedJasperReport</code>.
     *
     * @param template  the document template
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @param service   the archetype service
     * @throws IMObjectReportException if the report cannot be created
     */
    public TemplatedJasperReport(Document template, String[] mimeTypes,
                                 IArchetypeService service) {
        super(mimeTypes, service);
        this.template = new TemplateLoader(template, service);
    }

    /**
     * Constructs a new <code>TemplatedJasperReport</code>.
     *
     * @param design    the master report design
     * @param mimeTypes a list of mime-types, used to select the preferred
     *                  output format of the report
     * @param service   the archetype service
     * @throws IMObjectReportException if the report cannot be created
     */
    public TemplatedJasperReport(JasperDesign design, String[] mimeTypes,
                                 IArchetypeService service) {
        super(mimeTypes, service);
        this.template = new TemplateLoader(design, service);
    }

    /**
     * Returns the master report.
     *
     * @return the master report
     */
    public JasperReport getReport() {
        return template.getReport();
    }

    /**
     * Returns the sub-reports.
     *
     * @return the sub-reports.
     */
    public JasperReport[] getSubreports() {
        return template.getSubreports();
    }

    /**
     * Generates a report for an object.
     *
     * @param objects
     * @return a document containing the report
     * @throws IMObjectReportException   for any error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Document generate(Collection<IMObject> objects) {
        Document document;
        try {
            JasperPrint print = report(objects);
            document = convert(print);
        } catch (JRException exception) {
            throw new IMObjectReportException(exception, FailedToGenerateReport,
                                              exception.getMessage());
        }
        return document;
    }

    /**
     * Generates a report for an object.
     *
     * @param objects
     * @return the report
     * @throws JRException for any error
     */
    public JasperPrint report(Collection<IMObject> objects) throws JRException {
        IMObjectCollectionDataSource source
                = new IMObjectCollectionDataSource(objects,
                                                   getArchetypeService());
        HashMap<String, Object> properties
                = new HashMap<String, Object>(getParameters());
        properties.put("dataSource", source);
        return JasperFillManager.fillReport(template.getReport(), properties,
                                            source);
    }

    /**
     * Returns the report parameters to use when filling the report.
     *
     * @return the report parameters
     */
    protected Map<String, Object> getParameters() {
        Map<String, Object> result = super.getParameters();
        result.putAll(template.getParameters());
        return result;
    }

}
