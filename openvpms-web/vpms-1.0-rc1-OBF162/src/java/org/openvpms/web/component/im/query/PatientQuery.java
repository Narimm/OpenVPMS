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

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.LabelFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Query implementation that queries patients. The search can be further
 * constrained to only include those patients associated with the current
 * customer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientQuery extends AbstractEntityQuery<Party> {

    /**
     * The customer to limit the search to. If <code>null</code>, indicates to
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
     * Construct a new <code>PatientQuery</code> that queries IMObjects with the
     * specified short names, and using the current customer, if set.
     *
     * @param shortNames the patient archetype short names
     * @param context    the context
     */
    public PatientQuery(String[] shortNames, Context context) {
        this(shortNames, context.getCustomer());
    }

    /**
     * Construct a new <code>PatientQuery</code> that queries IMObjects with the
     * specified short names, and customer.
     *
     * @param shortNames the patient archetype short names
     * @param customer   the customer. May be <code>null</code>
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public PatientQuery(String[] shortNames, Party customer) {
        super(shortNames);
        this.customer = customer;
    }

    /**
     * Determines if the 'all patients' checkbox should be displayed.
     *
     * @param show if <code>true</code>, display the 'all patients' checkbox
     */
    public void setShowAllPatients(boolean show) {
        showAllPatients = show;
    }

    /**
     * Determines if the all patients checbox is selected.
     *
     * @return <code>true</code> if the 'all patients' checkbox is selected
     */
    public boolean isAllPatientsSelected() {
        return allPatients != null && allPatients.isSelected();
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Party> query(SortConstraint[] sort) {
        getComponent();  // ensure the component is rendered
        ResultSet<Party> result;
        if (isAllPatientsSelected()) {
            result = super.query(sort);
        } else {
            List<Party> objects = null;
            if (customer != null) {
                objects = filterForCustomer();
            }
            if (objects == null) {
                objects = Collections.emptyList();
            }
            result = new IMObjectListResultSet<Party>(objects, getMaxResults());
            if (sort != null) {
                result.sort(sort);
            }
        }
        return result;
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwie <code>false</code>
     */
    @Override
    public boolean isAuto() {
        return (customer != null);
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
        if (showAllPatients) {
            addAllPatients(container);
        }
        addInactive(container);
        ApplicationInstance.getActive().setFocusedComponent(getInstanceName());
    }

    /**
     * Adds the inactive checkbox to a container.
     *
     * @param container the container
     */
    protected void addAllPatients(Component container) {
        allPatients = new CheckBox();
        boolean selected = (customer == null);
        allPatients.setSelected(selected);
        Label label = LabelFactory.create(ALL_PATIENTS_ID);
        container.add(label);
        container.add(allPatients);
        getFocusGroup().add(allPatients);
    }

    /**
     * Filter patients associated with a customer.
     *
     * @return a list of patients associated with the customer that matches the
     *         specified criteria
     */
    private List<Party> filterForCustomer() {
        List<Party> result = null;
        List<Party> patients = getPatients(customer);
        String type = getShortName();
        String name = getName();
        boolean activeOnly = !includeInactive();
        boolean idSearch = isIdentitySearch();

        if (type == null || type.equals(ShortNameListModel.ALL)) {
            for (String shortName : getShortNames()) {
                List<Party> matches
                        = filter(patients, shortName, name, idSearch,
                                 activeOnly);
                if (result == null) {
                    result = matches;
                } else {
                    result.addAll(matches);
                }
            }
        } else {
            result = filter(patients, type, name, idSearch, activeOnly);
        }
        return result;
    }

    /**
     * Filter a list of objects.
     *
     * @param objects    the objects to filter
     * @param shortName  the archetype shortname to matches on
     * @param name       the object instance name to matches on
     * @param activeOnly if <code>true</code>, only include active objects
     * @return a list of objects that matches the specified criteria
     */
    private List<Party> filter(List<Party> objects, String shortName,
                               String name, boolean idSearch,
                               boolean activeOnly) {
        if (idSearch) {
            objects = IMObjectHelper.findEntityByIdentity(name, objects);
        } else {
            objects = IMObjectHelper.findEntityByName(name, objects);
        }

        List<Party> result = new ArrayList<Party>();
        for (Party object : objects) {
            ArchetypeId id = object.getArchetypeId();
            if (!TypeHelper.matches(id, shortName)) {
                continue;
            }
            if (activeOnly && !object.isActive()) {
                continue;
            }
            result.add(object);
        }

        return result;
    }

    /**
     * Returns tha patients associated with a customer.
     *
     * @param customer the customer
     * @return a list of patients associated with <code>nustomer</code>
     */
    private List<Party> getPatients(Party customer) {
        List<Party> result = new ArrayList<Party>();
        Set<EntityRelationship> relationships
                = customer.getEntityRelationships();
        IMObjectReference source = new IMObjectReference(customer);

        for (EntityRelationship relationship : relationships) {
            if (TypeHelper.isA(relationship,
                               "entityRelationship.patientOwner")) {
                if (source.equals(relationship.getSource()) &&
                        ((relationship.getActiveEndTime() == null) ||
                                (relationship.getActiveEndTime().after(
                                        new Date(System.currentTimeMillis())))))
                {
                    Party object = (Party) IMObjectHelper.getObject(
                            relationship.getTarget());
                    if (object != null) {
                        result.add(object);
                    }
                }
            }
        }
        return result;
    }

}
