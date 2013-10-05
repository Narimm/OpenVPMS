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

package org.openvpms.web.workspace.workflow.otc;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.PracticeMailContext;
import org.openvpms.web.component.im.util.DefaultIMObjectDeletionListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.SilentIMObjectDeleter;
import org.openvpms.web.component.workflow.DefaultTaskContext;
import org.openvpms.web.component.workflow.DefaultTaskListener;
import org.openvpms.web.component.workflow.EditIMObjectTask;
import org.openvpms.web.component.workflow.PrintIMObjectTask;
import org.openvpms.web.component.workflow.SynchronousTask;
import org.openvpms.web.component.workflow.TaskContext;
import org.openvpms.web.component.workflow.TaskEvent;
import org.openvpms.web.component.workflow.TaskProperties;
import org.openvpms.web.component.workflow.UpdateIMObjectTask;
import org.openvpms.web.component.workflow.WorkflowImpl;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;

import java.util.Arrays;


/**
 * Over-the-counter workflow.
 *
 * @author Tim Anderson
 */
public class OverTheCounterWorkflow extends WorkflowImpl {

    /**
     * The initial context.
     */
    private TaskContext initial;

    /**
     * location over-the-counter entity relationship short name.
     */
    private static final String LOCATION_OTC = "entityRelationship.locationOTC";


    /**
     * Constructs an {@code OverTheCounterWorkflow}.
     *
     * @param parent the parent context
     * @param help   the help context
     * @throws ArchetypeServiceException for any archetype service error
     * @throws OTCException              for any OTC error
     */
    public OverTheCounterWorkflow(final Context parent, HelpContext help) {
        super(help);
        initial = new DefaultTaskContext(null, help);
        Party location = parent.getLocation();
        if (location == null) {
            throw new OTCException(OTCException.ErrorCode.NoLocation);
        }
        EntityBean bean = new EntityBean(location);
        Party otc = (Party) bean.getTargetEntity(LOCATION_OTC);
        if (otc == null) {
            throw new OTCException(OTCException.ErrorCode.NoOTC, location.getName());
        }
        initial.setCustomer(otc);
        initial.setTill(parent.getTill());
        initial.setLocation(parent.getLocation());
        initial.setPractice(parent.getPractice());
        initial.setUser(parent.getUser());

        EditIMObjectTask charge = createChargeTask();
        charge.setDeleteOnCancelOrSkip(true);
        addTask(charge);
        EditIMObjectTask payment = createPaymentTask();
        payment.setDeleteOnCancelOrSkip(true);
        payment.addTaskListener(new DefaultTaskListener() {
            @Override
            public void taskEvent(TaskEvent event) {
                if (event.getType().equals(TaskEvent.Type.CANCELLED)) {
                    removeCharge();
                }
            }
        });
        addTask(payment);
        addTask(new SynchronousTask() {
            @Override
            public void execute(TaskContext context) {
                Act charge = (Act) context.getObject(CustomerAccountArchetypes.COUNTER);
                Act payment = (Act) context.getObject(CustomerAccountArchetypes.PAYMENT);
                charge.setStatus(ActStatus.POSTED);
                payment.setStatus(ActStatus.POSTED);
                ServiceHelper.getArchetypeService().save(Arrays.asList(charge, payment));
            }
        });

        // optionally select and print the counter charge
        PrintIMObjectTask printTask = createPrintTask(parent);
        printTask.setRequired(false);

        // task to save the act.customerAccountChargesCounter, setting its
        // 'printed' flag, if the document is printed.
        TaskProperties saveProperties = new TaskProperties();
        saveProperties.add("printed", true);
        UpdateIMObjectTask saveSale = new UpdateIMObjectTask(CustomerAccountArchetypes.PAYMENT, saveProperties, true);
        addTask(printTask);
        addTask(saveSale);

        // add a task to update the global context at the end of the workflow
        addTask(new SynchronousTask() {
            public void execute(TaskContext context) {
                parent.setTill(context.getTill());
                parent.setClinician(context.getClinician());
            }
        });

        setBreakOnSkip(true);
    }

    /**
     * Creates a task to edit the charge.
     *
     * @return a new task
     */
    protected EditIMObjectTask createChargeTask() {
        return new OTCChargeTask();
    }

    /**
     * Creates a task to edit the payment.
     *
     * @return a new task
     */
    private EditIMObjectTask createPaymentTask() {
        return new OTCPaymentTask();
    }

    /**
     * Creates a task to print the charge.
     *
     * @param parent the parent context
     * @return a new task
     */
    protected PrintIMObjectTask createPrintTask(Context parent) {
        return new PrintIMObjectTask(CustomerAccountArchetypes.COUNTER, new PracticeMailContext(parent));
    }

    /**
     * Starts the workflow.
     */
    @Override
    public void start() {
        super.start(initial);
    }

    /**
     * Removes the <em>act.customerAccountChargesCounter</em> when the payment is cancelled.
     */
    protected void removeCharge() {
        IMObject charge = initial.getObject(CustomerAccountArchetypes.COUNTER);
        charge = IMObjectHelper.reload(charge);
        if (charge != null) {
            // this will fail if someone has subsequently posted the charge.
            SilentIMObjectDeleter deleter = new SilentIMObjectDeleter(getContext());
            deleter.delete(charge, getHelpContext(), new DefaultIMObjectDeletionListener());
        }
    }

}
