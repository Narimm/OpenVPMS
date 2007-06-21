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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignSubreport;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.report.ReportException;
import static org.openvpms.report.ReportException.ErrorCode.FailedToCreateReport;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Generates a jasper report for an <tt>IMObject</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DynamicJasperReport extends AbstractJasperIMReport<IMObject> {

    /**
     * Design time report.
     */
    private JasperDesign design;

    /**
     * Template helper.
     */
    private JRElementFactory template;

    /**
     * The compiled report.
     */
    private JasperReport report;

    /**
     * Subreports, keyed on name.
     */
    private Map<String, JasperReport> subreports
            = new HashMap<String, JasperReport>();


    /**
     * Constructs a new <tt>DynamicJasperReport</tt>.
     *
     * @param archetype the archetype descriptor
     * @param service   the archetype service
     * @param handlers  the document handlers
     * @throws ReportException if the report cannot be created
     */
    public DynamicJasperReport(ArchetypeDescriptor archetype,
                               IArchetypeService service,
                               DocumentHandlers handlers) {
        super(service, handlers);
        try {
            init(archetype);
        } catch (JRException exception) {
            throw new ReportException(exception, FailedToCreateReport,
                                      exception.getMessage());
        }
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
        return subreports.values().toArray(new JasperReport[0]);
    }

    /**
     * Creates a data source for a collection of objects.
     *
     * @param objects an iterator over the collection of objects
     * @return a new datas ource
     */
    protected JRDataSource createDataSource(Iterator<IMObject> objects) {
        return new IMObjectCollectionDataSource(objects, getArchetypeService());
    }

    /**
     * Returns the default report parameters to use when filling the report.
     *
     * @return the report parameters
     */
    protected Map<String, Object> getDefaultParameters() {
        Map<String, Object> result = super.getDefaultParameters();
        result.putAll(subreports);
        return result;
    }

    /**
     * Initialises the report.
     *
     * @param archetype the archetype descriptor
     * @throws JRException for any error
     */
    private void init(ArchetypeDescriptor archetype) throws JRException {
        design = JasperReportHelper.getReportResource(
                "/archetype_template.jrxml");
        template = new JRElementFactory(design);

        JRDesignParameter param = new JRDesignParameter();
        param.setName("dataSource");
        param.setValueClass(IMObjectCollectionDataSource.class);
        design.addParameter(param);
        design.addImport(IMObjectCollectionDataSource.class.getName());

        JRDesignBand detail = new JRDesignBand();

        int y = 0;
        for (NodeDescriptor node : archetype.getSimpleNodeDescriptors()) {
            if (node.isHidden()) {
                continue;
            }
            JRDesignField field = new JRDesignField();
            field.setName(node.getName());
            Class valueClass = JasperReportHelper.getValueClass(node);
            field.setValueClass(valueClass);
            design.addField(field);

            JRDesignStaticText label = template.createStaticText(
                    node.getDisplayName());
            label.setY(y);
            JRDesignTextField textField = template.createTextField();
            textField.setY(y);
            JRDesignExpression expression = new JRDesignExpression();
            expression.setValueClass(valueClass);
            expression.setText("$F{" + node.getName() + "}");
            textField.setExpression(expression);

            detail.addElement(label);
            detail.addElement(textField);
            y += 20;
        }

        if (!design.getFieldsMap().containsKey("displayName")) {
            JRDesignField displayName = new JRDesignField();
            displayName.setName("displayName");
            displayName.setValueClass(String.class);
            design.addField(displayName);
        }

        for (NodeDescriptor node : archetype.getComplexNodeDescriptors()) {
            JRDesignSubreport report = getSubreport(node);
            detail.addElement(report);
            report.setY(y);
            y += report.getHeight();
        }
        detail.setHeight(y + 20);
        design.setDetail(detail);

        report = JasperCompileManager.compileReport(design);
    }

    /**
     * Generates a subreport for a node.
     *
     * @param node the node
     * @return a subreport for the node
     * @throws JRException for any error
     */
    private JRDesignSubreport getSubreport(NodeDescriptor node)
            throws JRException {
        SubreportGenerator generator
                = SubreportGeneratorFactory.create(
                node, getArchetypeService());
        JasperDesign collectionReport = generator.generate();
        String subreportName = collectionReport.getName();
        JRDesignSubreport subreport = template.createSubreport();
        subreport.setHeight(collectionReport.getPageHeight());
        JRExpression dataSource = createExpression(
                "$P{dataSource}.getDataSource(\"" + node.getName() + "\")",
                JRDataSource.class);
        subreport.setDataSourceExpression(dataSource);
        JRExpression expression = createExpression(
                "$P{" + subreportName + "}", JasperReport.class);
        subreport.setExpression(expression);
        JRDesignParameter param = new JRDesignParameter();
        param.setName(subreportName);
        param.setValueClass(JasperReport.class);
        design.addParameter(param);
        JasperReport compiled = JasperCompileManager.compileReport(
                collectionReport);
        subreports.put(subreportName, compiled);
        return subreport;
    }

    /**
     * Helper to create an expression.
     *
     * @param text  the expression text
     * @param clazz the value class
     * @return a new expression
     */
    private JRExpression createExpression(String text, Class clazz) {
        JRDesignExpression result = new JRDesignExpression();
        result.setText(text);
        result.setValueClass(clazz);
        return result;
    }

}
