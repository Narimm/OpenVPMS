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
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Editor for <em>entity.SMSEmail*</em> provider configurations.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class EmailSMSProviderConfigurationEditor extends AbstractIMObjectEditor {

    /**
     * The sample text message viewer.
     */
    private EmailSMSSampler sampler;


    /**
     * Constructs an <tt>EmailSMSProviderConfigurationEditor</tt>.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be <tt>null</tt>
     * @param layoutContext the layout context. May be <tt>null</tt>.
     */
    public EmailSMSProviderConfigurationEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        sampler = new EmailSMSSampler(object);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }

    /**
     * Invoked when an object is modified.
     * <p/>
     * This implementation refreshes the sampler
     *
     * @param modifiable the modified object
     */
    @Override
    protected void onModified(Modifiable modifiable) {
        super.onModified(modifiable);
        sampler.refresh();
    }

    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Apply the layout strategy.
         * <p/>
         * This renders an object in a <code>Component</code>, using a factory to
         * create the child components.
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
            Component container = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "SMSConfigEmailProvider");
            container.add(state.getComponent());
            container.add(sampler.getComponent());
            FocusGroup group = new FocusGroup("EmailSMSProviderConfigurationEditor");
            group.add(state.getFocusGroup());
            group.add(sampler.getFocusGroup());

            return new ComponentState(container, group);
        }
    }
}
