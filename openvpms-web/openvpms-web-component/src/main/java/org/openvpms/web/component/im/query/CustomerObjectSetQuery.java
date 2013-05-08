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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.TextComponentFactory;


/**
 * Query implementation that queries customers. The search can be further
 * constrained to match on:
 * <ul>
 * <li>partial patient name;
 * <li>patient id; and/or
 * <li>partial contact description
 * </ul>
 * <p/>
 * The returned {@link ObjectSet}s contain:
 * <ul>
 * <li>the customer:
 * <pre>Party customer = (Party) set.get("customer");</pre>
 * <li>the patient, if searching on patients:
 * <pre>Party patient = (Party) set.get("patient");</pre>
 * <li>the contact, if searching on contacts:
 * <pre>Contact contact = (Contact) set.get("contact");</pre>
 * <li>the identity, if searching on identities:
 * <pre>EntityIdentity identity = (EntityIdentity) set.get("identity");</pre>
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see CustomerResultSet
 */
public class CustomerObjectSetQuery extends AbstractEntityQuery<ObjectSet> {

    /**
     * The patient field.
     */
    private TextField patient;

    /**
     * The contact field.
     */
    private TextField contact;

    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT
        = {new NodeSortConstraint("customer", "name")};


    /**
     * Construct a new <tt>CustomerQuery</tt> that queries customers
     * instances with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public CustomerObjectSetQuery(String[] shortNames) {
        super(shortNames, ObjectSet.class);
        setDefaultSortConstraint(DEFAULT_SORT);
    }

    /**
     * Sets the name or id of the patient to search on.
     *
     * @param value the patient name or id. May be <tt>null</tt>
     */
    public void setPatient(String value) {
        getPatient().setText(value);
    }

    /**
     * Sets the contact description to search on.
     *
     * @param value the contact description to search on. May be <tt>null</tt>
     */
    public void setContact(String value) {
        getContact().setText(value);
    }

    /**
     * Determines if the query selects a particular object.
     *
     * @param object the object to check
     * @return <tt>true</tt> if the object is selected by the query
     */
    public boolean selects(IMObject object) {
        return selects(object.getObjectReference());
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return <tt>true</tt> if the object reference is selected by the query
     */
    @Override
    public boolean selects(IMObjectReference reference) {
        CustomerResultSet set = (CustomerResultSet) createResultSet(null);
        set.setReferenceConstraint(reference);
        return set.hasNext();
    }

    /**
     * Returns the query state.
     *
     * @return the query state
     */
    public QueryState getQueryState() {
        return new Memento(this);
    }

    /**
     * Sets the query state.
     *
     * @param state the query state
     */
    public void setQueryState(QueryState state) {
        Memento memento = (Memento) state;
        setValue(memento.customer);
        setPatient(memento.patient);
        setContact(memento.contact);
        getIdentitySearch().setSelected(memento.identity);
        getInactive().setSelected(memento.inactive);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     * @return a new result set
     */
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        String patientWildcard = getWildcardedText(getPatient());
        String contactWildcard = getWildcardedText(getContact(), true);

        return new CustomerResultSet(getArchetypeConstraint(), getValue(),
                                     isIdentitySearch(), patientWildcard,
                                     contactWildcard, sort, getMaxResults(),
                                     isDistinct());
    }

    /**
     * Creates a container component to lay out the query component in.
     * This implementation returns a new <tt>Grid</tt>.
     *
     * @return a new container
     * @see #doLayout(Component)
     */
    @Override
    protected Component createContainer() {
        return GridFactory.create(8);
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
        addInactive(container);
        addPatientName(container);
        addContact(container);
        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Adds the patient name field to a container.
     *
     * @param container the container
     */
    protected void addPatientName(Component container) {
        TextField field = getPatient();
        container.add(LabelFactory.create("customerquery.patient"));
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
        container.add(LabelFactory.create("customerquery.contact"));
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Returns the patient field.
     *
     * @return the patient field
     */
    private TextField getPatient() {
        if (patient == null) {
            patient = TextComponentFactory.create();
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
    private TextField getContact() {
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

    private static class Memento extends AbstractQueryState {

        public final String customer;

        public final String patient;

        public final String contact;

        public final boolean inactive;

        public final boolean identity;

        public Memento(CustomerObjectSetQuery query) {
            super(query);
            customer = query.getValue();
            patient = query.getPatient().getText();
            contact = query.getContact().getText();
            inactive = query.getInactive().isSelected();
            identity = query.isIdentitySearch();
        }

    }
}
