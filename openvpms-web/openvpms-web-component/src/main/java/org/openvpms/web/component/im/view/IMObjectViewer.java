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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.view;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.layout.ViewLayoutStrategyFactory;
import org.openvpms.web.component.property.PropertySet;


/**
 * {@link IMObject} viewer.
 *
 * @author Tim Anderson
 */
public class IMObjectViewer extends AbstractIMObjectView {

    /**
     * The layout context.
     */
    private final LayoutContext context;


    /**
     * Constructs an {@code IMObjectViewer}.
     *
     * @param object  the object to view.
     * @param context the layout context
     */
    public IMObjectViewer(IMObject object, LayoutContext context) {
        this(object, null, context);
    }

    /**
     * Constructs an {@code IMObjectViewer}.
     *
     * @param object  the object to view.
     * @param parent  the parent object. May be {@code null}
     * @param context the layout context
     */
    public IMObjectViewer(IMObject object, IMObject parent, LayoutContext context) {
        this(object, parent, new ViewLayoutStrategyFactory().create(object), context);
    }

    /**
     * Constructs an {@code IMObjectViewer}.
     *
     * @param object  the object to view.
     * @param parent  the parent object. May be {@code null}
     * @param layout  the layout strategy. May be {@code null}
     * @param context the layout context
     */
    public IMObjectViewer(IMObject object, IMObject parent, IMObjectLayoutStrategy layout, LayoutContext context) {
        super(object, new PropertySet(object, context), parent, layout);
        this.context = new DefaultLayoutContext(context);
        // don't increase the layout depth
        this.context.setLayoutDepth(context.getLayoutDepth());
        IMObjectComponentFactory factory = new ReadOnlyComponentFactory(this.context);
        this.context.setComponentFactory(factory);
    }

    /**
     * Returns a title for the viewer.
     *
     * @return a title for the viewer
     */
    public String getTitle() {
        return DescriptorHelper.getDisplayName(getObject());
    }

    /**
     * Registers a listener for context switch events.
     * <p/>
     * Note that this will only be utilised if set prior to {@link #getComponent()} being invoked.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setContextSwitchListener(ContextSwitchListener listener) {
        context.setContextSwitchListener(listener);
    }

    /**
     * Returns the layout context
     *
     * @return the layout context
     */
    protected LayoutContext getLayoutContext() {
        return context;
    }

}
