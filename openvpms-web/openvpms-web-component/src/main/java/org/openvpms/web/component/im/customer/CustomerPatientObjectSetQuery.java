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

package org.openvpms.web.component.im.customer;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.BaseArchetypeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.web.component.im.query.AbstractEntityQuery;
import org.openvpms.web.component.im.query.AbstractQueryState;
import org.openvpms.web.component.im.query.QueryState;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.echo.text.TextField;

/**
 * Base class for queries that return customers and their patients.
 *
 * @author Tim Anderson
 */
public abstract class CustomerPatientObjectSetQuery extends AbstractEntityQuery<ObjectSet> {

    /**
     * If {@code true}, automatically select the identity search box if the patient field contains numerics.
     */
    private final boolean checkPatientIdentity;

    /**
     * The current customer.
     */
    private Reference customerRef;

    /**
     * The patient field.
     */
    private TextField patient;

    /**
     * The contact field.
     */
    private TextField contact;

    /**
     * Constructs a {@link CustomerPatientObjectSetQuery}.
     *
     * @param shortNames            the archetypes to query
     * @param checkCustomerIdentity if {@code true}, automatically select the identity search box if the customer
     *                              field contains numerics, otherwise select it if the patient field contains numerics
     * @param customer              the customer. May be {@code null}
     */
    public CustomerPatientObjectSetQuery(String[] shortNames, boolean checkCustomerIdentity, Party customer) {
        super(shortNames, checkCustomerIdentity, Party.class);
        checkPatientIdentity = !checkCustomerIdentity;
        if (customer != null) {
            customerRef = customer.getObjectReference();
            setValue(customer.getName());
        }
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return {@code true} if the query should be run automatically; otherwise {@code false}
     */
    @Override
    public boolean isAuto() {
        return (customerRef != null);
    }


    /**
     * Sets the name or id of the patient to search on.
     *
     * @param value the patient name or id. May be {@code null}
     */
    public void setPatient(String value) {
        getPatient().setText(value);
    }

    /**
     * Sets the contact description to search on.
     *
     * @param value the contact description to search on. May be {@code null}
     */
    public void setContact(String value) {
        getContact().setText(value);
    }

    /**
     * Determines if the query selects a particular object.
     *
     * @param object the object to check
     * @return {@code true} if the object is selected by the query
     */
    public boolean selects(IMObject object) {
        return selects(object.getObjectReference());
    }

    /**
     * Sets the query state.
     *
     * @param state the query state
     */
    public void setQueryState(QueryState state) {
        Memento memento = (Memento) state;
        setValue(memento.customer);
        customerRef = memento.customerRef;
        setPatient(memento.patient);
        setContact(memento.contact);
        getIdentitySearch().setSelected(memento.identity);
        setActive(memento.active);
    }

    /**
     * Returns the preferred height of the query when rendered.
     *
     * @return the preferred height, or {@code null} if it has no preferred height
     */
    @Override
    public Extent getHeight() {
        return getHeight(2);
    }

    /**
     * Sets the customer.
     *
     * @param customer the customer. May be {@code null}
     */
    public void setCustomer(Party customer) {
        if (customer != null) {
            getPatient().setText(customer.getName());
            this.customerRef = customer.getObjectReference();
        } else {
            getPatient().setText(null);
        }
    }

    /**
     * Returns the customer reference.
     *
     * @return the customer reference
     */
    public Reference getCustomer() {
        return customerRef;
    }

    /**
     * Invoked when the search field changes.
     */
    @Override
    protected void onSearchFieldChanged() {
        onCustomerChanged();
        super.onSearchFieldChanged();
    }

    /**
     * Creates a container component to lay out the query component in.
     * This implementation returns a new {@code Grid}.
     *
     * @return a new container
     * @see #doLayout(Component)
     */
    @Override
    protected Component createContainer() {
        return GridFactory.create(6);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        addShortNameSelector(container);
        addSearchField(container);
        addPatient(container);
        addContact(container);
        addIdentitySearch(container);
        addActive(container);

        // if searching all customers, move the focus to the customer field, otherwise move it to the patient field
        if (customerRef == null) {
            FocusHelper.setFocus(getSearchField());
        } else {
            FocusHelper.setFocus(getPatient());
        }
    }

    /**
     * Adds the customer field to a container.
     *
     * @param container the container
     */
    @Override
    protected void addSearchField(Component container) {
        Label label = LabelFactory.create("customerpatientquery.customer");
        container.add(label);
        TextField field = getSearchField();
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Adds the contact field to a container.
     *
     * @param container the container
     */
    protected void addContact(Component container) {
        TextField field = getContact();
        container.add(LabelFactory.create("customerpatientquery.contact"));
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Returns the patient field.
     *
     * @return the patient field
     */
    protected TextField getPatient() {
        if (patient == null) {
            patient = TextComponentFactory.create();
            patient.addPropertyChangeListener((evt) -> onPatientChanged());
            patient.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onQuery();
                }
            });
        }
        return patient;
    }

    /**
     * Returns the contact field.
     *
     * @return the contact field
     */
    protected TextField getContact() {
        if (contact == null) {
            contact = TextComponentFactory.create();
            contact.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onQuery();
                }
            });
        }
        return contact;
    }

    /**
     * Adds the patient field to a container.
     *
     * @param container the container
     */
    private void addPatient(Component container) {
        TextField field = getPatient();
        container.add(LabelFactory.create("customerpatientquery.patient"));
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Invoked when the customer text is changed.
     * <p/>
     * This implementation clears the customer reference so the search is not constrained to a single customer.
     */
    private void onCustomerChanged() {
        customerRef = null;
    }

    /**
     * Invoked when the patient text is changed.
     * <p/>
     * This implementation checks the identity search box if {@link #checkPatientIdentity} is {@code true} and the text
     * contains numerics.
     */
    private void onPatientChanged() {
        if (checkPatientIdentity) {
            checkIdentityName(getPatient().getText());
        }
    }

    protected static abstract class Memento extends AbstractQueryState {

        private final Reference customerRef;

        private final String customer;

        private final String patient;

        private final String contact;

        private final BaseArchetypeConstraint.State active;

        private final boolean identity;

        public Memento(CustomerPatientObjectSetQuery query) {
            super(query);
            customerRef = query.getCustomer();
            customer = query.getValue();
            patient = query.getPatient().getText();
            contact = query.getContact().getText();
            active = query.getActive();
            identity = query.isIdentitySearch();
        }

    }
}
