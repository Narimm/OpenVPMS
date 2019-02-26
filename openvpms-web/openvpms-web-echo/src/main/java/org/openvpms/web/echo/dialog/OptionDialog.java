package org.openvpms.web.echo.dialog;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.button.ButtonGroup;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;

/**
 * A dialog that prompts to select one of several options.
 *
 * @author Tim Anderson
 */
public class OptionDialog extends MessageDialog {

    /**
     * The options.
     */
    private final RadioButton[] options;

    /**
     * Construct a {@link OptionDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param options the options to display
     */
    public OptionDialog(String title, String message, String[] options) {
        this(title, message, options, null);
    }

    /**
     * Construct a {@link OptionDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param options the options to display
     * @param help    the help context
     */
    public OptionDialog(String title, String message, String[] options, HelpContext help) {
        this(title, message, options, OK_CANCEL, help);
    }

    /**
     * Constructs an {@link OptionDialog}.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param options the options to display
     * @param buttons the dialog buttons to display
     * @param help    the help context
     */
    public OptionDialog(String title, String message, String[] options, String[] buttons, HelpContext help) {
        super(title, message, buttons, help);

        ButtonGroup group = new ButtonGroup();
        this.options = new RadioButton[options.length];
        for (int i = 0; i < this.options.length; ++i) {
            RadioButton button = ButtonFactory.create(null, group);
            button.setText(options[i]);
            group.addButton(button);
            this.options[i] = button;
        }
    }

    /**
     * Selects an option.
     *
     * @param selected the option offset, or -1 to deselect any existing option.
     */
    public void setSelected(int selected) {
        if (selected >= 0 && selected < options.length) {
            options[selected].setSelected(true);
        } else {
            for (RadioButton button : options) {
                button.setSelected(false);
            }
        }
    }

    /**
     * Returns the selected option.
     *
     * @return the selected option, or {@code -1} if no option is selected
     */
    public int getSelected() {
        int selected = -1;
        for (int i = 0; i < options.length; ++i) {
            if (options[i].isSelected()) {
                selected = i;
                break;
            }
        }
        return selected;
    }

    /**
     * Invoked when the 'OK' button is pressed. If an option is selected, this sets the action and closes the window.
     */
    @Override
    protected void onOK() {
        if (getSelected() != -1) {
            super.onOK();
        }
    }

    /**
     * Lays out the component prior to display.
     */
    @Override
    protected void doLayout() {
        Label message = LabelFactory.create(true, true);
        message.setText(getMessage());
        Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, message);
        for (RadioButton button : options) {
            column.add(button);
        }
        getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
    }
}
