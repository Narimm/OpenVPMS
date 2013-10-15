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

package org.openvpms.web.workspace.reporting.till;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.SelectField;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.List;

import static org.openvpms.component.business.service.archetype.functor.IsActiveRelationship.isActiveNow;

/**
 * Clear Till dialog.
 *
 * @author Tim Anderson
 */
public class ClearTillDialog extends AbstractClearTillDialog {

    /**
     * The bank account selector.
     */
    private final SelectField account;

    /**
     * Determines if the till float amount dialog will be displayed.
     */
    private final boolean showAmount;


    /**
     * Constructs a {@link ClearTillDialog}.
     *
     * @param location   the location to clear the till for
     * @param showAmount if {@code true} prompt for the cash float amount
     * @param context    the context
     * @param help       the help context
     */
    public ClearTillDialog(Party location, boolean showAmount, Context context, HelpContext help) {
        super(Messages.get("till.clear.title"), help);
        account = createAccountSelector(location, context);
        this.showAmount = showAmount;

        Grid grid = GridFactory.create(2);
        if (showAmount) {
            addAmount(grid);
        }
        grid.add(LabelFactory.create("till.clear.account"));
        grid.add(account);
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, grid));
    }

    /**
     * Returns the bank deposit organisation.
     *
     * @return the bank deposit organisation
     */
    public Party getAccount() {
        return (Party) account.getSelectedItem();
    }

    /**
     * Invoked when the OK button is pressed. Closes the window if the amount
     * and deposit are valid
     */
    protected void onOK() {
        if ((!showAmount || getCashFloat() != null) && getAccount() != null) {
            super.onOK();
        }
    }

    /**
     * Creates a select field to select a deposit account.
     *
     * @param location the location to use to determine the deposit accounts
     * @param context  the context
     * @return a new select field
     */
    private SelectField createAccountSelector(Party location, Context context) {
        SelectField result;
        IMObject selected = null;
        List<IMObject> accounts = new ArrayList<IMObject>();
        IMObjectBean bean = new IMObjectBean(location);
        List<EntityRelationship> relationships = bean.getValues("depositAccounts", isActiveNow(),
                                                                EntityRelationship.class);
        for (EntityRelationship relationship : relationships) {
            IMObject account = IMObjectHelper.getObject(relationship.getTarget(), context);
            if (account.isActive()) {
                accounts.add(account);
                IMObjectBean defBean = new IMObjectBean(relationship);
                if (defBean.getBoolean("default")) {
                    selected = account;
                    break;
                } else if (selected == null) {
                    selected = account;
                }
            }
        }
        IMObjectSorter.sort(accounts, "name");
        result = SelectFieldFactory.create(accounts);
        result.setCellRenderer(IMObjectListCellRenderer.NAME);
        if (!accounts.isEmpty()) {
            if (selected != null) {
                result.setSelectedItem(selected);
            } else {
                result.setSelectedIndex(0);
            }
        }
        return result;
    }

}
