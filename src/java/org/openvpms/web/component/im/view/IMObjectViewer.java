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

package org.openvpms.web.component.im.view;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.layout.ViewLayoutStrategyFactory;
import org.openvpms.web.component.property.PropertySet;


/**
 * {@link IMObject} viewer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectViewer extends AbstractIMObjectView {

    /**
     * The layout context.
     */
    private final LayoutContext context;


    /**
     * Constructs a new <code>IMObjectViewer</code>.
     *
     * @param object the object to view.
     * @param parent the parent object. May be <code>null</code>
     */
    public IMObjectViewer(IMObject object, IMObject parent) {
        this(object, parent, new ViewLayoutStrategyFactory().create(object),
             null);
    }

    /**
     * Construct a new <code>IMObjectViewer</code>.
     *
     * @param object  the object to view.
     * @param parent  the parent object. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>
     */
    public IMObjectViewer(IMObject object, IMObject parent,
                          LayoutContext context) {
        this(object, parent, new ViewLayoutStrategyFactory().create(object),
             context);
    }

    /**
     * Construct a new <code>IMObjectViewer</code>.
     *
     * @param object  the object to view.
     * @param parent  the parent object. May be <code>null</code>
     * @param layout  the layout strategy. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>
     */
    public IMObjectViewer(IMObject object, IMObject parent,
                          IMObjectLayoutStrategy layout,
                          LayoutContext context) {
        super(object, new PropertySet(object, context), parent, layout);
        if (context == null) {
            this.context = new DefaultLayoutContext();
        } else {
            this.context = new DefaultLayoutContext(context);
        }
        IMObjectComponentFactory factory
                = new ReadOnlyComponentFactory(this.context);
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
     * Returns the factory for creating components for displaying the object.
     *
     * @return the component factory
     */
    protected LayoutContext getLayoutContext() {
        return context;
    }

}
