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

import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignSubreport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.IMObjectReportException;
import static org.openvpms.report.IMObjectReportException.ErrorCode.FailedToCreateReport;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Helper for loading and compiling jasper report templates.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TemplateLoader {

    /**
     * The compiled report.
     */
    private JasperReport report;

    /**
     * The sub-reports.
     */
    private final List<JasperReport> subreports
            = new ArrayList<JasperReport>();
    /**
     * Report parameters.
     */
    private final Map<String, Object> parameters
            = new HashMap<String, Object>();

    /**
     * Constructs a new <code>TemplateLoader</code>.
     *
     * @param template the document template
     * @param service  the archetype service
     * @throws IMObjectReportException if the report cannot be created
     */
    public TemplateLoader(Document template, IArchetypeService service) {
        try {
            ByteArrayInputStream stream
                    = new ByteArrayInputStream(template.getContents());
            JasperDesign report = JRXmlLoader.load(stream);
            init(report, service);
        } catch (JRException exception) {
            throw new IMObjectReportException(exception, FailedToCreateReport,
                                              exception.getMessage());
        }
    }

    /**
     * Constructs a new <code>TemplateLoader</code>.
     *
     * @param design  the master report design
     * @param service the archetype service
     * @throws IMObjectReportException if the report cannot be created
     */
    public TemplateLoader(JasperDesign design, IArchetypeService service) {
        init(design, service);
    }

    /**
     * Returns the master report.
     *
     * @return the master report
     */
    public JasperReport getReport() {
        return report;
    }

    /**
     * Returns the sub-reports.
     *
     * @return the sub-reports.
     */
    public JasperReport[] getSubreports() {
        return subreports.toArray(new JasperReport[0]);
    }

    /**
     * Returns the report parameters to use when filling the report.
     *
     * @return the report parameters
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Initialises the report.
     *
     * @param design  the report design
     * @param service the archetype service
     * @throws IMObjectReportException if the report cannot be initialised
     */
    protected void init(JasperDesign design, IArchetypeService service) {
        try {
            JRElement[] elements = design.getDetail().getElements();
            for (JRElement element : elements) {
                if (element instanceof JRDesignSubreport) {
                    JRDesignSubreport subreport = (JRDesignSubreport) element;
                    String reportName = getReportName(subreport);
                    JasperDesign report = JasperReportHelper.getReport(
                            reportName, service);
                    if (report == null) {
                        throw new IMObjectReportException(
                                FailedToCreateReport,
                                "Failed to find subreport with name: "
                                        + reportName);
                    }

                    // replace the original expression with a parameter
                    JRDesignExpression expression = new JRDesignExpression();
                    expression.setText("$P{" + reportName + "}");
                    expression.setValueClass(JasperReport.class);
                    subreport.setExpression(expression);

                    JasperReport compiled
                            = JasperCompileManager.compileReport(report);
                    subreports.add(compiled);
                    parameters.put(reportName, compiled);

                    JRDesignParameter param = new JRDesignParameter();
                    param.setName(reportName);
                    param.setValueClass(JasperReport.class);
                    design.addParameter(param);
                }
            }
            report = JasperCompileManager.compileReport(design);
        } catch (JRException exception) {
            throw new IMObjectReportException(exception,
                                              FailedToCreateReport,
                                              exception.getMessage());
        }
    }

    /**
     * Returns the subreport name from a subreport.
     *
     * @param subreport the subreport
     * @return the subreport name
     */
    private String getReportName(JRDesignSubreport subreport) {
        String name = subreport.getExpression().getText();
        name = StringUtils.strip(name, " \"");
        return name;
    }
}
