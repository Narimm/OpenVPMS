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

package org.openvpms.web.app.reporting.till;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.functor.IsActiveRelationship;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.IMObjectSorter;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.factory.TextComponentFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Clear Till dialog.
 *
 * @author Tim Anderson
 */
public class ClearTillDialog extends PopupDialog {

    /**
     * The amount field.
     */
    private final TextField amount;

    /**
     * The bank account selector.
     */
    private final SelectField account;


    /**
     * Constructs a {@code ClearTillDialog}.
     *
     * @param location the location to clear the till for
     * @param context  the context
     * @param help     the help context
     */
    public ClearTillDialog(Party location, Context context, HelpContext help) {
        super(Messages.get("till.clear.title"), null, OK_CANCEL, help);
        setModal(true);
        amount = TextComponentFactory.create();
        account = createAccountSelector(location, context);


        Grid grid = GridFactory.create(2);
        grid.add(LabelFactory.create("till.clear.amount"));
        grid.add(amount);
        grid.add(LabelFactory.create("till.clear.account"));
        grid.add(account);
        getLayout().add(ColumnFactory.create("Inset", grid));
    }

    /**
     * Sets the till float amount.
     *
     * @param amount the till float amount
     */
    public void setAmount(BigDecimal amount) {
        this.amount.setText(amount.toString());
    }

    /**
     * Returns the till float amount.
     *
     * @return the till float amount. May be {@code null}
     */
    public BigDecimal getAmount() {
        BigDecimal result = null;
        try {
            result = new BigDecimal(amount.getText());
        } catch (NumberFormatException ignore) {
            // no-op
        }
        return result;
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
        if (getAmount() != null && getAccount() != null) {
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
        List<EntityRelationship> relationships = bean.getValues("depositAccounts", IsActiveRelationship.ACTIVE_NOW,
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
