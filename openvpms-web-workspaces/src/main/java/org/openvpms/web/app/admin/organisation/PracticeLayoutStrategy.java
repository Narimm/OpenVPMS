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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.admin.organisation;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.tabpane.TabPaneModel;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.resource.subscription.SubscriptionHelper;
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
     * Constructs a <tt>PracticeLayoutStrategy</tt>.
     */
    public PracticeLayoutStrategy() {
    }

    /**
     * Constructs a new <tt>PracticeLayoutStrategy</tt>.
     *
     * @param subscription the component representing the subscription
     * @param focusGroup   the subscription component's focus group. May be <tt>null</tt>
     */
    public PracticeLayoutStrategy(Component subscription, FocusGroup focusGroup) {
        this.subscription = new ComponentState(subscription, focusGroup);
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <tt>Component</tt>, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <tt>null</tt>
     * @param context    the layout context
     * @return the component containing the rendered <tt>object</tt>
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
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
     * @param object      the parent object
     * @param descriptors the property descriptors
     * @param properties  the properties
     * @param model       the tab model
     * @param context     the layout context
     * @param shortcuts   if <tt>true</tt> include short cuts
     */
    @Override
    protected void doTabLayout(IMObject object, List<NodeDescriptor> descriptors, PropertySet properties,
                               TabPaneModel model, LayoutContext context, boolean shortcuts) {
        super.doTabLayout(object, descriptors, properties, model, context, shortcuts);
        Component inset = ColumnFactory.create("Inset", subscription.getComponent());

        String label = Messages.get("admin.practice.subscription");
        if (shortcuts && model.size() < 10) {
            label = getShortcut(label, model.size() + 1);
        }
        model.addTab(label, inset);
    }
}
