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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.history;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.comparators.TransformingComparator;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.SelectionHistory;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.query.AbstractFilteredResultSet;
import org.openvpms.web.component.im.query.AbstractQuery;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.TextComponentFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Browser for customer and patient selection history.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerPatientHistoryQuery extends AbstractQuery<CustomerPatient> {

    /**
     * The history to query.
     */
    private final List<CustomerPatient> history;

    /**
     * The query component.
     */
    private Component component;

    /**
     * Field to filter the history.
     */
    private TextField filter;

    /**
     * The focus group.
     */
    private final FocusGroup focusGroup = new FocusGroup(getClass().getName());

    /**
     * The archetype short names for this query.
     */
    private static final String[] SHORT_NAMES = {"party.customer*", "party.patientpet"};


    /**
     * Construct a new <tt>AbstractQuery</tt> that queries objects with
     * the specified primary short names.
     *
     * @param customers customer selection history
     * @param patients  patient selection history
     * @throws org.openvpms.component.system.common.query.ArchetypeQueryException
     *          if the short names don't match any archetypes
     */
    public CustomerPatientHistoryQuery(SelectionHistory customers, SelectionHistory patients) {
        super(SHORT_NAMES, CustomerPatient.class);
        setAuto(true);
        history = getHistory(customers, patients);
        filter = TextComponentFactory.create();
        filter.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (component == null) {
            component = RowFactory.create("ControlRow");
            Label nameLabel = LabelFactory.create("query.search");
            component.add(nameLabel);
            component.add(filter);
            focusGroup.add(filter);
        }
        return component;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set. May be <tt>null</tt>
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          if the query fails
     */
    public ResultSet<CustomerPatient> query(SortConstraint[] sort) {
        CustomerPatientResultSet resultSet = new CustomerPatientResultSet(history, getMaxResults());
        resultSet.sort(sort);
        String text = filter.getText();
        return (!StringUtils.isEmpty(text)) ? new FilteredResultSet(resultSet, text) : resultSet;
    }

    /**
     * Returns the focus group for the component.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Sets the name filter.
     * <p/>
     * Selections containing the filter will be included in the results.
     *
     * @param filter the name filter
     */
    public void setValue(String filter) {
        this.filter.setText(filter);
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return <tt>false</tt>
     */
    public boolean selects(IMObjectReference reference) {
        return false;
    }

    /**
     * Returns a list of {@link CustomerPatient} instances representing the selected customers and their corresponding
     * patients, ordered on most recent selection first.
     *
     * @param customers the customer selection history
     * @param patients  the patient selection history
     * @return a list of customer/patient pairs
     */
    private List<CustomerPatient> getHistory(SelectionHistory customers, SelectionHistory patients) {
        List<CustomerPatient> result = new ArrayList<CustomerPatient>();
        PatientRules rules = new PatientRules();
        Set<SelectionHistory.Selection> allCustomers
                = new HashSet<SelectionHistory.Selection>(customers.getSelections());
        Set<SelectionHistory.Selection> allPatients
                = new HashSet<SelectionHistory.Selection>(patients.getSelections());
        for (SelectionHistory.Selection selection
                : allPatients.toArray(new SelectionHistory.Selection[allPatients.size()])) {
            Party patient = (Party) selection.getObject();
            if (patient != null) {
                IMObjectReference customerRef = rules.getOwnerReference(patient);
                Party customer = (Party) IMObjectHelper.getObject(customerRef);
                Date patientSelect = selection.getTime();
                Date customerSelect = (customer != null) ? customers.getSelected(customer) : null;
                Date selected;
                if (customerSelect != null) {
                    selected = (Date) ComparatorUtils.max(customerSelect, patientSelect, null);
                } else {
                    selected = patientSelect;
                }
                CustomerPatient pair = new CustomerPatient(customer, patient, selected);
                result.add(pair);
                if (customer != null) {
                    allCustomers.remove(new SelectionHistory.Selection(customerRef));
                }
            }
            allPatients.remove(selection);
        }

        for (SelectionHistory.Selection selection : allCustomers) {
            Party customer = (Party) selection.getObject();
            if (customer != null) {
                result.add(new CustomerPatient(customer, null, selection.getTime()));
            }
        }

        for (SelectionHistory.Selection selection : allPatients) {
            Party patient = (Party) selection.getObject();
            if (patient != null) {
                result.add(new CustomerPatient(null, patient, selection.getTime()));
            }
        }
        Collections.sort(result, new Comparator<CustomerPatient>() {
            public int compare(CustomerPatient o1, CustomerPatient o2) {
                return -o1.getSelected().compareTo(o2.getSelected());
            }
        });
        return result;
    }

    /**
     * Transformer that extracts the name of the customer or patient from an {@link CustomerPatient}.
     */
    private static class NameTransformer implements Transformer {

        /**
         * If <tt>true</tt> return the customer name, otherwise return the patient name.
         */
        private boolean customer;

        /**
         * Creates a new <tt>NameTransformer</tt>.
         *
         * @param customer if <tt>true</tt> return the customer name, otherwise return the patient name.
         */
        public NameTransformer(boolean customer) {
            this.customer = customer;
        }

        /**
         * Transforms the input object (leaving it unchanged) into some output object.
         *
         * @param input the object to be transformed, should be left unchanged
         * @return a transformed object
         * @throws ClassCastException       (runtime) if the input is the wrong class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws org.apache.commons.collections.FunctorException
         *                                  (runtime) if the transform cannot be completed
         */
        public Object transform(Object input) {
            CustomerPatient selection = (CustomerPatient) input;
            Party party = (customer) ? selection.getCustomer() : selection.getPatient();
            return (party != null) ? party.getName() : null;
        }
    }

    /**
     * Result set that provides sorting on customer and patient name.
     */
    private static class CustomerPatientResultSet extends ListResultSet<CustomerPatient> {

        /**
         * Constructs a new <tt>CustomerPatientResultSet</tt>.
         *
         * @param objects  the objects
         * @param pageSize the maximum no. of results per page
         */
        public CustomerPatientResultSet(List<CustomerPatient> objects, int pageSize) {
            super(objects, pageSize);
        }

        /**
         * This resets the iterator but does not do any sorting.
         *
         * @param sort the sort criteria. May be <tt>null</tt>
         */
        @Override
        public void sort(SortConstraint[] sort) {
            if (sort != null && sort.length > 0 && sort[0] instanceof NodeSortConstraint) {
                sort((NodeSortConstraint) sort[0]);
            }
            super.sort(sort);
        }

        @SuppressWarnings("unchecked")
        private void sort(NodeSortConstraint sort) {
            Comparator comparator = IMObjectSorter.getComparator(sort.isAscending());
            boolean customer = sort.getNodeName().equals("customer");
            TransformingComparator tc = new TransformingComparator(new NameTransformer(customer), comparator);
            Collections.sort(getObjects(), tc);
        }
    }

    /**
     * Result set that returns {@link CustomerPatient} instances that have a name or description matching the supplied
     * text. Matching is case-insensitive.
     */
    private static class FilteredResultSet extends AbstractFilteredResultSet<CustomerPatient> {

        /**
         * The text to match on.
         */
        private final String match;

        /**
         * Creates a new <tt>FilteredResultSet</tt>.
         *
         * @param set  the result set to filter
         * @param text the text to match on
         */
        public FilteredResultSet(ResultSet<CustomerPatient> set, String text) {
            super(set);
            this.match = text.toLowerCase();
        }

        /**
         * Determines if an object should be included in the result set.
         *
         * @param object  the object
         * @param results the result set to add included objects to
         */
        protected void filter(CustomerPatient object, List<CustomerPatient> results) {
            if (matches(object.getCustomer()) || matches(object.getPatient())) {
                results.add(object);
            }
        }

        /**
         * Determines if a party matches the specified text.
         *
         * @param party the party. May be <tt>null</tt>
         * @return <tt>true</tt> if there is a match, otherwise <tt>false</tt>
         */
        private boolean matches(Party party) {
            return party != null && (matches(party.getName()) || (matches(party.getDescription())));
        }

        /**
         * Determines if a string matches the specified text.
         *
         * @param string the string to check. May be <tt>null</tt>
         * @return <tt>true</tt> if there is a match, otherwise <tt>false</tt>
         */
        private boolean matches(String string) {
            return string != null && string.toLowerCase().contains(match);
        }

    }
}
