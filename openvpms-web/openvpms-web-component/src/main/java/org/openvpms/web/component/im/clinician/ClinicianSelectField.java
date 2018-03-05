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

package org.openvpms.web.component.im.clinician;

import nextapp.echo2.app.SelectField;
import org.openvpms.archetype.rules.user.ClinicianQueryFactory;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.echo.factory.ComponentFactory;

import java.util.List;

/**
 * A field to select a clinician.
 *
 * @author Tim Anderson
 */
public class ClinicianSelectField extends SelectField {

    /**
     * Constructs a {@link ClinicianSelectField}.
     */
    public ClinicianSelectField() {
        this(true);
    }

    /**
     * Constructs a {@link ClinicianSelectField}.
     *
     * @param all if {@code true}, add a localised "All"
     */
    public ClinicianSelectField(boolean all) {
        super(createModel(all));
        if (getModel().size() != 0) {
            setSelectedIndex(0);
        }
        ComponentFactory.setDefaultStyle(this);
        setCellRenderer(IMObjectListCellRenderer.NAME);
    }

    /**
     * Returns the available clinicians.
     *
     * @return the clinicians
     */
    @SuppressWarnings("unchecked")
    public List<User> getObjects() {
        List objects = ((IMObjectListModel) getModel()).getObjects();
        return (List<User>) objects;
    }

    /**
     * Returns the selected clinician.
     *
     * @return the selected clinician. May be {@code null}
     */
    public User getSelected() {
        return (User) getSelectedItem();
    }

    /**
     * Sets the selected clinician.
     *
     * @param user the user. May be {@code null}
     */
    public void setSelected(User user) {
        if (user != null) {
            setSelectedItem(user);
        } else {
            int index = ((IMObjectListModel) getModel()).getAllIndex();
            setSelectedIndex(index);
        }
    }

    /**
     * Sets the selected clinician by reference.
     *
     * @param reference the user reference. May be {@code null}
     */

    public void setSelected(IMObjectReference reference) {
        setSelected(IMObjectHelper.getObject(reference, getObjects()));
    }

    /**
     * Creates a model to select a clinician.
     *
     * @param all if {@code true}, add a localised "All"
     * @return a new model
     */
    private static IMObjectListModel createModel(boolean all) {
        ArchetypeQuery query = new ArchetypeQuery(UserArchetypes.USER, true, true);
        ClinicianQueryFactory.addClinicianConstraint(query);
        query.add(Constraints.sort("name"));
        query.add(Constraints.sort("id"));
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);

        List<IMObject> clinicians = QueryHelper.query(query);
        return new IMObjectListModel(clinicians, all, false);
    }

}
