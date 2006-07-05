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

package org.openvpms.web.app.financial;

import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.util.ButtonFactory;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;


/**
 * CRUD window for till balances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-07-03 23:56:49Z $
 */
public class TillCRUDWindow extends CRUDWindow {

    /**
     * The clear button.
     */
    private Button _clear;

    /**
     * The summary button.
     */
    private Button _summary;

    /**
     * The print button.
     */
    private Button _print;

    /**
     * The adjust button.
     */
    private Button _adjust;

    /**
     * Clear button identifier.
     */
    private static final String CLEAR_ID = "clear";

    /**
     * Summary button identifier.
     */
    private static final String SUMMARY_ID = "summary";

    /**
     * Print button identifier.
     */
    private static final String PRINT_ID = "print";

    /**
     * Adjust button identifier.
     */
    private static final String ADJUST_ID = "adjust";


    /**
     * Create a new <code>EstimationCRUDWindow</code>.
     *
     * @param type         display name for the types of objects that this may
     *                     create
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public TillCRUDWindow(String type, String refModelName,
                          String entityName, String conceptName) {
        super(type, new ShortNameList(refModelName, entityName, conceptName));
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        _clear = ButtonFactory.create(CLEAR_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onClear();
            }
        });
        _print = ButtonFactory.create(PRINT_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onPrint();
            }
        });
        _summary = ButtonFactory.create(SUMMARY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSummary();
            }
        });
        _adjust = ButtonFactory.create(ADJUST_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onAdjust();
            }
        });
        buttons.add(_clear);
        buttons.add(_summary);
        buttons.add(_print);
        buttons.add(_adjust);
        buttons.add(getEditButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(boolean enable) {
        Row buttons = getButtons();
        buttons.removeAll();
        if (enable) {
            Act act = (Act) getObject();
            boolean uncleared = act.getStatus().equals("Uncleared");
            if (uncleared) {
                buttons.add(_clear);
            }
            buttons.add(_summary);
            buttons.add(_print);
            if (uncleared) {
                buttons.add(_adjust);
                if (TypeHelper.isA(act, "act.tillBalanceAdjustment")) {
                    buttons.add(getEditButton());
                }
            }
        }
    }

    /**
     * Invoked when the 'clear' button is pressed.
     */
    protected void onClear() {
        final Act act = (Act) getObject();
    }

    /**
     * Invoked when the 'summary' button is pressed.
     */
    protected void onSummary() {
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    protected void onPrint() {
    }

    /**
     * Invoked when the 'adjust' button is pressed.
     */
    protected void onAdjust() {
    }


}
