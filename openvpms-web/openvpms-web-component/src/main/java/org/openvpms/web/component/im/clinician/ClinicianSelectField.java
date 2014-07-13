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

package org.openvpms.web.component.im.clinician;

import nextapp.echo2.app.SelectField;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.echo.factory.ComponentFactory;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Iterator;
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
        super(createModel());
        if (getModel().size() != 0) {
            setSelectedIndex(0);
        }
        ComponentFactory.setDefaultStyle(this);
        setCellRenderer(IMObjectListCellRenderer.NAME);
    }

    /**
     * Creates a model to select a clinician.
     *
     * @return a new model
     */
    private static IMObjectListModel createModel() {
        UserRules rules = ServiceHelper.getBean(UserRules.class);
        List<IMObject> clinicians = new ArrayList<IMObject>();
        ArchetypeQuery query = new ArchetypeQuery(UserArchetypes.USER, true, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        Iterator<User> iter = new IMObjectQueryIterator<User>(query);
        while (iter.hasNext()) {
            User user = iter.next();
            if (rules.isClinician(user)) {
                clinicians.add(user);
            }
        }
        return new IMObjectListModel(clinicians, true, false);
    }
}
