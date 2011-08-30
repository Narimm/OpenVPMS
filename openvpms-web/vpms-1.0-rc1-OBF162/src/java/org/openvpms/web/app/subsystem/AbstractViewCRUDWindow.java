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

package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
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
    private Component _objectContainer;

    /**
     * The style name.
     */
    private static final String STYLE = "CRUDWindow";


    /**
     * Constructs a new <code>AbstractCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public AbstractViewCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(T object) {
        super.setObject(object);
        _objectContainer.removeAll();
        if (object != null) {
            IMObjectViewer viewer = createViewer(object);
            _objectContainer.add(viewer.getComponent());
        }
    }

    /**
     * Creates a new {@link IMObjectViewer} for an object.
     *
     * @param object the object to view
     */
    protected IMObjectViewer createViewer(IMObject object) {
        return new IMObjectViewer(object, null);
    }

    /**
     * Lays out the component.
     */
    @Override
    protected Component doLayout() {
        super.doLayout();
        _objectContainer = ColumnFactory.create();
        return SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                STYLE, getButtons().getContainer(), _objectContainer);
    }
}
