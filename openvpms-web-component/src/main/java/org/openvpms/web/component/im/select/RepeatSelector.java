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
package org.openvpms.web.component.im.select;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.component.util.ButtonRow;


/**
 * An {@link Selector} that provides a "Select Again" button to enable a previous search to be repeated.
 * <p/>
 * By default the "Select Again" button is hidden.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RepeatSelector<T extends IMObject> extends Selector<T> {

    /**
     * The 'select again' button.
     */
    private Button selectAgain;


    /**
     * Constructs a <tt>RepeatSelector</tt>
     */
    public RepeatSelector() {
        super("button.find");
    }

    /**
     * Returns the "SelectAgain" button.
     * <p/>
     * This is initially hidden. Use {@link #setShowSelectAgain} to display it
     *
     * @return the button
     */
    public Button getSelectAgain() {
        if (selectAgain == null) {
            selectAgain = ButtonFactory.create("findAgain");
            selectAgain.setVisible(false);
            selectAgain.setEnabled(false); // disables shortcuts
        }
        return selectAgain;
    }

    /**
     * Determines if the "Select Again" button should be displayed or hidden
     *
     * @param show if <tt>true</tt> display the button, otherwise hide it
     */
    public void setShowSelectAgain(boolean show) {
        Button button = getSelectAgain();
        button.setVisible(show);
        button.setEnabled(show);
    }

    /**
     * Determines if the "Select Again" button is displayed.
     *
     * @return <tt>true</tt> if the button is displayed, <tt>false</tt> if it is hidden
     */
    public boolean isShowSelectAgain() {
        return getSelectAgain().isVisible();
    }

    /**
     * Returns the button component.
     *
     * @param container the parent container
     * @return the button(s)
     */
    @Override
    protected ButtonRow getButtons(Component container) {
        ButtonRow row = super.getButtons(container);
        row.addButton(getSelectAgain());
        return row;
    }
}
