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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.StringUtils;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.list.ArchetypeShortNameListModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.LabelFactory;


/**
 * Query implementation that queries patients. The search can be further
 * constrained to only include those patients associated with the current
 * customer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientQuery extends AbstractQuery {

    /**
     * The customer to limit the search to. If <code>null</code>, indicates to
     * query all patients.
     */
    private final Party _customer;

    /**
     * The 'all patients' checkbox. If selected, query all patients, otherwise
     * constrain the search to the current customer.
     */
    private CheckBox _allPatients;

    /**
     * All patients label id.
     */
    private static final String ALL_PATIENTS_ID = "allpatients";


    /**
     * Construct a new <code>PatientQuery</code> that queries IMObjects with the
     * specified criteria, and using the current customer, if set.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public PatientQuery(String refModelName, String entityName,
                        String conceptName) {
        super(refModelName, entityName, conceptName);
        _customer = Context.getInstance().getCustomer();
    }

    /**
     * Construct a new <code>PatientQuery</code> that queries IMObjects with the
     * specified short names, and using the current customer, if set.
     *
     * @param shortNames the patient archetype short names
     */
    public PatientQuery(String[] shortNames) {
        this(shortNames, Context.getInstance().getCustomer());
    }

    /**
     * Construct a new <code>PatientQuery</code> that queries IMObjects with the
     * specified short names, and customer.
     *
     * @param shortNames the patient archetype short names
     * @param customer   the customer. May be <code>null</code>
     */
    public PatientQuery(String[] shortNames, Party customer) {
        super(shortNames);
        _customer = customer;
    }

    /**
     * Performs the query.
     *
     * @param rows      the maxiomum no. of rows per page
     * @param node      the node to sort on. May be <code>null</code>
     * @param ascending if <code>true</code> sort the rows inascending order;
     *                  otherwise sort them in <code>descebding</code> order
     * @return the query result set
     */
    @Override
    public ResultSet query(int rows, String node, boolean ascending) {
        getComponent();  // ensure the component is rendered
        ResultSet result;
        if (_allPatients.isSelected()) {
            result = super.query(rows, node, ascending);
        } else {
            List<IMObject> objects = null;
            if (_customer != null) {
                objects = filterForCustomer();
            }
            if (objects == null) {
                objects = Collections.emptyList();
            }
            result = new PreloadedResultSet<IMObject>(objects, rows);
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
        return (_customer != null);
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
        addAllPatients(container);
        addInactive(container);
        ApplicationInstance.getActive().setFocusedComponent(getInstanceName());
    }

    /**
     * Adds the inactive checkbox to a container.
     *
     * @param container the container
     */
    protected void addAllPatients(Component container) {
        _allPatients = new CheckBox();
        boolean selected = (_customer == null);
        _allPatients.setSelected(selected);
        Label allPatients = LabelFactory.create(ALL_PATIENTS_ID);
        container.add(allPatients);
        container.add(_allPatients);
    }

    /**
     * Filter patients associated with a customer.
     *
     * @return a list of patients associated with the customer that matches the
     *         specified criteria
     */
    private List<IMObject> filterForCustomer() {
        List<IMObject> result = null;
        List<IMObject> patients = getPatients(_customer);
        String type = getShortName();
        String name = getName();
        boolean activeOnly = !includeInactive();

        if (type == null || type.equals(ArchetypeShortNameListModel.ALL)) {
            for (String shortName : getShortNames()) {
                List<IMObject> matches
                        = filter(patients, shortName, name, activeOnly);
                if (result == null) {
                    result = matches;
                } else {
                    result.addAll(matches);
                }
            }
        } else {
            result = filter(patients, type, name, activeOnly);
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
    private List<IMObject> filter(List<IMObject> objects, String shortName,
                                  String name, boolean activeOnly) {
        final String wildcard = "*";
        List<IMObject> result = new ArrayList<IMObject>();
        for (IMObject object : objects) {
            ArchetypeId id = object.getArchetypeId();
            if (!DescriptorHelper.matches(id.getShortName(), shortName)) {
                continue;
            }
            if (!StringUtils.isEmpty(name)) {
                if (name.startsWith(wildcard) || name.endsWith(wildcard)) {
                    name = StringUtils.strip(name, wildcard);
                }
                if (StringUtils.indexOf(object.getName(), name) == -1) {
                    continue;
                }
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
    private List<IMObject> getPatients(Party customer) {
        List<IMObject> result = new ArrayList<IMObject>();
        Set<EntityRelationship> relationships
                = customer.getEntityRelationships();
        IMObjectReference source = new IMObjectReference(customer);

        for (EntityRelationship relationship : relationships) {
            if (IMObjectHelper.isA(relationship,
                                   "entityRelationship.patientOwner")) {
                if (source.equals(relationship.getSource())) {
                    IMObject object = IMObjectHelper.getObject(
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
