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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.location;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.ListModel;
import org.openvpms.archetype.rules.practice.Location;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.echo.factory.ComponentFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
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
     * This displays all active locations linked to the practice
     *
     * @param practice the practice
     */
    public LocationSelectField(Party practice) {
        super(createModel(practice));
        initialise();
    }

    /**
     * Constructs a {@link LocationSelectField}.
     * <p/>
     * This displays all locations for the specified user, or those for the practice if the user doesn't define any.
     *
     * @param user     the user. May be {@code null}
     * @param practice the practice. May be {@code null}
     * @param all      if {@code true}, add a localised "All"
     */
    public LocationSelectField(User user, Party practice, boolean all) {
        super(createModel(user, practice, all, false));
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
     * Sets the selected location.
     *
     * @param location may be {@code null}
     */
    public void setSelected(Location location) {
        IMObjectListModel model = getModel();
        if (location == null || (location.isAll() && model.getAllIndex() == -1)
            || (location.isNone() && model.getNoneIndex() == -1)) {
            setSelectedItem(null);
        } else if (location.isAll()) {
            setSelectedIndex(model.getAllIndex());
        } else if (location.isNone()) {
            setSelectedIndex(model.getNoneIndex());
        } else {
            setSelectedItem(location.getLocation());
        }
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
     * Returns the locations.
     *
     * @return the locations
     */
    public List<Party> getLocations() {
        List<Party> result = new ArrayList<>();
        for (IMObject object : getModel().getObjects()) {
            if (object instanceof Party) {
                result.add((Party) object);
            }
        }
        return result;
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
     * @param practice the practice
     * @return a new list model
     */
    private static ListModel createModel(Party practice) {
        List<Party> locations = getLocations(practice);
        return new IMObjectListModel(locations, true, true);
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
        if (user != null && practice != null) {
            UserRules rules = ServiceHelper.getBean(UserRules.class);
            locations = rules.getLocations(user, practice);
            if (!locations.isEmpty()) {
                sort(locations);
            }
        } else if (practice != null) {
            locations = getLocations(practice);
        }
        return new IMObjectListModel(locations, all, none);
    }

    /**
     * Returns the active locations associated with the practice, sorted on name.
     *
     * @param practice the practice
     * @return the locations
     */
    private static List<Party> getLocations(Party practice) {
        List<Party> locations;
        if (practice != null) {
            locations = ServiceHelper.getBean(PracticeRules.class).getLocations(practice);
            sort(locations);
        } else {
            locations = new ArrayList<>();
        }
        return locations;
    }

    /**
     * Sorts locations on name.
     *
     * @param locations the locations
     */
    private static void sort(List<Party> locations) {
        IMObjectSorter.sort(locations, "name");
    }

}
