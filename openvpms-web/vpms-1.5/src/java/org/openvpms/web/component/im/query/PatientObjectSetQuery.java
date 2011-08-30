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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.focus.FocusHelper;
import org.openvpms.web.component.util.CheckBoxFactory;
import org.openvpms.web.component.util.LabelFactory;


/**
 * Query implementation that queries patients. The search can be further
 * constrained to only include those patients associated with the current
 * customer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientObjectSetQuery extends AbstractEntityQuery<ObjectSet> {

    /**
     * The customer to limit the search to. If <tt>null</tt>, indicates to
     * query all patients.
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
     * Construct a new <tt>PatientQuery</tt> that queries IMObjects with the
     * specified short names, and using the current customer, if set.
     *
     * @param shortNames the patient archetype short names
     * @param context    the context
     */
    public PatientObjectSetQuery(String[] shortNames, Context context) {
        this(shortNames, context.getCustomer());
    }

    /**
     * Construct a new <tt>PatientQuery</tt> that queries IMObjects with the
     * specified short names, and customer.
     *
     * @param shortNames the patient archetype short names
     * @param customer   the customer. May be <tt>null</tt>
     * @throws org.openvpms.component.system.common.query.ArchetypeQueryException
     *          if the short names don't match any
     *          archetypes
     */
    public PatientObjectSetQuery(String[] shortNames, Party customer) {
        super(shortNames, Party.class);
        this.customer = customer;
    }

    /**
     * Determines if the 'all patients' checkbox should be displayed.
     *
     * @param show if <tt>true</tt>, display the 'all patients' checkbox
     */
    public void setShowAllPatients(boolean show) {
        showAllPatients = show;
    }

    /**
     * Determines if the all patients checbox is selected.
     *
     * @return <tt>true</tt> if the 'all patients' checkbox is selected
     */
    public boolean isAllPatientsSelected() {
        return allPatients != null && allPatients.isSelected();
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <tt>true</tt> if the query should be run automaticaly;
     *         otherwise <tt>false</tt>
     */
    @Override
    public boolean isAuto() {
        return (customer != null);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     * @return a new result set
     */
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        return new PatientResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), customer, sort,
                                    getMaxResults());
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
        addInactive(container);
        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Adds the inactive checkbox to a container.
     *
     * @param container the container
     */
    protected void addAllPatients(Component container) {
        boolean selected = (customer == null);
        allPatients = CheckBoxFactory.create(selected);
        Label label = LabelFactory.create(ALL_PATIENTS_ID);
        container.add(label);
        container.add(allPatients);
        getFocusGroup().add(allPatients);
    }

}