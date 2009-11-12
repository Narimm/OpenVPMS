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
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.focus.FocusHelper;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.TextComponentFactory;


/**
 * Query implementation that queries customers. The search can be further
 * constrained to match on:
 * <ul>
 * <li>partial patient name; and/or
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
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 * @see CustomerResultSet
 */
public class CustomerObjectSetQuery extends AbstractEntityQuery<ObjectSet> {

    /**
     * The patient name field.
     */
    private TextField patientName;

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
     * Creates the result set.
     *
     * @param sort the sort criteria. May be <tt>null</tt>
     * @return a new result set
     */
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        String patientWildcard = getWildcardedText(getPatientName());
        String contactWildcard = getWildcardedText(getContact(), true);

        return new CustomerResultSet(getArchetypeConstraint(), getName(),
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
        addInstanceName(container);
        addIdentitySearch(container);
        addInactive(container);
        addPatientName(container);
        addContact(container);
        FocusHelper.setFocus(getInstanceName());
    }

    /**
     * Adds the patient name field to a container.
     *
     * @param container the container
     */
    protected void addPatientName(Component container) {
        TextField field = getPatientName();
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
     * Returns the patient name field.
     *
     * @return the patient name field
     */
    private TextField getPatientName() {
        if (patientName == null) {
            patientName = TextComponentFactory.create();
            patientName.addActionListener(new ActionListener() {
                public void onAction(ActionEvent event) {
                    onQuery();
                }
            });
        }
        return patientName;
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
}
