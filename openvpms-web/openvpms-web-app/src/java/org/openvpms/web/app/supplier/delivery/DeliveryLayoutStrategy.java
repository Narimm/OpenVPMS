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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.supplier.delivery;

import nextapp.echo2.app.Component;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.TitledTextArea;

import java.util.List;


/**
 * Layout strategy for <em>act.supplierDelivery</em> and <em>act.supplierReturn</em> acts.
 * <p/>
 * Displays the <tt>supplierNotes</tt> below the simple items, if non-null.
 */
public class DeliveryLayoutStrategy extends ActLayoutStrategy {

    /**
     * Constructs a <tt>DeliveryLayoutStrategy</tt> for viewing deliveries.
     */
    public DeliveryLayoutStrategy() {
    }

    /**
     * Constructs a <tt>DeliveryLayoutStrategy</tt> for editing deliveries.
     *
     * @param editor the delivery items editor
     */
    public DeliveryLayoutStrategy(IMObjectCollectionEditor editor) {
        super(editor);
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object      the object to lay out
     * @param parent      the parent object. May be <tt>null</tt>
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param container   the container to use
     * @param context     the layout context
     */
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<NodeDescriptor> descriptors,
                                  PropertySet properties, Component container, LayoutContext context) {
        super.doSimpleLayout(object, parent, descriptors, properties, container, context);
        IMObjectBean bean = new IMObjectBean(object);
        if (bean.hasNode("supplierNotes")) {
            String notes = bean.getString("supplierNotes");
            if (!StringUtils.isEmpty(notes)) {
                container.add(ColumnFactory.create("InsetX", getSupplierNotes(notes)));
            }
        }
    }

    /**
     * Returns a node filter to filter nodes. This implementation filters the "supplierNotes" node.
     *
     * @param object  the object
     * @param context the context
     * @return a node filter to filter nodes
     */
    @Override
    protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
        NamedNodeFilter notesFilter = new NamedNodeFilter("supplierNotes");
        return new ChainedNodeFilter(super.getNodeFilter(object, context), notesFilter);
    }

    /**
     * Returns a component to display the supplier notes.
     *
     * @param notes the notes
     * @return a new component
     */
    private Component getSupplierNotes(String notes) {
        String displayName = DescriptorHelper.getDisplayName(SupplierArchetypes.DELIVERY, "supplierNotes");
        TitledTextArea supplierNotes = new TitledTextArea(displayName);
        supplierNotes.setEnabled(false);
        supplierNotes.setText(notes);
        return supplierNotes;
    }
}
