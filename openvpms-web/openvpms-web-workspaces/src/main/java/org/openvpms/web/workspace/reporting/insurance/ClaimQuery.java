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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.insurance;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.GridLayoutData;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.workflow.WorkflowStatus;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.model.lookup.Lookup;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.clinician.ClinicianSelectField;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.location.LocationSelectField;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.NodeLookupQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.select.IMObjectSelector;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.text.TextField;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

import static org.openvpms.archetype.rules.insurance.InsuranceArchetypes.CLAIM;
import static org.openvpms.archetype.rules.insurance.InsuranceArchetypes.POLICY;


/**
 * Query for insurance claims.
 *
 * @author Tim Anderson
 */
public class ClaimQuery extends DateRangeActQuery<Act> {

    /**
     * The act short names.
     */
    public static final String[] ARCHETYPES = new String[]{CLAIM};

    /**
     * Dummy not submitted status. Finds all claims that are PENDING or FINALISED status.
     */
    public static final String NOT_SUBMITTED = "NOT_SUBMITTED";

    /**
     * The location selector.
     */
    private final LocationSelectField locationSelector;

    /**
     * The insurer selector.
     */
    private final IMObjectSelector insurerSelector;

    /**
     * The clinician selector.
     */
    private final SelectField clinicianSelector;

    /**
     * The result status dropdown.
     */
    private LookupField gapStatusSelector;

    /**
     * Dummy incomplete status, used in the result status selector.
     */
    private static Lookup NOT_SUBMITTED_STATUS = new org.openvpms.component.business.domain.im.lookup.Lookup(
            new ArchetypeId("lookup.local"), NOT_SUBMITTED, Messages.get("insurance.status.notsubmitted"));

    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT = {new NodeSortConstraint("startTime", true)};

    /**
     * The act statuses to query..
     */
    private static final ActStatuses STATUSES = new ActStatuses(new StatusLookupQuery());

    /**
     * The gap statuses to query.
     */
    private static final ActStatuses GAP_STATUSES = new ActStatuses(new NodeLookupQuery(CLAIM, "status2"));


    /**
     * Constructs a {@link ClaimQuery}.
     *
     * @param context the layout context
     */
    public ClaimQuery(LayoutContext context) {
        super(null, null, null, ARCHETYPES, STATUSES, Act.class);
        setDefaultSortConstraint(DEFAULT_SORT);
        setAuto(true);

        locationSelector = createLocationSelector(context.getContext());
        clinicianSelector = createClinicianSelector();
        insurerSelector = new IMObjectSelector<Party>(DescriptorHelper.getDisplayName(POLICY, "insurer"), context,
                                                      SupplierArchetypes.INSURER);
        gapStatusSelector = LookupFieldFactory.create(GAP_STATUSES, true);
    }

    /**
     * Returns the selected practice location.
     *
     * @return the selected location. May be {@code null}
     */
    public Party getLocation() {
        return (Party) locationSelector.getSelectedItem();
    }

    /**
     * Returns the claim status.
     * <p>
     * The status {@code NOT_SUBMITTED} finds claims with {@code PENDING} or {@code POSTED} status.
     *
     * @return the claim status. May be {@code null}
     */
    public String getStatus() {
        LookupField statusSelector = getStatusSelector();
        return statusSelector != null ? statusSelector.getSelectedCode() : null;
    }

