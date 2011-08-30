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
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.subsystem.AbstractViewWorkspace;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * Generic CRUD workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class CRUDWorkspace<T extends IMObject>
        extends AbstractViewWorkspace<T> {

    /**
     * The CRUD window.
     */
    private CRUDWindow<T> window;


    /**
     * Constructs a new <tt>CRUDWorkspace</tt>.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identfifier
     * @param shortNames  the archetype short names that this operates on
     */
    public CRUDWorkspace(String subsystemId, String workspaceId,
                         ShortNames shortNames) {
        super(subsystemId, workspaceId, shortNames);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    @Override
    public void setObject(T object) {
        super.setObject(object);
        getCRUDWindow().setObject(object);
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        CRUDWindow<T> window = getCRUDWindow();
        container.add(window.getComponent());
        window.setListener(new CRUDWindowListener<T>() {
            public void saved(T object, boolean isNew) {
                onSaved(object, isNew);
            }

            public void deleted(T object) {
                onDeleted(object);
            }

            public void refresh(T object) {
                onRefresh(object);
            }
        });
    }

    /**
     * Invoked when the 'select' button is pressed. This pops up an {@link
     * Browser} to select an object.
     */
    @Override
    protected void onSelect() {
        try {
            final Browser<T> browser = createBrowser();

            String title = Messages.get("imobject.select.title", getType());
            final BrowserDialog<T> popup = new BrowserDialog<T>(
                    title, browser, true);

            popup.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    if (popup.createNew()) {
                        getCRUDWindow().onCreate();
                    } else {
                        T object = popup.getSelected();
                        if (object != null) {
                            onSelected(object);
                        }
                    }
                }

            });

            popup.show();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(T object, boolean isNew) {
        setObject(object);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(T object) {
        setObject(null);
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(T object) {
        object = IMObjectHelper.reload(object);
        setObject(object);
    }

    /**
     * Returns the CRUD window, creating it if it doesn't exist.
     *
     * @return the CRUD window
     */
    protected CRUDWindow<T> getCRUDWindow() {
        if (window == null) {
            window = createCRUDWindow();
        }
        return window;
    }

    /**
     * Creates a new CRUD window.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<T> createCRUDWindow() {
        return new DefaultCRUDWindow<T>(getType(), getShortNames());
    }

}
