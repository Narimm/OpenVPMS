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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.sms;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.SplitPaneFactory;

/**
 * Layout strategy for <em>entity.SMSConfigEmail*</em>.
 * <p/>
 * This includes rendering of an {@link EmailSMSSampler}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SMSConfigEmailLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * The sampler. May be <tt>null</tt>
     */
    private EmailSMSSampler sampler;

    /**
     * Sets the sampler.
     *
     * @param sampler the sampler. May be <tt>null</tt>
     */
    public void setSampler(EmailSMSSampler sampler) {
        this.sampler = sampler;
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders the component and includes the EmailSMSSampler, if one is provided.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be <tt>null</tt>
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    @Override
    public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
        ComponentState state = super.apply(object, properties, parent, context);
        if (sampler != null) {
            Component container = createSplitPane();
            container.add(state.getComponent());
            container.add(sampler.getComponent());
            FocusGroup group = new FocusGroup("SMSConfigEmail");
            group.add(state.getFocusGroup());
            group.add(sampler.getFocusGroup());
            state = new ComponentState(container, group);
        }
        return state;
    }

    /**
     * Creates a aplit pane to render the component and sampler in.
     *
     * @return a new split pane
     */
    protected SplitPane createSplitPane() {
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "SMSConfigEmail");
    }
}
