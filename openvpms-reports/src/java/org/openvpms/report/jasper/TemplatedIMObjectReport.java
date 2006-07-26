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
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * {@link IMObjectReport} that uses pre-defined templates.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class TemplatedIMObjectReport extends AbstractIMObjectReport {

    /**
     * The compiled report.
     */
    private final JasperReport _report;

    /**
     * The sub-reports.
     */
    private final List<JasperReport> _subreports
            = new ArrayList<JasperReport>();

    /**
     * Report parameters.
     */
    private final Map<String, Object> _parameters
            = new HashMap<String, Object>();


    /**
     * Constructs a new <code>TemplatedIMObjectReport</code>.
     *
     * @param design  the master report design
     * @param service the archetype service
     * @throws JRException for any error
     */
    public TemplatedIMObjectReport(JasperDesign design,
                                   IArchetypeService service)
            throws JRException {
        super(service);
        JRElement[] elements = design.getDetail().getElements();
        for (JRElement element : elements) {
            if (element instanceof JRDesignSubreport) {
                JRDesignSubreport subreport = (JRDesignSubreport) element;
                String reportName = getReportName(subreport);
                JasperDesign report = TemplateHelper.getReport(reportName,
                                                               service);
                if (report == null) {
                    throw new JRException("Failed to find subreport with name: "
                            + reportName);
                }

                // replace the original expression with a parameter
                JRDesignExpression expression = new JRDesignExpression();
                expression.setText("$P{" + reportName + "}");
                expression.setValueClass(JasperReport.class);
                subreport.setExpression(expression);

                JasperReport compiled
                        = JasperCompileManager.compileReport(report);
                _subreports.add(compiled);
                _parameters.put(reportName, compiled);

                JRDesignParameter param = new JRDesignParameter();
                param.setName(reportName);
                param.setValueClass(JasperReport.class);
                design.addParameter(param);
            }
        }
        _report = JasperCompileManager.compileReport(design);
    }

    /**
     * Returns the master report.
     *
     * @return the master report
     */
    public JasperReport getReport() {
        return _report;
    }

    /**
     * Returns the sub-reports.
     *
     * @return the sub-reports.
     */
    public JasperReport[] getSubreports() {
        return _subreports.toArray(new JasperReport[0]);
    }

    /**
     * Returns the report parameters to use when filling the report.
     *
     * @param object the object to report on
     * @return the report parameters
     */
    protected Map<String, Object> getParameters(IMObject object) {
        Map<String, Object> result = super.getParameters(object);
        result.putAll(_parameters);
        return result;
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
