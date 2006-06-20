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

import net.sf.jasperreports.engine.JRBand;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRTextElement;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignElement;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignSubreport;
import net.sf.jasperreports.engine.design.JRDesignTextElement;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;


/**
 * Report template.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class JRElementFactory {

    /**
     * The template.
     */
    private JasperDesign _template;

    /**
     * The band to source field templates from.
     */
    private JRBand _band;


    /**
     * Construct a new <code>JRElementFactory</code>.
     *
     * @param template the report template
     */
    public JRElementFactory(JasperDesign template) {
        this(template, null);
    }

    /**
     * Construct a new <code>JRElementFactory</code>.
     *
     * @param template the report template
     * @param band     the band to source field templates from.
     *                 If <code>null</code> defaults to
     *                 <code>JasperDesign.getDetail()</code>
     */
    public JRElementFactory(JasperDesign template, JRBand band) {
        _template = template;
        _band = (band != null) ? band : _template.getDetail();
    }

    /**
     * Creates a column header.
     *
     * @return a new column header
     */
    public JRDesignBand createColumnHeader() {
        JRDesignBand header = new JRDesignBand();
        JRBand template = _template.getColumnHeader();
        if (template != null) {
            copy(template, header);
        }
        return header;
    }

    /**
     * Creates a new detail band.
     *
     * @return a new detail band.
     */
    public JRDesignBand createDetail() {
        JRDesignBand detail = new JRDesignBand();
        JRBand template = _template.getDetail();
        if (template != null) {
            copy(template, detail);
        }
        return detail;
    }

    /**
     * Creates a new sub report.
     *
     * @return a new sub report
     */
    public JRDesignSubreport createSubreport() {
        JRDesignSubreport report = new JRDesignSubreport(_template);
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

    /**
     * Creates a static text field.
     *
     * @param text the text
     * @return a new static text field
     */
    public JRDesignStaticText createStaticText(String text) {
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
    public JRDesignTextField createTextField() {
        JRDesignTextField result = new JRDesignTextField();

        JRDesignTextField template = (JRDesignTextField) getTemplate(
                JRDesignTextField.class);
        if (template != null) {
            copy(template, result);
        }
        return result;
    }

    /**
     * Returns an element of the specified class.
     *
     * @param clazz the class
     * @return an element with class <code>clazz</code> or <code>null</code> if
     *         none is found
     */
    private JRElement getTemplate(Class clazz) {
        if (_band != null) {
            for (JRElement element : _band.getElements()) {
                if (element.getClass().isAssignableFrom(clazz)) {
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * Helper to copy band style.
     *
     * @param source the source band
     * @param target the target band
     */
    private void copy(JRBand source, JRDesignBand target) {
        target.setHeight(source.getHeight());
        target.setSplitAllowed(source.isSplitAllowed());
        for (JRElement element : source.getElements()) {
            if (!(element instanceof JRTextElement) &&
                    (element instanceof JRDesignElement)) {
                target.addElement((JRDesignElement) element);
            }
        }
    }

    /**
     * Helper to copy text element style.
     *
     * @param source the source element
     * @param target the target element
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

}
