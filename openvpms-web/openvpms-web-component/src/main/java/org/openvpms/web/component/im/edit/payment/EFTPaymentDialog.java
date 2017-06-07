package org.openvpms.web.component.im.edit.payment;

import nextapp.echo2.app.*;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import nextapp.echo2.webcontainer.ContainerContext;
import org.apache.commons.lang.StringUtils;
import org.openvpms.pos.api.Prompt;
import org.openvpms.pos.api.Terminal;
import org.openvpms.pos.api.Transaction;
import org.openvpms.web.component.bound.BoundRadioButton;
import org.openvpms.web.component.error.ErrorFormatter;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;

import java.util.List;

/**
 * EFTPOS payment dialog.
 *
 * @author Tim Anderson
 */
class EFTPaymentDialog extends ModalDialog {

    /**
     * The POS terminal.
     */
    private final Terminal terminal;

    /**
     * The POS transaction.
     */
    private Transaction transaction;

    /**
     * Container for POS messages.
     */
    private final Component container;

    /**
     * The task queue, in order to asynchronously trigger processing.
     */
    private TaskQueueHandle taskQueue;

    /**
     * Constructs a {@link EFTPaymentDialog}.
     */
    public EFTPaymentDialog(Terminal terminal, Transaction transaction) {
        super("EFT", "MessageDialog", OK_CANCEL);
        this.terminal = terminal;
        this.transaction = transaction;
        getButtons().setEnabled(OK_ID, false);
        container = ColumnFactory.create(Styles.LARGE_INSET);
    }

    /**
     * Show the window.
     */
    @Override
    public void show() {
        super.show();
        queueTask(false);
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        display();
        getLayout().add(container);
    }

    @Override
    public void dispose() {
        removeTaskQueue();
        super.dispose();
    }

    /**
     * Cancels the operation.
     * <p>
     * This implementation closes the dialog, setting the action to {@link #CANCEL_ID}.
     */
    @Override
    protected void doCancel() {
        if (transaction.getStatus() == Transaction.Status.PENDING
                || transaction.getStatus() == Transaction.Status.PROMPT) {
            transaction.cancel();
        }
        super.doCancel();
    }

    protected void refresh() {
        try {
            Transaction latest = terminal.get(transaction.getId());
            if (latest != null) {
                transaction = latest;
            }
            display();
            queueTask(false);
        } catch (Exception exception) {
            ErrorHelper.show(exception, new WindowPaneListener() {
                @Override
                public void windowPaneClosing(WindowPaneEvent windowPaneEvent) {
                    queueTask(true);
                }
            });
        }
    }

    protected void display() {
        container.removeAll();
        Label content = LabelFactory.create(true, true);
        Column options = ColumnFactory.create(Styles.CELL_SPACING);
        try {
            List<String> messages = transaction.getMessages();
            if (!messages.isEmpty()) {
                String message = StringUtils.join(messages, '\n');
                content.setText(message);
            }
            final Prompt prompt = transaction.getPrompt();
            if (prompt != null) {
                final Property property = new SimpleProperty("option", null, String.class);
                ButtonGroup group = new ButtonGroup();
                for (final String option : prompt.getOptions()) {
                    RadioButton button = new BoundRadioButton(property, group, option);
                    button.setText(option);
                    options.add(button);
                }
                property.addModifiableListener(new ModifiableListener() {
                    @Override
                    public void modified(Modifiable modifiable) {
                        String option = property.getString();
                        if (option != null) {
                            prompt.send(option);
                            queueTask(true);
                        }
                    }
                });
            }
        } catch (Throwable exception) {
            content.setText(ErrorFormatter.format(exception));
        }
        container.add(ColumnFactory.create(Styles.CELL_SPACING, content, options));
    }

    private void queueTask(boolean prompt) {
        Transaction.Status status = transaction.getStatus();
        boolean enableOK = false;
        boolean enableCancel = true;
        if (status == Transaction.Status.PENDING || prompt) {
            getApplicationInstance().enqueueTask(getTaskQueue(), new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            });
        } else if (status == Transaction.Status.APPROVED) {
            enableOK = true;
            enableCancel = false;
        }
        getButtons().setEnabled(OK_ID, enableOK);
        getButtons().setEnabled(CANCEL_ID, enableCancel);
    }

    /**
     * Returns the task queue, creating it if it doesn't exist.
     *
     * @return the task queue
     */
    private TaskQueueHandle getTaskQueue() {
        if (taskQueue == null) {
            ApplicationInstance app = ApplicationInstance.getActive();
            taskQueue = app.createTaskQueue();
            ContainerContext context
                    = (ContainerContext) app.getContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME);
            if (context != null) {
                // set the task queue to call back in 1s
                context.setTaskQueueCallbackInterval(taskQueue, 1000);
            }
        }
        return taskQueue;
    }

    /**
     * Cleans up the task queue.
     */
    private void removeTaskQueue() {
        if (taskQueue != null) {
            final ApplicationInstance app = ApplicationInstance.getActive();
            app.removeTaskQueue(taskQueue);
            taskQueue = null;
        }
    }

}
