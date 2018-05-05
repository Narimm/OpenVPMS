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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.organisation;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.IMObjectTabPaneModel;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.subscription.SubscriptionHelper;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * Layout strategy for <em>party.organisationPractice</em>.
 *
 * @author Tim Anderson
 */
public class PracticeLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The subscription component.
     */
    private ComponentState subscription;

    /**
     * Prescription expiry units node.
     */
    private static final String PRESCRIPTION_EXPIRY_UNITS = "prescriptionExpiryUnits";

    /**
     * Record lock period units.
     */
    private static final String RECORD_LOCK_PERIOD_UNITS = "recordLockPeriodUnits";

    /**
     * Minimum quantities override.
     */
    private static final String MINIMUM_QUANTITIES_OVERRIDE = "minimumQuantitiesOverride";

    /**
     * The archetype nodes. This excludes nodes rendered alongside others.
     */
    private static final ArchetypeNodes NODES = new ArchetypeNodes().exclude(PRESCRIPTION_EXPIRY_UNITS,
                                                                             RECORD_LOCK_PERIOD_UNITS,
                                                                             MINIMUM_QUANTITIES_OVERRIDE);

    /**
     * Default constructor.
     */
    public PracticeLayoutStrategy() {
        super(NODES);
    }

    /**
     * Constructs a {@link PracticeLayoutStrategy}.
     *
     * @param subscription the component representing the subscription
     * @param focusGroup   the subscription component's focus group. May be {@code null}
     */
    public PracticeLayoutStrategy(Component subscription, FocusGroup focusGroup) {
        super(NODES);
        this.subscription = new ComponentState(subscription, focusGroup);
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        IMObjectComponentFactory factory = context.getComponentFactory();
        addPrescriptionExpiry(object, properties, factory);
        addAutoLockScreen(object, properties, factory);
        addAutoLogout(object, properties, factory);
        addRecordLockPeriod(object, properties, factory);
        addMinimumQuantities(object, properties, factory);
        if (subscription == null) {
            IArchetypeService service = ServiceHelper.getArchetypeService();
            Participation participation = SubscriptionHelper.getSubscriptionParticipation((Party) object, service);
            SubscriptionViewer viewer = new SubscriptionViewer(context);
            if (participation != null) {
                DocumentAct act = (DocumentAct) context.getCache().get(participation.getAct());
                viewer.setSubscription(act);
            }
            subscription = new ComponentState(viewer.getComponent());
        }
        return super.apply(object, properties, parent, context);
    }

    /**
     * Lays out child components in a tab model.
     *
     * @param object     the parent object
     * @param properties the properties
     * @param model      the tab model
     * @param context    the layout context
     * @param shortcuts  if {@code true} include short cuts
     */
    @Override
    protected void doTabLayout(IMObject object, List<Property> properties,
                               IMObjectTabPaneModel model, LayoutContext context, boolean shortcuts) {
        super.doTabLayout(object, properties, model, context, shortcuts);
        Component inset = ColumnFactory.create("Inset", subscription.getComponent());

        String label = Messages.get("admin.practice.subscription");
        if (shortcuts && model.size() < 10) {
            label = getShortcut(label, model.size() + 1);
        }
        model.addTab(label, inset);
    }

    /**
     * Registers a component to render the expiry period and units.
     *
     * @param object     the practice object
     * @param properties the properties
     * @param factory    the component factory
     */
    private void addPrescriptionExpiry(IMObject object, PropertySet properties, IMObjectComponentFactory factory) {
        addPeriod(object, "prescriptionExpiryPeriod", PRESCRIPTION_EXPIRY_UNITS, properties, factory);
    }

    /**
     * Registers a component to render the medical record lock period and units.
     *
     * @param object     the practice object
     * @param properties the properties
     * @param factory    the component factory
     */
    private void addRecordLockPeriod(IMObject object, PropertySet properties, IMObjectComponentFactory factory) {
        addPeriod(object, "recordLockPeriod", RECORD_LOCK_PERIOD_UNITS, properties, factory);
    }

    /**
     * Registers a component to render a period and its associated units.
     *
     * @param object     the practice object
     * @param periodName the period node name
     * @param unitsName  the units node name
     * @param properties the properties
     * @param factory    the component factory
     */
    private void addPeriod(IMObject object, String periodName, String unitsName, PropertySet properties,
                           IMObjectComponentFactory factory) {
        Property period = properties.get(periodName);
        Property units = properties.get(unitsName);

        ComponentState periodComponent = factory.create(period, object);
        ComponentState unitsComponent = factory.create(units, object);
        Row row = RowFactory.create(Styles.CELL_SPACING, periodComponent.getComponent(), unitsComponent.getComponent());
        FocusGroup group = new FocusGroup(periodName);
        group.add(periodComponent.getComponent());
        group.add(unitsComponent.getComponent());
        addComponent(new ComponentState(row, period, group));
    }

    /**
     * Registers a component to render the session auto-lock screen and minutes.
     *
     * @param object     the practice object
     * @param properties the properties
     * @param factory    the component factory
     */
    private void addAutoLockScreen(IMObject object, PropertySet properties, IMObjectComponentFactory factory) {
        addInterval("autoLockScreen", object, properties, factory);
    }

    /**
     * Registers a component to render the session auto-logout and minutes.
     *
     * @param object     the practice object
     * @param properties the properties
     * @param factory    the component factory
     */
    private void addAutoLogout(IMObject object, PropertySet properties, IMObjectComponentFactory factory) {
        addInterval("autoLogout", object, properties, factory);
    }

    /**
     * Add an interval property.
     *
     * @param name       the property name
     * @param object     the practice object
     * @param properties the properties
     * @param factory    the component factory
     */
    private void addInterval(String name, IMObject object, PropertySet properties, IMObjectComponentFactory factory) {
        Property property = properties.get(name);
        ComponentState state = factory.create(property, object);
        Row row = RowFactory.create(Styles.CELL_SPACING, state.getComponent(),
                                    LabelFactory.create("admin.practice.minutes"));
        addComponent(new ComponentState(row, property));
    }

    /**
     * Registers a component to render the minimum quantities and overrides properties.
     *
     * @param object     the practice object
     * @param properties the properties
     * @param factory    the component factory
     */
    private void addMinimumQuantities(IMObject object, PropertySet properties, IMObjectComponentFactory factory) {
        Property minimumQuantities = properties.get("minimumQuantities");
        Property minimumQuantitiesOverride = properties.get(MINIMUM_QUANTITIES_OVERRIDE);

        ComponentState quantities = factory.create(minimumQuantities, object);
        ComponentState override = factory.create(minimumQuantitiesOverride, object);
        Row row = RowFactory.create(Styles.CELL_SPACING, quantities.getComponent(), override.getLabel(),
                                    override.getComponent());
        FocusGroup group = new FocusGroup(minimumQuantities.getName());
        group.add(quantities.getComponent());
        group.add(override.getComponent());
        addComponent(new ComponentState(row, minimumQuantities, group));
    }

}
