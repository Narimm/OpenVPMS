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

package org.openvpms.web.component.im.edit.invoice;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.ColumnLayoutData;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMObjectPrinter;
import org.openvpms.web.component.im.print.IMObjectReportPrinter;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Layout strategy that includes a 'Print Label' button to print
 * the act.
 */
public class PatientMedicationActLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Determines if the product node should be displayed. False if
     * the parent act has a product.
     */
    private boolean showProduct;

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <code>null</code>
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties,
                                IMObject parent, LayoutContext context) {

        if (parent instanceof Act) {
            ActBean bean = new ActBean((Act) parent);
            showProduct = !bean.hasNode("product");
        } else {
            showProduct = true;
        }
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lay out out the object in the specified container.
     *
     * @param object     the object to lay out
     * @param properties the object's properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doLayout(final IMObject object, PropertySet properties,
                            Component container, LayoutContext context) {
        Button button = ButtonFactory.create(
                "printlabel", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onPrintLabel(object);
            }
        });

        RowLayoutData rowLayout = new RowLayoutData();
        Alignment topRight = new Alignment(Alignment.RIGHT, Alignment.TOP);
        rowLayout.setAlignment(topRight);
        rowLayout.setWidth(new Extent(100, Extent.PERCENT));
        button.setLayoutData(rowLayout);
        Row row = RowFactory.create("InsetX", button);
        ColumnLayoutData columnLayout = new ColumnLayoutData();
        columnLayout.setAlignment(topRight);
        row.setLayoutData(columnLayout);
        container.add(row);
        super.doLayout(object, properties, container, context);
        getFocusGroup().add(button);
    }

    /**
     * Returns a node filter to filter nodes. This implementation return {@link
     * LayoutContext#getDefaultNodeFilter()}.
     *
     * @param context the context
     * @return a node filter to filter nodes, or <code>null</code> if no
     *         filterering is required
     */
    @Override
    protected NodeFilter getNodeFilter(LayoutContext context) {
        NodeFilter filter;
        if (!showProduct) {
            filter = super.getNodeFilter(context,
                                         new NamedNodeFilter("product"));
        } else {
            filter = super.getNodeFilter(context);
        }
        return filter;
    }

    /**
     * Invoked when the 'Print Label' button is pressed.
     */
    private void onPrintLabel(IMObject object) {
        IMObjectPrinter<IMObject> printer
                = new IMObjectReportPrinter<IMObject>();
        printer.print(object);
    }

}
