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

package org.openvpms.web.workspace.patient.charge;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.AbstractCollectionResultSetFactory;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectTableCollectionEditor;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;

import java.util.List;

/**
 * A factory for {@link ResultSet}s for display by {@link IMObjectTableCollectionEditor} that filters acts
 * based on the current patient.
 *
 * @author Tim Anderson
 */
public class PatientCollectionResultSetFactory extends AbstractCollectionResultSetFactory {

    /**
     * The context.
     */
    private final Context context;

    /**
     * Constructs an {@link PatientCollectionResultSetFactory}.
     *
     * @param context the context
     */
    public PatientCollectionResultSetFactory(Context context) {
        this.context = context;
    }

    /**
     * Creates a new result set.
     *
     * @param property the collection property
     * @return a new result set
     */
    @Override
    public ResultSet<IMObject> createResultSet(CollectionPropertyEditor property) {
        List<IMObject> objects = property.getObjects(new PatientPredicate<IMObject>(context.getPatient()));
        IMObjectListResultSet<IMObject> set = new IMObjectListResultSet<IMObject>(objects, DEFAULT_ROWS);
        set.sort(new SortConstraint[]{new NodeSortConstraint("startTime", false)});
        return set;
    }
}
