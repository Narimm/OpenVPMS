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
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.edit.PropertySet;


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
    private final LayoutContext _context;


    /**
     * Construct a new <code>IMObjectViewer</code>.
     *
     * @param object the object to view.
     */
    public IMObjectViewer(IMObject object) {
        this(object, new DefaultLayoutStrategyFactory().create(object),
             null);
    }

    /**
     * Construct a new <code>IMObjectViewer</code>.
     *
     * @param object  the object to view.
     * @param context the layout context. May be <code>null</code>
     */
    public IMObjectViewer(IMObject object, LayoutContext context) {
        this(object, new DefaultLayoutStrategyFactory().create(object),
             context);
    }

    /**
     * Construct a new <code>IMObjectViewer</code>.
     *
     * @param object  the object to view.
     * @param layout  the layout strategy. May be <code>null</code>
     * @param context the layout context. May be <code>null</code>
     */
    public IMObjectViewer(IMObject object, IMObjectLayoutStrategy layout,
                          LayoutContext context) {
        super(object, new PropertySet(object), layout);
        IMObjectComponentFactory factory = new ReadOnlyComponentFactory(context);
        if (context == null) {
            _context = new DefaultLayoutContext(factory);
        } else {
            _context = new DefaultLayoutContext(context);
            _context.setComponentFactory(factory);
        }
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
        return _context;
    }

}
