package org.openvpms.web.workspace.admin.system;

import nextapp.echo2.app.Component;
import org.openvpms.web.component.workspace.TabComponent;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Abstract implementation of the {@link TabComponent} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractTabComponent implements TabComponent {

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The buttons.
     */
    private ButtonRow buttons;

    /**
     * The focus group.
     */
    private final FocusGroup focusGroup = new FocusGroup(getClass().getName());


    /**
     * Constructs an {@link AbstractTabComponent}.
     *
     * @param help the help context for the tab
     */
    public AbstractTabComponent(HelpContext help) {
        this.help = help;
        buttons = new ButtonRow(new FocusGroup(getClass().getName() + ".buttons"), "ControlRow",
                                ButtonRow.BUTTON_STYLE);
    }

    /**
     * Returns the button component.
     *
     * @return the button component, or {@code null} if this tab doesn't provide any buttons
     */
    @Override
    public Component getButtons() {
        return buttons;
    }

    /**
     * Returns the help context for the tab.
     *
     * @return the help context
     */
    @Override
    public HelpContext getHelpContext() {
        return help;
    }

    /**
     * Returns the buttons.
     *
     * @return the buttons
     */
    protected ButtonSet getButtonSet() {
        return buttons.getButtons();
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    protected FocusGroup getFocusGroup() {
        return focusGroup;
    }
}
