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

package org.openvpms.web.component.subsystem;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.SplitPaneFactory;


/**
 * Abstract implementation of the {@link CRUDWindow} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractViewCRUDWindow<T extends IMObject>
        extends AbstractCRUDWindow<T> {

    /**
     * The selected object container.
     */
    private Component objectContainer;

    /**
     * The style name.
     */
    private static final String STYLE = "CRUDWindow";


    /**
     * Constructs a new <tt>AbstractViewCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create. If <tt>null</tt>
     *                   the subclass must override {@link #getArchetypes}
     */
    public AbstractViewCRUDWindow(Archetypes<T> archetypes) {
        super(archetypes);
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    public void setObject(T object) {
        super.setObject(object);
        objectContainer.removeAll();
        if (object != null) {
            IMObjectViewer viewer = createViewer(object);
            objectContainer.add(viewer.getComponent());
        }
    }

    /**
     * Creates a new {@link IMObjectViewer} for an object.
     *
     * @param object the object to view
     * @return a new viewer
     */
    protected IMObjectViewer createViewer(IMObject object) {
        LayoutContext context = createViewLayoutContext();
        return new IMObjectViewer(object, null, context);
    }

    /**
     * Creates a layout context for viewing objects.
     *
     * @return a new layout context
     */
    protected LayoutContext createViewLayoutContext() {
        LayoutContext context = new DefaultLayoutContext();
        context.setMailContext(getMailContext());
        context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);
        return context;
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        super.doLayout();
        objectContainer = ColumnFactory.create();
        return SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                STYLE, getButtons().getContainer(), objectContainer);
    }
}
