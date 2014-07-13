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
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.echo.factory.ComponentFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.Collections;
import java.util.List;

/**
 * A field to select an {@link Location}.
 *
 * @author Tim Anderson
 */
public class LocationSelectField extends SelectField {

    /**
     * Constructs a {@link LocationSelectField}.
     * <p/>
     * This displays all locations.
     */
    public LocationSelectField() {
        super(createModel());
        initialise();
    }

    /**
     * Constructs a {@link LocationSelectField}.
     * <p/>
     * This displays all locations for the specified user, or those for the practice if the user doesn't define any.
     */
    public LocationSelectField(User user, Party practice) {
        super(createModel(user, practice, false, false));
        initialise();
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
     * Initialises this.
     */
    private void initialise() {
        ComponentFactory.setDefaultStyle(this);
        setCellRenderer(IMObjectListCellRenderer.NAME);
        if (getModel().size() != 0) {
            setSelectedIndex(0);
        }
    }

    /**
     * Constructs a new list model for all active locations.
     *
     * @return a new list model
     */
    private static ListModel createModel() {
        ArchetypeQuery query = new ArchetypeQuery(PracticeArchetypes.LOCATION, true)
                .add(Constraints.sort("name"))
                .setMaxResults(ArchetypeQuery.ALL_RESULTS);
        return new IMObjectListModel(QueryHelper.query(query), true, true);
    }

    /**
     * Creates a new list model for the locations for a user.
     *
     * @param user     the user. May be {@code null}
     * @param practice the practice. May be {@code null}
     * @param all      if {@code true}, add a localised "All"
     * @param none     if {@code true}, add a localised "None"
     * @return the model
     */
    private static ListModel createModel(User user, Party practice, boolean all, boolean none) {
        List<Party> locations = Collections.emptyList();
        if (user != null) {
            UserRules rules = ServiceHelper.getBean(UserRules.class);
            locations = rules.getLocations(user);
            if (locations.isEmpty()) {
                if (practice != null) {
                    locations = ServiceHelper.getBean(PracticeRules.class).getLocations(practice);
                }
            }
            IMObjectSorter.sort(locations, "name");
        }
        return new IMObjectListModel(locations, all, none);
    }

}
