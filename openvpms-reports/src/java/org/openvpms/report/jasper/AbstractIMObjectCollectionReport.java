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
import net.sf.jasperreports.engine.JRTextElement;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignExpression;
import net.sf.jasperreports.engine.design.JRDesignField;
import net.sf.jasperreports.engine.design.JRDesignStaticText;
import net.sf.jasperreports.engine.design.JRDesignTextField;
import net.sf.jasperreports.engine.design.JasperDesign;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract implementation of the {@link IMObjectCollectionReport} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractIMObjectCollectionReport
        implements IMObjectCollectionReport {

    /**
     * The collection node descriptor.
     */
    private final NodeDescriptor _descriptor;

    /**
     * The archetype service.
     */
    private final IArchetypeService _service;


    /**
     * Construct a new <code>AbstractIMObjectCollectionReport</code>.
     *
     * @param descriptor the collection node descriptor
     * @param service    the archetype service
     */
    public AbstractIMObjectCollectionReport(NodeDescriptor descriptor,
                                            IArchetypeService service) {
        _descriptor = descriptor;
        _service = service;
    }

    /**
     * Generates the report.
     *
     * @return the report
     * @throws JRException for any error
     */
    public JasperDesign generate() throws JRException {
        JasperDesign design = getDesign();
        design.setName(_descriptor.getName() + "_subreport");
        NodeDescriptor[] nodes = getDescriptors();

        JRElementFactory factory = new JRElementFactory(design);
        JRTextElement proto = factory.createTextField();
        Font font = getFont(proto);
        int[] widths = getWidths(nodes, font, design.getPageWidth());

        JRElementFactory columnFactory
                = new JRElementFactory(design, design.getColumnHeader());
        JRDesignBand columns = columnFactory.createColumnHeader();
        int x = 0;
        for (int i = 0; i < widths.length; ++i) {
            NodeDescriptor node = nodes[i];
            JRDesignStaticText label = columnFactory.createStaticText(
                    node.getDisplayName());
            label.setX(x);
            label.setWidth(widths[i]);
            columns.addElement(label);
            x += widths[i];
        }
        design.setColumnHeader(columns);

        JRDesignBand detail = factory.createDetail();
        x = 0;
        for (int i = 0; i < widths.length; ++i) {
            NodeDescriptor node = nodes[i];
            JRDesignField field = new JRDesignField();
            field.setName(getFieldName(node));
            Class valueClass = JasperReportHelper.getValueClass(node);
            field.setValueClass(valueClass);
            design.addField(field);
            JRDesignTextField textField = factory.createTextField();
            JRDesignExpression expression = new JRDesignExpression();
            expression.setValueClass(valueClass);
            expression.setText("$F{" + getFieldName(node) + "}");
            textField.setExpression(expression);
            textField.setX(x);
            textField.setWidth(widths[i]);
            detail.addElement(textField);
            x += widths[i];
        }
        design.setDetail(detail);
        return design;
    }

    /**
     * Returns the descriptors of the nodes to display.
     *
     * @return the descriptors of the nodes to display
     */
    protected abstract NodeDescriptor[] getDescriptors();

    /**
     * Returns the node name to be used in a field expression.
     *
     * @param descriptor the node descriptor
     * @return the node name
     */
    protected String getFieldName(NodeDescriptor descriptor) {
        return descriptor.getName();
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getArchetypeService() {
        return _service;
    }

    /**
     * Helper to return the first archetype.
     *
     * @return the first archetype
     */
    protected ArchetypeDescriptor getArchetype() {
        return getArchetypes().get(0);
    }

    /**
     * Helper to return the archetypes.
     */
    protected List<ArchetypeDescriptor> getArchetypes() {
        return getArchetypes(_descriptor);
    }

    /**
     * Helper to return archetypes for a node.
     *
     * @param descriptor the node descriptor
     * @return the archetypes corresponding to <code>descriptor</code>
     */
    protected List<ArchetypeDescriptor> getArchetypes(
            NodeDescriptor descriptor) {
        List<ArchetypeDescriptor> archetypes
                = new ArrayList<ArchetypeDescriptor>();
        for (String shortName : descriptor.getArchetypeRange()) {
            List<ArchetypeDescriptor> matches
                    = _service.getArchetypeDescriptors(shortName);
            archetypes.addAll(matches);
        }
        return archetypes;
    }

    /**
     * Returns the report design.
     *
     * @return the report design
     * @throws JRException for any error
     */
    protected JasperDesign getDesign() throws JRException {
        return JasperReportHelper.getReportResource(
                "/archetype_subreport_template.jrxml");
    }

    /**
     * Helper to calculate the widths for the nodes.
     *
     * @param nodes     the nodes
     * @param font      the font
     * @param pageWidth the pageWidth
     * @return the widths of each of the nodes
     */
    private int[] getWidths(NodeDescriptor[] nodes,
                            Font font, int pageWidth) {
        int[] widths = new int[nodes.length];
        BufferedImage hack = new BufferedImage(1, 1,
                                               BufferedImage.TYPE_INT_RGB);
        Graphics graphics = hack.getGraphics();
        FontMetrics metrics = graphics.getFontMetrics(font);
        int wWidth = metrics.charWidth('W');

        int total = 0;
        for (int i = 0; i < widths.length; ++i) {
            NodeDescriptor node = nodes[i];
            int length = node.getMaxLength();
            if (length > 50) {
                length = 50;
            }
            int width = length * wWidth;
            total += width;
            widths[i] = width;
        }
        if (total > pageWidth) {
            double ratio = (double) pageWidth / total;
            for (int i = 0; i < widths.length; ++i) {
                widths[i] = (int) (widths[i] * ratio);
            }
        }
        return widths;
    }

    /**
     * Helper to return a font from a text element.
     *
     * @param text the text element
     * @return font the font
     */
    private Font getFont(JRTextElement text) {
        int style = 0;
        if (text.isBold()) {
            style |= Font.BOLD;
        }
        if (text.isItalic()) {
            style |= Font.ITALIC;
        }
        return new Font(text.getFontName(), style, text.getFontSize());
    }
}
