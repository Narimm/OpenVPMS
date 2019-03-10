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

package org.openvpms.web.component.alert;

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.rules.finance.account.AccountType;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.table.TableHelper;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * A layout strategy for active alerts.
 * <p>
 * This excludes the status and the startTime, endTime, reason, product,  author, clinician and notes, if empty.
 *
 * @author Tim Anderson
 */
public class ActiveAlertLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Optional account type to display.
     */
    private final AccountType accountType;

    /**
     * Constructs an {@link ActiveAlertLayoutStrategy}.
     */
    public ActiveAlertLayoutStrategy() {
        this(null);
    }

    /**
     * Constructs an {@link ActiveAlertLayoutStrategy}.
     *
     * @param accountType the account type associated with an alert. May be {@code null}
     */
    public ActiveAlertLayoutStrategy(AccountType accountType) {
        this.accountType = accountType;
    }

    /**
     * Apply the layout strategy.
     * <p>
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
        Property notes = properties.get("notes");
        addComponent(createComponent(notes, null, new Extent(15, Extent.EM), object, context));
        return super.apply(object, properties, parent, context);
    }

    /**
     * Returns {@link ArchetypeNodes} to determine which nodes will be displayed.
     *
     * @param object  the object to display
     * @param context the layout context
     * @return the archetype nodes
     */
    @Override
    protected ArchetypeNodes getArchetypeNodes(IMObject object, LayoutContext context) {
        ArchetypeNodes nodes = ArchetypeNodes.allSimple().exclude("status")
                .excludeIfEmpty("startTime", "endTime", "reason", "product", "author", "clinician", "notes");
        String node = object.isA(PatientArchetypes.ALERT) ? "patient" : "customer";
        nodes.simple(node).first(node);
        return nodes;
    }

    /**
     * Lays out components in a grid.
     *
     * @param object     the object to lay out
     * @param properties the properties
     * @param context    the layout context
     */
    @Override
    protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context) {
        ComponentGrid grid = createGrid(object, properties, context, 1);
        if (accountType != null) {
            Label label = LabelFactory.create();
            label.setText(Messages.format("alert.accounttype", accountType.getName()));
            grid.add(TableHelper.createSpacer());
            grid.add(label, 2);
        }
        return grid;
    }

}
