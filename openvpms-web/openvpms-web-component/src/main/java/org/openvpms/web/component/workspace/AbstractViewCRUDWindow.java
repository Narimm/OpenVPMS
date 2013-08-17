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

package org.openvpms.web.component.workspace;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.IMObjectActions;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Abstract implementation of the {@link CRUDWindow} interface.
 *
 * @author Tim Anderson
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
     * The object viewer.
     */
    private IMObjectViewer viewer;


    /**
     * Constructs an {@link AbstractViewCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create. If {@code null}
     *                   the subclass must override {@link #getArchetypes}
     * @param actions    determines the operations that may be performed on the selected object
     * @param context    the context
     * @param help       the help context
     */
    public AbstractViewCRUDWindow(Archetypes<T> archetypes, IMObjectActions<T> actions, Context context,
                                  HelpContext help) {
        super(archetypes, actions, context, help);
        objectContainer = ColumnFactory.create();
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be {@code null}
     */
    public void setObject(T object) {
        super.setObject(object);
        objectContainer.removeAll();
        viewer = null;
        if (object != null) {
            viewer = createViewer(object);
            objectContainer.add(viewer.getComponent());
        }
    }

    /**
     * Edits the current object.
     */
    @Override
    public void edit() {
        if (viewer != null) {
            setSelectionPath(viewer.getSelectionPath());
        }
        super.edit();
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
        LayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        context.setMailContext(getMailContext());
        context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);
        return context;
    }


    /**
     * Returns the view container.
     *
     * @return the container
     */
    protected Component getContainer() {
        return objectContainer;
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        super.doLayout();
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, STYLE, getButtons().getContainer(),
                                       getContainer());
    }
}
