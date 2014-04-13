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

package org.openvpms.web.component.im.location;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.ListModel;
import org.openvpms.archetype.rules.practice.Location;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.echo.factory.ComponentFactory;

/**
 * A field to select an {@link Location}.
 *
 * @author Tim Anderson
 */
public class LocationSelectField extends SelectField {

    /**
     * Constructs a {@link LocationSelectField}.
     */
    public LocationSelectField() {
        super(createModel());
        ComponentFactory.setDefaultStyle(this);
        setCellRenderer(IMObjectListCellRenderer.NAME);
        if (getModel().size() != 0) {
            setSelectedIndex(0);
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
     * Returns the selected location.
     *
     * @return the selected location.
     */
    public Location getSelected() {
        return isAllSelected() ? Location.ALL : new Location((Party) getSelectedItem());
    }

    /**
     * Returns the list model.
     *
     * @return the list model
     */
    @Override
    public IMObjectListModel getModel() {
        return (IMObjectListModel) super.getModel();
    }

    /**
     * Constructs a new list model.
     *
     * @return a new list model
     */
    private static ListModel createModel() {
        ArchetypeQuery query = new ArchetypeQuery(PracticeArchetypes.LOCATION, true)
                .add(Constraints.sort("name"))
                .setMaxResults(ArchetypeQuery.ALL_RESULTS);
        return new IMObjectListModel(QueryHelper.query(query), true, true);
    }

}
