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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.till;

import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.resource.i18n.Messages;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.AttributeSet;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import java.math.BigDecimal;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CASH;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CHEQUE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_CREDIT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.PAYMENT_EFT;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CASH;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CHEQUE;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.REFUND_CREDIT;

/**
 * Helper to open a cash drawer associated with a till.
 *
 * @author Tim Anderson
 */
public class CashDrawer {

    /**
     * The printer name.
     */
    private final String printer;

    /**
     * The open command.
     */
    private byte[] command;


    /**
     * Constructs a {@link CashDrawer}.
     *
     * @param till the till
     */
    public CashDrawer(Entity till) {
        IMObjectBean bean = new IMObjectBean(till);
        printer = bean.getString("printerName");
        String drawerCommand = bean.getString("drawerCommand");
        if (!StringUtils.isEmpty(drawerCommand)) {
            command = getCommand(drawerCommand);
        }
    }

    /**
     * Determines if the drawer can be opened.
     *
     * @return {@code true} if the drawer can be opened
     */
    public boolean canOpen() {
        return !StringUtils.isEmpty(printer) && command != null;
    }

    /**
     * Determines if an act needs the cash drawer open.
     * <p/>
     * The cash drawer needs to be opened for POSTED payments or refunds that have cash, cheque, or credit items,
     * or payment EFT items with a non-zero cash out.
     * <p/>
     * Credit items return {@code true} to support users that put the receipt in the drawer.
     *
     * @param act the act
     * @return {@code true} if the act needs the cash drawer open
     */
    public boolean needsOpen(Act act) {
        boolean result = false;
        if (ActStatus.POSTED.equals(act.getStatus()) && TypeHelper.isA(act, PAYMENT, REFUND)) {
            ActBean bean = new ActBean(act);
            for (Act item : bean.getNodeActs("items")) {
                if (TypeHelper.isA(item, PAYMENT_CASH, PAYMENT_CHEQUE, PAYMENT_CREDIT, REFUND_CASH, REFUND_CHEQUE,
                                   REFUND_CREDIT)) {
                    result = true;
                    break;
                } else if (TypeHelper.isA(item, PAYMENT_EFT)) {
                    ActBean itemBean = new ActBean(item);
                    BigDecimal cashout = itemBean.getBigDecimal("cashout", BigDecimal.ZERO);
                    if (cashout.compareTo(BigDecimal.ZERO) != 0) {
                        result = true;
                        break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Opens the drawer.
     *
     * @throws PrintException if the drawer cannot be opened
     */
    public void open() throws PrintException {
        AttributeSet attrSet = new HashPrintServiceAttributeSet(new PrinterName(printer, null));
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, attrSet);
        if (printServices.length == 0) {
            throw new PrintException(Messages.format("till.drawerCommand.printernotfound", printer));
        }
        DocPrintJob job = printServices[0].createPrintJob();
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(command, flavor, null);
        job.print(doc, null);
    }

    /**
     * Parses the open drawer command string.
     *
     * @param command the open drawer command
     * @return the control codes, or {@code null} if there are none or they are invalid
     */
    private byte[] getCommand(String command) {
        byte[] result = null;
        String[] codes = command.split(",");
        if (codes.length != 0) {
            result = new byte[codes.length];
            try {
                for (int i = 0; i < codes.length; ++i) {
                    int value = Integer.valueOf(codes[i].trim());
                    result[i] = (byte) value;
                }
            } catch (NumberFormatException exception) {
                result = null;
            }
        }
        return result;
    }

}
