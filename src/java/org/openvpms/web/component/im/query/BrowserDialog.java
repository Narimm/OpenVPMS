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

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * Displays a {@link Browser} in a popup dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BrowserDialog<T> extends PopupDialog {

    /**
     * New button identifier.
     */
    public static final String NEW_ID = "new";

    /**
     * The selected object.
     */
    private T selected;

    /**
     * Determines if the user wants to create a new object. Set when the 'New'
     * button is pressed.
     */
    private boolean createNew = false;

    /**
     * The browser.
     */
    private final Browser<T> browser;

    /**
     * Window style name.
     */
    private static final String STYLE = "BrowserDialog";


    /**
     * Construct a new <tt>BrowserDialog</tt>.
     *
     * @param title   the dialog title
     * @param browser the browser
     */
    public BrowserDialog(String title, Browser<T> browser) {
        this(title, browser, false);
    }

    /**
     * Constructs a new <tt>BrowserDialog</tt>.
     *
     * @param title   the dialog title
     * @param buttons the buttons to display
     * @param browser the browser
     */
    public BrowserDialog(String title, String[] buttons, Browser<T> browser) {
        this(title, null, buttons, browser, false);
    }

    /**
     * Construct a new <tt>BrowserDialog</tt>.
     *
     * @param title   the dialog title
     * @param browser the browser
     * @param addNew  if <tt>true</tt> add a 'new' button
     */
    public BrowserDialog(String title, Browser<T> browser, boolean addNew) {
        this(title, null, CANCEL, browser, addNew);
    }

    /**
     * Construct a new <tt>BrowserDialog</tt>.
     *
     * @param title   the dialog title
     * @param message the dialog message. May be <tt>null</tt>
     * @param buttons the buttons to display
     * @param browser the browser
     * @param addNew  if <tt>true</tt> add a 'new' button
     */
    public BrowserDialog(String title, String message, String[] buttons,
                         Browser<T> browser, boolean addNew) {
        super(title, STYLE, buttons, browser.getFocusGroup());
        setModal(true);

        Component component = browser.getComponent();
        if (message != null) {
            Label label = LabelFactory.create(null, "bold");
            label.setText(message);
            Row inset = RowFactory.create("Inset", label);
            Column column = ColumnFactory.create("CellSpacing", inset,
                                                 component);
            getLayout().add(column);
        } else {
            getLayout().add(component);
        }

        if (addNew) {
            addButton(NEW_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onNew();
                }
            });
        }
        browser.addQueryListener(new QueryBrowserListener<T>() {
            public void query() {
            }

            public void selected(T object) {
                onSelected(object);
            }
        });
        this.browser = browser;
    }

    /**
     * Returns the browser.
     *
     * @return the browser
     */
    public Browser<T> getBrowser() {
        return browser;
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <tt>null</tt> if none was selected
     */
    public T getSelected() {
        return selected;
    }

    /**
     * Determines if the 'New' button was selected, indicating that a new object
     * should be created.
     *
     * @return <tt>true</tt> if 'New' was selected
     */
    public boolean createNew() {
        return createNew;
    }

    /**
     * Select the current object, and close the browser.
     *
     * @param object the selected object
     */
    protected void onSelected(T object) {
        selected = object;
        close();
    }

    /**
     * Flags that the user wants to create a new instance, and closes the
     * browser.
     */
    protected void onNew() {
        createNew = true;
        close();
    }

}
