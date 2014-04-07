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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.product;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.ListModel;
import org.openvpms.archetype.rules.product.PricingGroup;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.list.LookupListCellRenderer;
import org.openvpms.web.component.im.list.LookupListModel;
import org.openvpms.web.component.im.lookup.ArchetypeLookupQuery;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.echo.factory.ComponentFactory;

/**
 * Pricing group select field.
 *
 * @author Tim Anderson
 */
public class PricingGroupSelectField extends SelectField {

    /**
     * Constructs a {@link PricingGroupSelectField}.
     *
     * @param initialSelection the initial selection. May be {@code null}
     * @param all              if {@code true}, include an option to select 'All'
     */
    public PricingGroupSelectField(Lookup initialSelection, boolean all) {
        super(createModel(all));
        ComponentFactory.setDefaultStyle(this);
        setCellRenderer(LookupListCellRenderer.INSTANCE);
        if (initialSelection != null) {
            setSelectedItem(initialSelection.getCode());
        }
    }

    /**
     * Determines if 'All' is selected.
     *
     * @return {@code true} if 'All' is selected
     */
    public boolean isAllSelected() {
        return getModel().isAll(getSelectedIndex());
    }

    /**
     * Returns the selected pricing group.
     *
     * @return the selected pricing group. May be {@code null}
     */
    public PricingGroup getSelected() {
        if (isAllSelected()) {
            return PricingGroup.ALL;
        }
        int index = getSelectedIndex();
        return (index >= 0) ? new PricingGroup(getModel().getLookup(index)) : null;
    }

    /**
     * Returns the list model.
     *
     * @return the list model
     */
    @Override
    public LookupListModel getModel() {
        return (LookupListModel) super.getModel();
    }

    /**
     * Constructs a new list model.
     *
     * @param all if {@code true} add a localised "All"
     * @return a new list model
     */
    private static ListModel createModel(boolean all) {
        LookupQuery query = new ArchetypeLookupQuery("lookup.pricingGroup");
        return new LookupListModel(query, all, true);
    }
}
