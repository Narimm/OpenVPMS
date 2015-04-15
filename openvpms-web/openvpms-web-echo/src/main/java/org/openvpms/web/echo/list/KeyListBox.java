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
package org.openvpms.web.echo.list;

import nextapp.echo2.app.ListBox;
import nextapp.echo2.app.list.ListModel;


/**
 * A {@link KeyListBox} provides the same look and feel as <tt>ListBox</tt> except with the exception that it only
 * triggers action events when:
 * <ul>
 * <li>enter is pressed
 * <li>an option is selected with the mouse
 * </ul>
 * This is a workaround for <tt>ListBox</tt>, which triggers action events when the arrow keys are used.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class KeyListBox extends ListBox {

    /**
     * Creates an empty <code>ListBox</code>.
     * A <code>DefaultListModel</code> will be created.
     * A <code>DefaultListSelectionModel</code> will be created and used
     * to describe selections.
     */
    public KeyListBox() {
    }

    /**
     * Creates a <code>ListBox</code> visualizing the specified model.
     * A <code>DefaultListSelectionModel</code> will be created and used
     * to describe selections.
     *
     * @param model the initial model
     */
    public KeyListBox(ListModel model) {
        super(model);
    }
}
