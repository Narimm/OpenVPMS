package org.openvpms.web.workspace.reporting.reminder;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.PopupDialog;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;

/**
 * Displays a {@link ReminderItemProgressBarProcessor} in a popup dialog, and supports cancellation.
 *
 * @author Tim Anderson
 */
class ReminderItemProgressBarDialog extends PopupDialog {

    /**
     * The processor.
     */
    private final ReminderItemProgressBarProcessor processor;

    /**
     * Constructs a {@link ReminderItemProgressBarDialog}.
     *
     * @param processor the processor
     */
    public ReminderItemProgressBarDialog(String title, String message, ReminderItemProgressBarProcessor processor) {
        super(title, CANCEL);
        this.processor = processor;
        setModal(true);
        Label label = LabelFactory.create();
        label.setText(message);
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, label, processor.getComponent());
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }

    /**
     * Show the window.
     */
    @Override
    public void show() {
        super.show();
        processor.process();
        processor.setListener(new BatchProcessorListener() {
            @Override
            public void completed() {
                close();
            }

            @Override
            public void error(Throwable exception) {
                ErrorHelper.show(exception);
            }
        });
    }

    /**
     * Invoked when the 'cancel' button is pressed.
     */
    @Override
    protected void onCancel() {
        processor.cancel();
        super.onCancel();
    }
}
