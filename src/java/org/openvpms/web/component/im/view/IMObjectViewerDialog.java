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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.Stack;


/**
 * Displays an {@link IMObjectViewer} in a popup window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectViewerDialog extends PopupDialog {

    /**
     * The current component;
     */
    private Component current;

    /**
     * The history of displayed references.
     */
    private Stack<IMObjectReference> history = new Stack<IMObjectReference>();

    /**
     * Dialog style name.
     */
    private static final String STYLE = "IMObjectViewerDialog";

    /**
     * The 'previous' button identifier.
     */
    private static final String PREVIOUS_ID = "previous";

    /**
     * The buttons to display.
     */
    private static final String[] BUTTONS = {CANCEL_ID, PREVIOUS_ID};


    /**
     * Constructs an <tt>IMObjectViewerDialog</tt>.
     *
     * @param viewer the viewer to display. May be <tt>null</tt>
     */
    public IMObjectViewerDialog(IMObjectViewer viewer) {
        super(null, STYLE, BUTTONS);
        setModal(true);
        if (viewer != null) {
            setViewer(viewer);
        } else {
            enableButtons();
        }
    }

    /**
     * Constructs an <tt>IMObjectViewerDialog</tt>.
     *
     * @param object the object to display. May be <tt>null</tt>
     */
    public IMObjectViewerDialog(IMObject object) {
        super(null, STYLE, BUTTONS);
        setModal(true);
        if (object != null) {
            setObject(object);
        } else {
            enableButtons();
        }
    }

    /**
     * Displays an object.
     *
     * @param object the object to display
     */
    public void setObject(IMObject object) {
        LayoutContext context = new DefaultLayoutContext();
        context.setContextSwitchListener(new ContextSwitchListener() {
            public void switchTo(IMObject child) {
                setObject(child);
            }

            public void switchTo(String shortName) {
            }
        });
        IMObjectViewer viewer = new IMObjectViewer(object, null, context);
        setViewer(viewer);
    }

    /**
     * Displays a new viewer.
     *
     * @param viewer the viewer to diaplay
     */
    public void setViewer(IMObjectViewer viewer) {
        setTitle(viewer.getTitle());
        SplitPane pane = getLayout();
        if (current != null) {
            pane.remove(current);
        }
        current = viewer.getComponent();
        pane.add(current);

        IMObjectReference reference = viewer.getObject().getObjectReference();
        history.push(reference);
        enableButtons();
    }

    /**
     * Invoked when a button is pressed.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (PREVIOUS_ID.equals(button)) {
            onPrevious();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Invoked when the 'previous' button is displayed. Displays the prior object, if any.
     */
    private void onPrevious() {
        if (history.size() > 1) {
            history.pop(); // pop the current object.
            IMObject object = null;
            while (object == null && !history.isEmpty()) {
                IMObjectReference previous = history.pop();
                object = IMObjectHelper.getObject(previous);
            }
            if (object != null) {
                setObject(object);
            } else {
                enableButtons();
            }
        }
    }

    /**
     * Enables/disables the 'previous' button.
     */
    private void enableButtons() {
        getButtons().setEnabled(PREVIOUS_ID, history.size() > 1);
    }


}
