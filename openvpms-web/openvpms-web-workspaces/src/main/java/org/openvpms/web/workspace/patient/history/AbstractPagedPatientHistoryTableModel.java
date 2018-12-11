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
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.component.im.act.PagedActHierarchyTableModel;

import java.util.Set;
import java.util.function.Predicate;

/**
 * A table model that for patient history that supports paging and text filtering.
 *
 * @author Tim Anderson
 */
public class AbstractPagedPatientHistoryTableModel extends PagedActHierarchyTableModel<Act> {

    /**
     * The text search criteria.
     */
    private TextSearch textSearch;

    /**
     * The product type search criteria.
     */
    private ProductTypeSearch productTypeSearch;

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
     * Sets the text search criteria.
     *
     * @param search the search criteria. May be {@code null}
     */
    public void setTextSearch(String search) {
        search = StringUtils.trimToNull(search);
        // NOTE: the service associated with the model performs caching
        AbstractPatientHistoryTableModel model = getModel();
        this.textSearch = (search != null) ? new TextSearch(search, model.showClinician(), model.showBatches(),
                                                            model.getService()) : null;
    }

    /**
     * Sets the product type search criteria.
     *
     * @param productTypes the product types
     */
    public void setProductTypes(Set<Reference> productTypes) {
        if (productTypes.isEmpty()) {
            productTypeSearch = null;
        } else {
            productTypeSearch = new ProductTypeSearch(productTypes, getModel().getService());
        }
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
    protected Predicate<org.openvpms.component.model.act.Act> getSearch() {
        if (productTypeSearch != null && textSearch != null) {
            return productTypeSearch.and(textSearch);
        } else if (productTypeSearch != null) {
            return productTypeSearch;
        } else if (textSearch != null) {
            return textSearch;
        }
        return null;
    }

}
