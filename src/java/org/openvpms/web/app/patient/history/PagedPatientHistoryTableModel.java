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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.app.patient.history;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.act.ActHierarchyIterator;
import org.openvpms.web.component.im.act.PagedActHierarchyTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModel;

import java.util.List;

/**
 * Patient history table model that supports paging.
 *
 * @author Tim Anderson
 */
public class PagedPatientHistoryTableModel extends PagedActHierarchyTableModel<Act> {

    /**
     * Constructs a {@code PagedPatientHistoryTableModel}.
     *
     * @param model      the underlying table model
     * @param shortNames the archetype short names of the child acts to display
     */
    public PagedPatientHistoryTableModel(IMObjectTableModel<Act> model, String... shortNames) {
        super(model, shortNames);
    }

    /**
     * Creates a new {@link ActHierarchyIterator}.
     *
     * @param objects    the acts
     * @param shortNames the child archetype short names
     * @return an iterator to flatten the act hierarchy
     */
    @Override
    protected ActHierarchyIterator<Act> createFlattener(List<Act> objects, final String[] shortNames) {
        PatientHistoryFilter filter = new PatientHistoryFilter(shortNames);
        // maxDepth = 2 - display the events, and their immediate children
        return new ActHierarchyIterator<Act>(objects, filter, 2);
    }

}
