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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.investigation;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.list.IMObjectListModel;
import org.openvpms.web.component.im.location.LocationSelectField;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.select.AbstractIMObjectSelectorListener;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.openvpms.archetype.rules.patient.InvestigationArchetypes.INVESTIGATION_TYPE;
import static org.openvpms.archetype.rules.patient.InvestigationArchetypes.PATIENT_INVESTIGATION;


/**
 * Query for patient investigations.
 *
 * @author Tim Anderson
 */
public class InvestigationsQuery extends DateRangeActQuery<Act> {

    /**
     * The act short names.
     */
    public static final String[] SHORT_NAMES = new String[]{InvestigationArchetypes.PATIENT_INVESTIGATION};

    /**
     * The location selector.
     */
    private final SelectField location;

    /**
     * The clinician selector.
     */
    private final SelectField clinician;


    /**
     * The investigation type selector.
     */
    private final IMObjectSelector<Entity> investigationType;


    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT = {new NodeSortConstraint("startTime", false)};

    /**
     * The act statuses to query..
     */
    private static final ActStatuses STATUSES = new ActStatuses(new StatusLookupQuery(), null);

    /**
     * Dummy incomplete status, used in the status selector.
     */
    private static Lookup INCOMPLETE_STATUS = new Lookup(new ArchetypeId("lookup.local"), "INCOMPLETE",
                                                         Messages.get("investigation.incomplete"));

    /**
     * The incomplete investigation statuses.
     */
    private static final String[] INCOMPLETE_CODES = {"IN_PROGRESS", "RECEIVED", "PRELIMINARY", "FINAL"};


    /**
     * Constructs an {@link InvestigationsQuery}.
     *
     * @param context the context
     */
    public InvestigationsQuery(LayoutContext context) {
        super(null, null, null, SHORT_NAMES, STATUSES, Act.class);
        setDefaultSortConstraint(DEFAULT_SORT);
        setAuto(true);

        setStatus(INCOMPLETE_STATUS.getCode());
        // TODO - shouldn't need this as its returned by StatusLookupQuery, but addStatusSelector() ignores it

        location = createLocationSelector(context.getContext());
        clinician = createClinicianSelector();
        investigationType = createInvestigationTypeSelector(context);
    }

    /**
     * Returns the act statuses to query.
     *
     * @return the act statuses to query
     */
    @Override
    protected String[] getStatuses() {
        String[] statuses = super.getStatuses();
        if (statuses.length == 1 && statuses[0].equals(INCOMPLETE_STATUS.getCode())) {
            statuses = INCOMPLETE_CODES;
        }
        return statuses;
    }

    /**
     * Creates a container component to lay out the query component in.
     *
     * @return a new container
     * @see #doLayout(Component)
     */
    @Override
    protected Component createContainer() {
        return GridFactory.create(6);
    }

