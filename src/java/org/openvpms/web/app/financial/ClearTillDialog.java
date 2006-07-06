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

import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.im.list.IMObjectListCellRenderer;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.SelectFieldFactory;
import org.openvpms.web.resource.util.Messages;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;

import nextapp.echo2.app.Grid;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.TextField;

import java.math.BigDecimal;
import java.util.List;


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ClearTillDialog extends PopupDialog {

    /**
     * The amount field.
     */
    private final TextField _amount;

    /**
     * The bank account selector.
     */
    private final SelectField _account;

    /**
     * Construct a new <code>ClearTillDialog</code>.
     */
    public ClearTillDialog() {
        super(Messages.get("till.clear.title"), "ClearTillDialog",
              Buttons.OK_CANCEL);
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        setModal(true);

        _amount = new TextField();

        String[] shortNames = {"party.organisationDeposit"};
        List<IMObject> objects = ArchetypeQueryHelper.get(service, shortNames,
                                                          true, 0,
                                                          ArchetypeQuery.ALL_ROWS).getRows();
        _account = SelectFieldFactory.create(objects);
        _account.setCellRenderer(new IMObjectListCellRenderer());

        Grid grid = GridFactory.create(2);
        grid.add(LabelFactory.create("till.amount"));
        grid.add(_amount);
        grid.add(LabelFactory.create("till.deposit"));
        grid.add(_account);
        getLayout().add(grid);
    }

    /**
     * Sets the till float amount.
     *
     * @param amount the till float amount
     */
    public void setAmount(BigDecimal amount) {
        _amount.setText(amount.toString());
    }

    /**
     * Returns the till float amount.
     *
     * @return the till float amount
     */
    public BigDecimal getAmount() {
        return new BigDecimal(_amount.getText());
    }

    /**
     * Returns the bank deposit organisation.
     *
     * @return the bank deposit organisation
     */
    public Party getAccount() {
        return (Party) _account.getSelectedItem();
    }
}
