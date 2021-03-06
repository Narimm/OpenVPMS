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

package org.openvpms.web.component.im.patient;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusHelper;


/**
 * Query implementation that queries patients. The search can be further
 * constrained to only include those patients associated with the current
 * customer.
 *
 * @author Tim Anderson
 */
public class PatientObjectSetQuery extends AbstractEntityQuery<ObjectSet> {

    /**
     * The customer to limit the search to. If {@code null}, indicates to query all patients.
     */
    private final Party customer;

    /**
     * Determines if the 'all patients' checkbox should be displayed.
     */
    private boolean showAllPatients;

    /**
     * The 'all patients' checkbox. If selected, query all patients, otherwise
     * constrain the search to the current customer.
     */
    private CheckBox allPatients;

    /**
     * All patients label id.
     */
    private static final String ALL_PATIENTS_ID = "allpatients";


    /**
     * Constructs a {@link PatientQuery} that queries IMObjects with the specified short names, and using the current
     * customer, if set.
     *
     * @param shortNames the patient archetype short names
     * @param context    the context
     */
    public PatientObjectSetQuery(String[] shortNames, Context context) {
        this(shortNames, context.getCustomer());
    }

    /**
     * Constructs a {@link PatientQuery} that queries IMObjects with the specified short names, and customer.
     *
     * @param shortNames the patient archetype short names
     * @param customer   the customer. May be {@code null}
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public PatientObjectSetQuery(String[] shortNames, Party customer) {
        super(shortNames, Party.class);
        this.customer = customer;
    }

    /**
     * Determines if the 'all patients' checkbox should be displayed.
     *
     * @param show if {@code true}, display the 'all patients' checkbox
     */
    public void setShowAllPatients(boolean show) {
        showAllPatients = show;
    }

    /**
     * Determines if all patients should be returned by the query.
     * <p/>
     * Only applies if the query has a customer. If not, then the flag is ignored.
     *
     * @param all if {@code true} query all patients, otherwise query patients associated with the customer
     */
    public void setQueryAllPatients(boolean all) {
        getAllPatients().setSelected(all);
    }

    /**
     * Determines if all patients are being queried.
     *
     * @return {@code true} if the 'all patients' checkbox is selected
     */
    public boolean isQueryAllPatients() {
        return getAllPatients().isSelected() || customer == null;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return {@code true} if the query should be run automatically; otherwise {@code false}
     */
    @Override
    public boolean isAuto() {
        return (customer != null);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        Reference party = isQueryAllPatients() ? null : customer != null ? customer.getObjectReference() : null;
        return new PatientResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), party,
                                    getConstraints(), sort, getMaxResults());
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        addShortNameSelector(container);
        addSearchField(container);
        addIdentitySearch(container);
        if (showAllPatients) {
            addAllPatients(container);
        }
        addActive(container);
        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Adds the 'all patients' checkbox to a container.
     *
     * @param container the container
     */
    protected void addAllPatients(Component container) {
        CheckBox box = getAllPatients();
        Label label = LabelFactory.create(ALL_PATIENTS_ID);
        container.add(label);
        container.add(box);
        getFocusGroup().add(box);
    }

    /**
     * Returns the 'all patients' check box.
     *
     * @return the check box
     */
    private CheckBox getAllPatients() {
        if (allPatients == null) {
            allPatients = CheckBoxFactory.create();
            allPatients.addActionListener(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                }
            });
            boolean selected = (customer == null);
            allPatients.setSelected(selected);
        }
        return allPatients;
    }
}