    /**
     * Returns the gap status.
     *
     * @return the gap status. May be {@code null}
     */
    public String getGapStatus() {
        return gapStatusSelector.getSelectedCode();
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician. May be {@code null}
     */
    public void setClinician(User clinician) {
        this.clinicianSelector.setSelectedItem(clinician);
    }

    /**
     * Sets the gap status to filter on.
     * <p>
     * The {@link #NOT_SUBMITTED} result status code can be used to return all incomplete investigations that haven't
     * been cancelled nor reviewed.
     *
     * @param status the result status, or {@code null} to include all result statuses
     */
    public void setGapStatus(String status) {
        gapStatusSelector.setSelected(status);
    }

    /**
     * Returns the preferred height of the query when rendered.
     *
     * @return the preferred height, or {@code null} if it has no preferred height
     */
    @Override
    public Extent getHeight() {
        return getHeight(3);
    }

    /**
     * Returns the selected insurer.
     *
     * @return the selected insurer. May be {@code null}
     */
    public Party getInsurer() {
        return (Party) insurerSelector.getObject();
    }

    /**
     * Returns the selected clinician.
     *
     * @return the selected clinician. May be {@code null}
     */
    public User getClinician() {
        return (User) clinicianSelector.getSelectedItem();
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
        addStatusSelector(container);
        addInsurer(container);
        addDateRange(container);
        addGapStatusSelector(container);
        addLocation(container);
        addClinician(container);
    }

    /**
     * Adds the search field to a container.
     *
     * @param container the container
     */
    @Override
    protected void addSearchField(Component container) {
        Label label = LabelFactory.create();
        label.setText(DescriptorHelper.getDisplayName(CLAIM, "id"));
        container.add(label);
        TextField field = getSearchField();
        container.add(field);
        getFocusGroup().add(field);
    }

    /**
     * Adds a result status selector to the container.
     *
     * @param container the container
     */
    protected void addGapStatusSelector(Component container) {
        Label label = LabelFactory.create();
        label.setText(DescriptorHelper.getDisplayName(CLAIM, "status2"));
        container.add(label);
        container.add(gapStatusSelector);
        getFocusGroup().add(gapStatusSelector);
    }

    /**
     * Creates a new result set.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<Act> createResultSet(SortConstraint[] sort) {
        Lookup selected = getStatusSelector().getSelected();
        String[] statuses;
        if (selected == NOT_SUBMITTED_STATUS) {
            statuses = new String[]{WorkflowStatus.PENDING, WorkflowStatus.POSTED};
        } else {
            statuses = super.getStatuses();
        }
        Party location = getLocation();
        User clinician = (User) clinicianSelector.getSelectedItem();
        Party insurer = (Party) insurerSelector.getObject();
        String gapStatus = gapStatusSelector.getSelectedCode();
        return new ClaimResultSet(getArchetypeConstraint(), getValue(), location, insurer, clinician, getFrom(),
                                  getTo(), statuses, gapStatus, getMaxResults(), sort);
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
     * Adds the clinician selector to a container.
     *
     * @param container the container
     */
    private void addClinician(Component container) {
        Label label = LabelFactory.create();
        label.setText(Messages.get("label.clinician"));
        container.add(label);
        container.add(clinicianSelector);
        getFocusGroup().add(clinicianSelector);
    }

    /**
     * Adds the location selector to a container.
     *
     * @param container the container
     */
    private void addLocation(Component container) {
        Label label = LabelFactory.create();
        label.setText(DescriptorHelper.getDisplayName(CLAIM, "location"));
        container.add(label);
        container.add(locationSelector);
        getFocusGroup().add(locationSelector);
    }

    /**
     * Adds the insurer selector to a container.
     *
     * @param container the container
     */
    private void addInsurer(Component container) {
        Label label = LabelFactory.create();
        label.setText(insurerSelector.getType());
        container.add(label);
        Component component = insurerSelector.getComponent();
        container.add(component);
        getFocusGroup().add(component);
    }

    /**
     * Creates a field to select the location.
     *
     * @param context the context
     * @return a new selector
     */
    private LocationSelectField createLocationSelector(Context context) {
        LocationSelectField result = new LocationSelectField(context.getUser(), context.getPractice(), true);
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
        SelectField result = new ClinicianSelectField();
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
        StatusLookupQuery() {
            super(CLAIM, "status");
        }

        /**
         * Returns the default lookup.
         *
         * @return {@link #NOT_SUBMITTED_STATUS}
         */
        @Override
        public Lookup getDefault() {
            return NOT_SUBMITTED_STATUS;
        }

        /**
         * Returns the lookups.
         *
         * @return the lookups
         */
        @Override
        public List<Lookup> getLookups() {
            List<Lookup> lookups = super.getLookups();
            lookups.add(0, NOT_SUBMITTED_STATUS);
            return lookups;
        }
    }

}
