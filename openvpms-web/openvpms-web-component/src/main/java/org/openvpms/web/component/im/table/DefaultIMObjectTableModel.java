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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.table;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.web.component.im.query.Query;


/**
 * Default {@link IMObjectTableModel}, displaying the {@code IMObject}'s name and description.
 *
 * @author Tim Anderson
 */
public class DefaultIMObjectTableModel<T extends IMObject>
        extends BaseIMObjectTableModel<T> {

    /**
     * Constructs a {@code DefaultIMObjectTableModel}, displaying the {@code IMObjects} name and description.
     */
    public DefaultIMObjectTableModel() {
        this(true, true);
    }

    /**
     * Constructs a {@code DefaultIMObjectTableModel}.
     *
     * @param showName        if {@code true} show the object's name
     * @param showDescription if {@code true} show the object's description
     */
    public DefaultIMObjectTableModel(boolean showName, boolean showDescription) {
        super(null);
        setTableColumnModel(createTableColumnModel(false, false, showName, showDescription, false));
    }

    /**
     * Constructs a {@code DefaultIMObjectTableModel}, displaying the {@code IMObjects} name and description.
     * <p/>
     * If the query is querying both active and inactive objects, an active column will be displayed.
     *
     * @param query the query
     */
    public DefaultIMObjectTableModel(Query<T> query) {
        super(null);
        boolean showActive = query.getActive() == BaseArchetypeConstraint.State.BOTH;
        setTableColumnModel(createTableColumnModel(false, false, true, true, showActive));
    }

}
