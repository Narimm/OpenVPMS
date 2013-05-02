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

package org.openvpms.web.app.customer.charge;


import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.app.customer.CustomerActCRUDWindow;
import org.openvpms.web.app.workflow.payment.PaymentWorkflow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.DefaultActActions;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.PrintActTask;
import org.openvpms.web.component.workflow.ReloadTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.Tasks;

import java.math.BigDecimal;

import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.COUNTER;
import static org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes.INVOICE;


/**
 * CRUD window for customer charges.
 *
 * @author Tim Anderson
 */
public class ChargeCRUDWindow extends CustomerActCRUDWindow<FinancialAct> {

    /**
     * Constructs a {@code ChargeCRUDWindow}.
     * <p/>
     * This makes the default archetype {@link CustomerAccountArchetypes#INVOICE}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public ChargeCRUDWindow(Archetypes<FinancialAct> archetypes, Context context, HelpContext help) {
        super(Archetypes.create(archetypes.getShortNames(), archetypes.getType(), INVOICE, archetypes.getDisplayName()),
              DefaultActActions.<FinancialAct>getInstance(), context, help);
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(FinancialAct object) {
        super.setObject(object);
        updateContext(CustomerAccountArchetypes.INVOICE, object);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createPostButton());
        buttons.add(createPreviewButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(POST_ID, enable);
        buttons.setEnabled(PREVIEW_ID, enable);
    }

    /**
     * Invoked when posting of an act is complete.
     * <p/>
     * This prompts to pay the account, and pops up a dialog to print the act.
     *
     * @param act the act
     */
    @Override
    protected void onPosted(final FinancialAct act) {
        HelpContext help = getHelpContext().subtopic("post");
        Tasks tasks = new Tasks(help);
        TaskContext context = new DefaultTaskContext(getContext(), help);
        context.addObject(act);
        String shortName = act.getArchetypeId().getShortName();
        BigDecimal total = act.getTotal();
        if (TypeHelper.isA(act, INVOICE, COUNTER)) {
            PaymentWorkflow payment = new PaymentWorkflow(total, getContext(), help);
            payment.setRequired(false);
            tasks.addTask(payment);
            // need to reload the act as it may be changed via the payment
            // workflow as part of the CustomerAccountRules
            tasks.addTask(new ReloadTask(shortName));
            tasks.addTaskListener(new DefaultTaskListener() {
                public void taskEvent(TaskEvent event) {
                    // force a refresh so the summary updates
                    onRefresh(act);
                }
            });
        }
        PrintActTask print = new PrintActTask(shortName, getMailContext());
        print.setRequired(false);
        print.setEnableSkip(false);
        tasks.addTask(print);
        tasks.start(context);
    }

}