    /**
     * Lays out the component in a container.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        addSearchField(container);
        super.doLayout(container);
        addLocation(container);
        addClinician(container);
        addInvestigationType(container);
        container.add(container);
        container.add(container);
    }

    /**
     * Invoked when a status is selected.
     */
    @Override
    protected void onStatusChanged() {
        super.onStatusChanged();
        onQuery();
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<Act> createResultSet(SortConstraint[] sort) {
        List<ParticipantConstraint> list = new ArrayList<ParticipantConstraint>();
        ParticipantConstraint supplier = getParticipantConstraint();
        if (supplier != null) {
            list.add(supplier);
        }
        addParticipantConstraint(list, "investigationType", InvestigationArchetypes.INVESTIGATION_TYPE_PARTICIPATION,
                                 investigationType.getObject());
        addParticipantConstraint(list, "clinician", UserArchetypes.CLINICIAN_PARTICIPATION,
                                 (Entity) clinician.getSelectedItem());
        addParticipantConstraint(list, "location", "participation.location", (Entity) location.getSelectedItem());
        ParticipantConstraint[] participants = list.toArray(new ParticipantConstraint[list.size()]);

        return new InvestigationResultSet(getArchetypeConstraint(), getValue(), participants, getFrom(), getTo(),
                                          getStatuses(), getMaxResults(), sort);
    }

    /**
     * Adds a date range to the container.
     *
     * @param container the container
     */
    @Override
    protected void addDateRange(final Component container) {
        super.addDateRange(container);
        GridLayoutData layoutData = new GridLayoutData();
        layoutData.setColumnSpan(2);
        getDateRange().getComponent().setLayoutData(layoutData);

    }

    /**
     * Adds the investigation selector to a container.
     *
     * @param container the container
     */
    private void addInvestigationType(Component container) {
        Label label = LabelFactory.create();
        label.setText(investigationType.getType());
        container.add(label);
        Component component = investigationType.getComponent();
        GridLayoutData layoutData = new GridLayoutData();
        layoutData.setColumnSpan(3);
        component.setLayoutData(layoutData);
        container.add(component);
        investigationType.getSelect().setFocusTraversalParticipant(false);
        getFocusGroup().add(investigationType.getTextField());
    }

    /**
     * Creates a field to select the investigation type.
     *
     * @param context the layout context
     * @return a new selector
     */
    private IMObjectSelector<Entity> createInvestigationTypeSelector(LayoutContext context) {
        IMObjectSelector<Entity> selector = new IMObjectSelector<Entity>(
                DescriptorHelper.getDisplayName(INVESTIGATION_TYPE), context, INVESTIGATION_TYPE);
        AbstractIMObjectSelectorListener<Entity> listener = new AbstractIMObjectSelectorListener<Entity>() {
            public void selected(Entity object) {
                onQuery();
            }
        };
        selector.setListener(listener);
        return selector;
    }

    /**
     * Helper to create and add a participant constraint to the supplied constraints, if {@code entity} is non-null.
     *
     * @param constraints the constraints to add to
     * @param name        rhe participation node name
     * @param shortName   the participation archetype short name
     * @param object      the entity. May be {@code null}
     */
    private void addParticipantConstraint(List<ParticipantConstraint> constraints, String name,
                                          String shortName, Entity object) {
        if (object != null) {
            constraints.add(new ParticipantConstraint(name, shortName, object));
        }
    }

    /**
     * Adds the clinician selector to a container.
     *
     * @param container the container
     */
    private void addClinician(Component container) {
        Label label = LabelFactory.create();
        label.setText(Messages.get("label.clinician"));
        container.add(label);
        container.add(clinician);
        getFocusGroup().add(clinician);
    }

    /**
     * Adds the location selector to a container.
     *
     * @param container the container
     */
    private void addLocation(Component container) {
        Label label = LabelFactory.create();
        label.setText(DescriptorHelper.getDisplayName(PATIENT_INVESTIGATION, "location"));
        container.add(label);
        container.add(location);
        getFocusGroup().add(location);
    }

    /**
     * Creates a field to select the location.
     *
     * @param context the context
     * @return a new selector
     */
    private SelectField createLocationSelector(Context context) {
        LocationSelectField result = new LocationSelectField(context.getUser(), context.getPractice());
        result.setSelectedItem(context.getLocation());
        result.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });
        return result;
    }


    /**
     * Creates a new dropdown to select clinicians.
     *
     * @return a new clinician selector
     */
    private SelectField createClinicianSelector() {
        UserRules rules = ServiceHelper.getBean(UserRules.class);
        List<IMObject> clinicians = new ArrayList<IMObject>();
        ArchetypeQuery query = new ArchetypeQuery(UserArchetypes.USER, true, true);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        Iterator<User> iter = new IMObjectQueryIterator<User>(query);
        while (iter.hasNext()) {
            User user = iter.next();
            if (rules.isClinician(user)) {
                clinicians.add(user);
            }
        }
        IMObjectListModel model = new IMObjectListModel(clinicians, true, false);
        SelectField result = SelectFieldFactory.create(model);
        result.setCellRenderer(IMObjectListCellRenderer.NAME);

        result.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });

        return result;
    }

    private static class StatusLookupQuery extends NodeLookupQuery {

        /**
         * Constructs an {@link StatusLookupQuery}.
         */
        public StatusLookupQuery() {
            super(InvestigationArchetypes.PATIENT_INVESTIGATION, "status");
        }

        /**
         * Returns the default lookup.
         *
         * @return {@link #INCOMPLETE_STATUS}
         */
        @Override
        public Lookup getDefault() {
            return INCOMPLETE_STATUS;
        }

        /**
         * Returns the lookups.
         *
         * @return the lookups
         */
        @Override
        public List<Lookup> getLookups() {
            List<Lookup> lookups = super.getLookups();
            lookups.add(0, INCOMPLETE_STATUS);
            return lookups;
        }
    }

}
