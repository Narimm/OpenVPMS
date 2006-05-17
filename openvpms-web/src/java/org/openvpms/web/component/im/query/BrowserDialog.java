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
public class BrowserDialog extends PopupDialog {

    /**
     * New button identifier.
     */
    public static final String NEW_ID = "new";

    /**
     * The selected object.
     */
    private IMObject _selected;

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
     * @param browser the editor
     */
    public BrowserDialog(String title, Browser browser) {
        this(title, browser, false);
    }

    /**
     * Construct a new <code>BrowserDialog</code>.
     *
     * @param title   the dialog title
     * @param browser the editor
     * @param addNew  if <code>true</code> add a 'new' button
     */
    public BrowserDialog(String title, Browser browser, boolean addNew) {
        super(title, STYLE, Buttons.CANCEL);
        setModal(true);
        getLayout().add(browser.getComponent());

        if (addNew) {
            addButton(NEW_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onNew();
                }
            });
        }
        browser.addQueryListener(new QueryBrowserListener() {
            public void query() {
            }

            public void selected(IMObject object) {
                onSelected(object);
            }
        });
    }

    /**
     * Returns the selected object.
     *
     * @return the selected object, or <code>null</code> if none was selected
     */
    public IMObject getSelected() {
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
    protected void onSelected(IMObject object) {
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
