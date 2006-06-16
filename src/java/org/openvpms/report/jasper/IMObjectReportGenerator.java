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
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExpression;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignSubreport;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.HashMap;
import java.util.Map;


/**
 * Generates a jasper report for an <code>IMObject</code>.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectReportGenerator {

    /**
     * The archetype descriptor.
     */
    private final ArchetypeDescriptor _archetype;

    /**
     * The archetype service.
     */
    private final IArchetypeService _service;

    /**
     * Design time report.
     */
    private final JasperDesign _design;

    /**
     * Detail template.
     */
    private JRDesignBand _detail;


    private JasperReport _report;

    /**
     * Subreports, keyed on name.
     */
    private Map<String, JasperReport> _subreports
            = new HashMap<String, JasperReport>();


    /**
     * Construct a new <code>IMObjectReportGenerator</code>.
     *
     * @param archetype the archetype descriptor
     * @param service   the archetype service
     */
    public IMObjectReportGenerator(ArchetypeDescriptor archetype,
                                   IArchetypeService service)
            throws JRException {
        _design = JRXmlLoader.load("src/reports/archetype_template.jrxml");
        _detail = (JRDesignBand) _design.getDetail();
        _archetype = archetype;
        _service = service;

        JRDesignParameter param = new JRDesignParameter();
        param.setName("dataSource");
        param.setValueClass(IMObjectDataSource.class);
        _design.addParameter(param);
        _design.addImport(IMObjectDataSource.class.getName());

        JRDesignBand detail = new JRDesignBand();

        JRDesignField displayName = new JRDesignField();
        displayName.setName("displayName");
        displayName.setValueClass(String.class);
        _design.addField(displayName);

        int y = 0;
        for (NodeDescriptor node : _archetype.getSimpleNodeDescriptors()) {
            if (node.isHidden()) {
                continue;
            }
            JRDesignField field = new JRDesignField();
            field.setName(node.getName());
            field.setValueClass(node.getClazz());
            _design.addField(field);

            JRDesignStaticText label = createStaticText(node.getDisplayName());
            label.setY(y);
            JRDesignTextField textField = createText();
            textField.setY(y);
            JRDesignExpression expression = new JRDesignExpression();
            expression.setValueClass(node.getClazz());
            expression.setText("$F{" + node.getName() + "}");
            textField.setExpression(expression);

            detail.addElement(label);
            detail.addElement(textField);
            y += 20;
        }

        for (NodeDescriptor node : _archetype.getComplexNodeDescriptors()) {
            JRDesignSubreport report = getSubreport(node);
            detail.addElement(report);
            report.setY(y);
            y += report.getHeight();
        }
        detail.setHeight(y + 20);
        _design.setDetail(detail);
        _report = JasperCompileManager.compileReport(_design);
    }

    /**
     * Generates a report for an object.
     *
     * @param object the object
     * @return the report
     * @throws JRException for any error
     */
    public JasperPrint generate(IMObject object) throws JRException {
        HashMap properties = new HashMap(_subreports);
        IMObjectDataSource source = new IMObjectDataSource(object, _service);
        properties.put("dataSource", source);
        JasperPrint print = JasperFillManager.fillReport(
                _report, properties, source);
        return print;
    }

    public void report(IMObject object) throws JRException {
        HashMap properties = new HashMap(_subreports);
        IMObjectDataSource source = new IMObjectDataSource(object, _service);
        properties.put("dataSource", source);
        JasperPrint print = JasperFillManager.fillReport(
                _report, properties, source);
        JasperViewer viewer = new JasperViewer(print, true);
        viewer.setVisible(true);
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
        JasperDesign design = JRXmlLoader.load(
                "src/reports/archetype_subreport_template.jrxml");
        String subreportName = node.getName() + "_subreport";
        JRDesignSubreport report = createSubreport();
        JRExpression dataSource = createExpression(
                "$P{dataSource}.getDataSource(\"" + node.getName() + "\")",
                JRDataSource.class);
        report.setDataSourceExpression(dataSource);
        JRExpression expression = createExpression(
                "$P{" + subreportName + "}", JasperReport.class);
        report.setExpression(expression);
        JRDesignParameter param = new JRDesignParameter();
        param.setName(subreportName);
        param.setValueClass(JasperReport.class);
        _design.addParameter(param);
        JasperReport compiled = JasperCompileManager.compileReport(design);
        _subreports.put(subreportName, compiled);
        return report;
    }

    /**
     * Helper to create a static text field.
     *
     * @param text the text
     * @return a new static text field
     */
    private JRDesignStaticText createStaticText(String text) {
        JRDesignStaticText result = new JRDesignStaticText();
        result.setText(text);

        JRDesignStaticText template = (JRDesignStaticText) getTemplate(
                JRDesignStaticText.class);
        if (template != null) {
            copy(template, result);
        }
        return result;
    }

    /**
     * Helper to create a text field.
     *
     * @return a new text field
     */
    private JRDesignTextField createText() {
        JRDesignTextField result = new JRDesignTextField();

        JRDesignTextField template = (JRDesignTextField) getTemplate(
                JRDesignTextField.class);
        if (template != null) {
            copy(template, result);
        }
        return result;
    }

    /**
     * Helper to create a sub report.
     *
     * @return a new sub report
     */
    private JRDesignSubreport createSubreport() {
        JRDesignSubreport report = new JRDesignSubreport(_design);
        JRDesignSubreport template
                = (JRDesignSubreport) getTemplate(JRDesignSubreport.class);
        if (template != null) {
            report.setForecolor(template.getForecolor());
            report.setBackcolor(template.getBackcolor());
            report.setHeight(template.getHeight());
            report.setKey(template.getKey());
            report.setMode(template.getMode());
            report.setPositionType(template.getPositionType());
            report.setX(template.getX());
            report.setY(template.getY());
            report.setStretchType(template.getStretchType());
            report.setRemoveLineWhenBlank(template.isRemoveLineWhenBlank());
            report.setPrintInFirstWholeBand(template.isPrintInFirstWholeBand());
            report.setPrintRepeatedValues(template.isPrintRepeatedValues());
            report.setPrintWhenDetailOverflows(
                    template.isPrintWhenDetailOverflows());
            report.setPrintWhenExpression(template.getPrintWhenExpression());
        }
        return report;
    }

    private JRElement getTemplate(Class clazz) {
        if (_detail != null) {
            for (JRElement element : _detail.getElements()) {
                if (element.getClass().isAssignableFrom(clazz)) {
                    return element;
                }
            }
        }
        return null;
    }


    /**
     * Helper to copy style.
     *
     * @param source
     * @param target
     */
    private void copy(JRDesignTextElement source, JRDesignTextElement target) {
        target.setHeight(source.getHeight());
        target.setWidth(source.getWidth());
        target.setX(source.getX());
        target.setY(source.getY());

        target.setForecolor(source.getForecolor());
        target.setBackcolor(source.getBackcolor());

        target.setFontName(source.getFontName());
        target.setFontSize(source.getFontSize());
        target.setBold(source.isBold());
        target.setHorizontalAlignment(source.getHorizontalAlignment());
        target.setVerticalAlignment(source.getVerticalAlignment());

        target.setBorder(source.getBorder());
        target.setTopBorder(source.getTopBorder());
        target.setTopBorderColor(source.getTopBorderColor());
        target.setBottomBorder(source.getBottomBorder());
        target.setBottomBorderColor(source.getBottomBorderColor());
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
        if (clazz != null) {
            result.setValueClass(clazz);
        }
        return result;
    }

}
