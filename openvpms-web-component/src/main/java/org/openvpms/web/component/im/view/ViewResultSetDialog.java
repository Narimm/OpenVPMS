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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.keyboard.KeyStrokeHelper;


/**
 * A dialog that allows the results from an {@link ResultSet} to be iterated through and viewed.
 *
 * @author Tim Anderson
 */
public class ViewResultSetDialog<T extends IMObject> extends PopupDialog {

    /**
     * The 'edit' button identifier.
     */
    public static final String EDIT_ID = "edit";

    /**
     * The context.
     */
    private final Context context;

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
     * The view-only buttons to display.
     */
    private static final String[] VIEW_BUTTONS = {CANCEL_ID, PREVIOUS_ID, NEXT_ID};

    /**
     * The view/edit buttons to display.
     */
    private static final String[] EDIT_BUTTONS = {CANCEL_ID, EDIT_ID, PREVIOUS_ID, NEXT_ID};

    /**
     * The object being viewed.
     */
    private IMObjectViewer viewer;

    /**
     * The selected object.
     */
    private T selected;

    /**
     * The listener for context switch events.
     */
    private final ContextSwitchListener listener;


    /**
     * Constructs a {@code ViewResultSetDialog}.
     *
     * @param title   the window title
     * @param first   the first object to view
     * @param set     the set of results to view
     * @param edit    if {@code true} display an edit button
     * @param context the context
     * @param help    the help context
     */
    public ViewResultSetDialog(String title, T first, ResultSet<T> set, boolean edit, Context context,
                               HelpContext help) {
        super(title, "IMObjectViewerDialog", edit ? EDIT_BUTTONS : VIEW_BUTTONS, help);
        this.context = context;
        setDefaultButton(OK_ID);
        setDefaultCloseAction(CANCEL_ID);
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
     * @return the selected object. May be {@code null}
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
     * Returns the help context.
     * <p/>
     * This implementation returns the help context of the viewer, if one is registered
     *
     * @return the help context
     */
    @Override
    public HelpContext getHelpContext() {
        return (viewer != null) ? viewer.getHelpContext() : super.getHelpContext();
    }

    /**
     * Views an object.
     *
     * @param object the object to view
     */
    protected void view(T object) {
        selected = object;
        HelpContext help = getHelpContext().topic(object, "view");
        LayoutContext context = new DefaultLayoutContext(this.context, help);
        context.getContext().setCurrent(object); // TODO - remove requirement for setCurrent()
        context.setContextSwitchListener(listener);
        IMObjectViewer viewer = new IMObjectViewer(object, null, context);
        SplitPane pane = getLayout();
        if (this.viewer != null) {
            pane.remove(this.viewer.getComponent());
        }
        this.viewer = viewer;
        pane.add(this.viewer.getComponent());
    }

    /**
     * Enables/disables the previous and next buttons.
     * <p/>
     * This should be invoked <em>after</em> moving the iterator.
     *
     * @param focusOK   if {@code true} move the focus to the OK button
     * @param focusNext if {@code true} move the focus to the 'next' button, unless its disabled in which case the
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
        HelpContext help = getHelpContext().topic(object, "view");
        IMObjectViewerDialog dialog = new IMObjectViewerDialog(object, context, help);
        dialog.show();
    }

}