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
import org.openvpms.web.component.bound.BoundTextComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;

import java.math.BigDecimal;

/**
 * Base class for till clearing dialogs.
 *
 * @author Tim Anderson
 */
public abstract class AbstractClearTillDialog extends PopupDialog {

    /**
     * The till float amount field.
     */
    private final Property cashFloat = new SimpleProperty("amount", BigDecimal.class);

    /**
     * Constructs an {@link AbstractClearTillDialog}.
     *
     * @param title the window title
     * @param help  the helper context
     */
    public AbstractClearTillDialog(String title, HelpContext help) {
        super(title, null, OK_CANCEL, help);
        setModal(true);
    }

    /**
     * Sets the till float amount.
     *
     * @param cashFloat the till float amount
     */
    public void setCashFloat(BigDecimal cashFloat) {
        this.cashFloat.setValue(cashFloat);
    }

    /**
     * Returns the till float amount.
     *
     * @return the till float amount. May be {@code null}
     */
    public BigDecimal getCashFloat() {
        return cashFloat.getBigDecimal();
    }

    /**
     * Adds the till float amount field to a grid.
     *
     * @param grid the grid to add to
     */
    protected void addAmount(Grid grid) {
        grid.add(LabelFactory.create("till.clear.amount"));
        grid.add(BoundTextComponentFactory.createNumeric(cashFloat, 10));
    }

}
