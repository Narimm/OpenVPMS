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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.component.util.KeyStrokeHelper;


/**
 * A dialog that allows the results from an {@link ResultSet} to be iterated through and viewed.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ViewResultSetDialog<T extends IMObject> extends PopupDialog {

    /**
     * The 'edit' button identifier.
     */
    public static final String EDIT_ID = "edit";

    /**
     * The iterator over the results.
     */
    private ResultSetIterator<T> iter;

    /**
     * The 'previous' button id.
     */
    private static final String PREVIOUS_ID = "previous";

    /**
     * The 'next' button id.
     */
    private static final String NEXT_ID = "next";

    /**
     * The buttons to display.
     */
    private static final String[] BUTTONS = {CANCEL_ID, EDIT_ID, PREVIOUS_ID, NEXT_ID};

    /**
     * The object being viewed.
     */
    private Component currentViewer;

    /**
     * The selected object.
     */
    private T selected;

    /**
     * The listener for context switch events.
     */
    private final ContextSwitchListener listener;


    /**
     * Constructs a <tt>ViewResultSetDialog</tt>.
     *
     * @param title the window title
     * @param first the first object to view
     * @param set   the set of results to view
     */
    public ViewResultSetDialog(String title, T first, ResultSet<T> set) {
        super(title, "IMObjectViewerDialog", BUTTONS);
        setDefaultButton(OK_ID);
        iter = new ResultSetIterator<T>(set, first);
        listener = new ContextSwitchListener() {
            public void switchTo(IMObject object) {
                viewChild(object);
            }

            public void switchTo(String shortName) {
            }
        };
        setModal(true);
        if (iter.hasNext()) {
            view(iter.next());
        }
        enableButtons(true, false);
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object. May be <tt>null</tt>
     */
    public T getSelected() {
        return selected;
    }

    /**
     * Invoked when the 'previous' button is pressed.
     */
    protected void onPrevious() {
        long id = (selected != null) ? selected.getId() : -1;
        if (iter.hasPrevious()) {
            T object = iter.previous();
            if (object.getId() == id) {
                // list iterator returns the same object if previous()/next() invoked or vice versa
                if (iter.hasPrevious()) {
                    object = iter.previous();
                }
            }
            view(object);
            enableButtons(false, false);
        }
    }

    /**
     * Invoked when the 'next' button is pressed.
     */
    protected void onNext() {
        long id = (selected != null) ? selected.getId() : -1;
        if (iter.hasNext()) {
            T object = iter.next();
            if (object.getId() == id) {
                if (iter.hasNext()) {
                    // list iterator returns the same object if previous()/next() invoked or vice versa
                    object = iter.next();
                }
            }
            view(object);
            enableButtons(false, true);
        }
    }

    /**
     * Invoked when a button is pressed. This delegates to the appropriate
     * on*() method for the button if it is known, else sets the action to
     * the button identifier and closes the window.
     *
     * @param button the button identifier
     */
    @Override
    protected void onButton(String button) {
        if (PREVIOUS_ID.equals(button)) {
            onPrevious();
        } else if (NEXT_ID.equals(button)) {
            onNext();
        } else {
            super.onButton(button);
        }
    }

    /**
     * Views an object.
     *
     * @param object the object to view
     */
    private void view(T object) {
        selected = object;
        LayoutContext context = new DefaultLayoutContext();
        context.setContextSwitchListener(listener);
        IMObjectViewer viewer = new IMObjectViewer(object, null, context);
        SplitPane pane = getLayout();
        if (currentViewer != null) {
            pane.remove(currentViewer);
        }
        currentViewer = viewer.getComponent();
        pane.add(currentViewer);
    }

    /**
     * Enables/disables the previous and next buttons.
     * <p/>
     * This should be invoked <em>after</em> moving the iterator.
     *
     * @param focusOK   if <tt>true</tt> move the focus to the OK button
     * @param focusNext if <tt>true</tt> move the focus to the 'next' button, unless its disabled in which case the
     *                  'previous' button will be used
     */
    private void enableButtons(boolean focusOK, boolean focusNext) {
        ButtonSet set = getButtons();
        Button ok = set.getButton(OK_ID);
        Button previous = set.getButton(PREVIOUS_ID);
        Button next = set.getButton(NEXT_ID);
        previous.setEnabled(iter.lastIndex() > 0);
        next.setEnabled(iter.hasNext());
        if (focusOK) {
            setFocus(ok);
        } else if (focusNext && next.isEnabled()) {
            setFocus(next);
        } else if (previous.isEnabled()) {
            setFocus(previous);
        } else {
            setFocus(ok);
        }

        KeyStrokeHelper.reregisterKeyStrokeListeners(this);
        // TODO - without the above, keyboard shortcuts can stop working, despite elements on the dialog
        // having the focus.        
    }

    /**
     * Views a child object.
     *
     * @param object the object to view
     */
    private void viewChild(IMObject object) {
        IMObjectViewerDialog dialog = new IMObjectViewerDialog(object);
        dialog.show();
    }

}