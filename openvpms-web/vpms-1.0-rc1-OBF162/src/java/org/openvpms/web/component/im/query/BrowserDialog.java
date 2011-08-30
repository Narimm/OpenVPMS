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

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.dialog.PopupDialog;


/**
 * Displays an {@link TableBrowser} in a popup dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class BrowserDialog<T extends IMObject> extends PopupDialog {

    /**
     * New button identifier.
     */
    public static final String NEW_ID = "new";

    /**
     * The selected object.
     */
    private T _selected;

    /**
     * Determines if the user wants to create a new object. Set when the 'New'
     * button is pressed.
     */
    private boolean _createNew = false;

    /**
     * Window style name.
     */
    private static final String STYLE = "BrowserDialog";


    /**
     * Construct a new <code>BrowserDialog</code>.
     *
     * @param title   the dialog title
     * @param browser the browser
     */
    public BrowserDialog(String title, Browser<T> browser) {
        this(title, browser, false);
    }

    /**
     * Constructs a new <code>BrowserDialog</code>.
     *
     * @param title   the dialog title
     * @param buttons the buttons to display
     * @param browser the browser
     */
    public BrowserDialog(String title, String[] buttons, Browser<T> browser) {
        this(title, buttons, browser, false);
    }

    /**
     * Construct a new <code>BrowserDialog</code>.
     *
     * @param title   the dialog title
     * @param browser the browser
     * @param addNew  if <code>true</code> add a 'new' button
     */
    public BrowserDialog(String title, Browser<T> browser, boolean addNew) {
        this(title, CANCEL, browser, addNew);
    }

    /**
     * Construct a new <code>BrowserDialog</code>.
     *
     * @param title   the dialog title
     * @param buttons the buttons to display
     * @param browser the browser
     * @param addNew  if <code>true</code> add a 'new' button
     */
    public BrowserDialog(String title, String[] buttons, Browser<T> browser,
                         boolean addNew) {
        super(title, STYLE, buttons, browser.getFocusGroup());
        setModal(true);
        getLayout().add(browser.getComponent());

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
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none was selected
     */
    public T getSelected() {
        return _selected;
    }

    /**
     * Determines if the 'New' button was selected, indicating that a new object
     * should be created.
     *
     * @return <code>true</code> if 'New' was selected
     */
    public boolean createNew() {
        return _createNew;
    }

    /**
     * Select the current object, and close the browser.
     *
     * @param object the selected object
     */
    protected void onSelected(T object) {
        _selected = object;
        close();
    }

    /**
     * Flags that the user wants to create a new instance, and closes the
     * browser.
     */
    protected void onNew() {
        _createNew = true;
        close();
    }

}
