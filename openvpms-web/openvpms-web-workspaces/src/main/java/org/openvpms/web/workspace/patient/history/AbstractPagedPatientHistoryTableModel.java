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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.act.PagedActHierarchyTableModel;

/**
 * A table model that for patient history that supports paging and text filtering.
 *
 * @author Tim Anderson
 */
public class AbstractPagedPatientHistoryTableModel extends PagedActHierarchyTableModel<Act> {

    /**
     * The search criteria.
     */
    private TextSearch search;

    /**
     * Constructs an {@link AbstractPagedPatientHistoryTableModel}.
     *
     * @param model      the underlying table model
     * @param shortNames the archetype short names of the child acts to display
     */
    public AbstractPagedPatientHistoryTableModel(AbstractPatientHistoryTableModel model, String[] shortNames) {
        super(model, shortNames);
    }

    /**
     * Sets the search criteria.
     *
     * @param search the search criteria. May be {@code null}
     */
    public void setSearch(String search) {
        search = StringUtils.trimToNull(search);
        // NOTE: the service associated with the model performs caching
        AbstractPatientHistoryTableModel model = getModel();
        this.search = (search != null) ? new TextSearch(search, model.showClinician(), model.showBatches(),
                                                        model.getService()) : null;
    }

    /**
     * Returns the model to delegate to.
     *
     * @return the model
     */
    @Override
    public AbstractPatientHistoryTableModel getModel() {
        return (AbstractPatientHistoryTableModel) super.getModel();
    }

    /**
     * Returns the search criteria.
     *
     * @return the search criteria. May be {@code null}
     */
    protected TextSearch getSearch() {
        return search;
    }

}
