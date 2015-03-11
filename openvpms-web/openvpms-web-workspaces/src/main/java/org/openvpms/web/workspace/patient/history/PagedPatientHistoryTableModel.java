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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.act.ActHierarchyIterator;
import org.openvpms.web.component.im.act.PagedActHierarchyTableModel;

import java.util.List;

/**
 * Patient history table model that supports paging.
 *
 * @author Tim Anderson
 */
public class PagedPatientHistoryTableModel extends PagedActHierarchyTableModel<Act> {

    /**
     * Constructs a {@link PagedPatientHistoryTableModel}.
     *
     * @param model      the underlying table model
     * @param shortNames the archetype short names of the child acts to display
     */
    public PagedPatientHistoryTableModel(AbstractPatientHistoryTableModel model, String... shortNames) {
        super(model, shortNames);
    }

    /**
     * Sets the objects for the current page.
     *
     * @param objects the objects to set
     */
    @Override
    protected ActHierarchyIterator<Act> createIterator(List<Act> objects, String[] shortNames) {
        return new PatientHistoryIterator(objects, shortNames, isSortAscending());
    }

}
