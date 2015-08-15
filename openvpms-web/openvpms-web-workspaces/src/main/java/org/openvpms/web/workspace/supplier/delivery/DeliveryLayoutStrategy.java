/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.supplier.delivery;

import nextapp.echo2.app.Component;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.edit.IMObjectCollectionEditor;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TitledTextArea;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * Layout strategy for <em>act.supplierDelivery</em> and <em>act.supplierReturn</em> acts.
 * <p/>
 * Displays the {@code supplierNotes} below the simple items, if non-null.
 */
public class DeliveryLayoutStrategy extends ActLayoutStrategy {

    /**
     * The supplier invoice identifier node.
     */
    private static final String SUPPLIER_INVOICE_ID = "supplierInvoiceId";

    /**
     * The supplier notes node.
     */
    private static final String SUPPLIER_NOTES = "supplierNotes";

    /**
     * Constructs a {@link DeliveryLayoutStrategy} for viewing deliveries.
     */
    public DeliveryLayoutStrategy() {
        this(null);
    }

    /**
     * Constructs a {@link DeliveryLayoutStrategy} for editing deliveries.
     *
     * @param editor the delivery items editor. May be {@code null}
     */
    public DeliveryLayoutStrategy(IMObjectCollectionEditor editor) {
        super(editor);
        // exclude the supplier notes, as these are added manually
        ArchetypeNodes result = new ArchetypeNodes().exclude(SUPPLIER_NOTES);
        NodeDescriptor node = DescriptorHelper.getNode(SupplierArchetypes.DELIVERY, SUPPLIER_INVOICE_ID,
                                                       ServiceHelper.getArchetypeService());
        if (node != null && node.isReadOnly()) {
            result.excludeIfEmpty(SUPPLIER_INVOICE_ID);
        }
    }

    /**
     * Lays out child components in a grid.
     *
     * @param object     the object to lay out
     * @param parent     the parent object. May be {@code null}
     * @param properties the properties
     * @param container  the container to use
     * @param context    the layout context
     */
    @Override
    protected void doSimpleLayout(IMObject object, IMObject parent, List<Property> properties,
                                  Component container, LayoutContext context) {
        super.doSimpleLayout(object, parent, properties, container, context);
        IMObjectBean bean = new IMObjectBean(object);
        if (bean.hasNode(SUPPLIER_NOTES)) {
            String notes = bean.getString(SUPPLIER_NOTES);
            if (!StringUtils.isEmpty(notes)) {
                container.add(ColumnFactory.create(Styles.INSET_X, getSupplierNotes(notes)));
            }
        }
    }

    /**
     * Returns a component to display the supplier notes.
     *
     * @param notes the notes
     * @return a new component
     */
    private Component getSupplierNotes(String notes) {
        String displayName = DescriptorHelper.getDisplayName(SupplierArchetypes.DELIVERY, SUPPLIER_NOTES);
        TitledTextArea supplierNotes = new TitledTextArea(displayName);
        supplierNotes.setEnabled(false);
        supplierNotes.setText(notes);
        return supplierNotes;
    }

}
