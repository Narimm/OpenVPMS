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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.charge;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.invoice.InvoiceItemStatus;
import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper to determine if an invoice being posted has undispensed orders.
 *
 * @author Tim Anderson
 */
public class UndispensedOrderChecker {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The undispensed items.
     */
    private final List<Act> items;


    /**
     * Constructs an {@link UndispensedOrderChecker} for a charge just prior to save.
     *
     * @param charge the charge
     */
    public UndispensedOrderChecker(Act charge) {
        service = ServiceHelper.getBean(IArchetypeService.class);
        items = getUndispensedItems(charge);
    }

    /**
     * Constructs an {@link UndispensedOrderChecker} for an editor just prior to save.
     *
     * @param editor the editor
     */
    public UndispensedOrderChecker(CustomerChargeActEditor editor) {
        service = ServiceHelper.getBean(IArchetypeService.class);
        items = getUndispensedItems(editor);
    }

    /**
     * Determines if there are any undispensed items.
     *
     * @return {@code true} if there are any undispensed items.
     */
    public boolean hasUndispensedItems() {
        return !items.isEmpty();
    }

    /**
     * Returns the undispensed items.
     *
     * @return the undispensed items
     */
    public List<Act> getUndispensedItems() {
        return items;
    }

    /**
     * Determines if an invoice is being posted and if it has any undispensed items.
     * <p/>
     * If so, displays a confirmation dialog.
     *
     * @param help     the help context
     * @param listener the listener to invoke if no confirmation is required, or the user confirms posting
     */
    public void confirm(HelpContext help, final Runnable listener) {
        if (!items.isEmpty()) {
            UndispensedOrderDialog dialog = new UndispensedOrderDialog(items, help);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    listener.run();
                }
            });
            dialog.show();
        } else {
            listener.run();
        }
    }

    /**
     * Returns the undispensed items for a charge.
     *
     * @param charge the charge
     * @return the undispensed items
     */
    private List<Act> getUndispensedItems(Act charge) {
        List<Act> items = Collections.emptyList();
        if (charge.isA(CustomerAccountArchetypes.INVOICE)) {
            IMObjectBean bean = service.getBean(charge);
            items = new ArrayList<>();
            for (Act item : bean.getTargets("items", Act.class)) {
                IMObjectBean itemBean = service.getBean(item);
                boolean ordered = InvoiceItemStatus.ORDERED.equalsIgnoreCase(item.getStatus());
                if (ordered) {
                    BigDecimal quantity = itemBean.getBigDecimal("quantity", BigDecimal.ZERO);
                    BigDecimal received = itemBean.getBigDecimal("receivedQuantity", BigDecimal.ZERO);
                    if (!MathRules.equals(quantity, received)) {
                        items.add(item);
                    }
                }
            }
        }
        return items;
    }

    /**
     * Returns the undispensed items for a charge editor.
     *
     * @param editor the charge editor
     * @return the undispensed items
     */
    private List<Act> getUndispensedItems(CustomerChargeActEditor editor) {
        List<Act> items = Collections.emptyList();
        if (editor.getObject().isA(CustomerAccountArchetypes.INVOICE) && ActStatus.POSTED.equals(editor.getStatus())) {
            items = editor.getNonDispensedItems();
        }
        return items;
    }

}

