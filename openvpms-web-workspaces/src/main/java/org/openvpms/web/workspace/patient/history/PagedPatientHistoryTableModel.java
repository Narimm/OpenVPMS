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

package org.openvpms.web.workspace.patient.history;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.app.Context;
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
     * Determines if the visit items are being sorted ascending or descending.
     */
    private boolean sortAscending = true;


    /**
     * Constructs a {@link PagedPatientHistoryTableModel}.
     *
     * @param model      the underlying table model
     * @param context    the context
     * @param shortNames the archetype short names of the child acts to display
     */
    public PagedPatientHistoryTableModel(IMObjectTableModel<Act> model, Context context, String... shortNames) {
        super(model, context, shortNames);
    }

    /**
     * Determines if the visit items are being sorted ascending or descending.
     *
     * @param ascending if {@code true} visit items are to be sorted ascending; {@code false} if descending
     */
    public void setSortAscending(boolean ascending) {
        sortAscending = ascending;
    }

    /**
     * Determines if the visit items are being sorted ascending or descending.
     *
     * @return {@code true} if visit items are to be sorted ascending; {@code false} if descending
     */
    public boolean isSortAscending() {
        return sortAscending;
    }

    /**
     * Creates an iterator over the act hierarchy.
     *
     * @param objects    the objects to iterate over
     * @param shortNames the child archetype short names to include in the iteration
     * @return a new iterator
     */
    @Override
    protected ActHierarchyIterator<Act> createIterator(List<Act> objects, String[] shortNames) {
        return new PatientHistoryIterator(objects, shortNames, sortAscending);
    }
}
