package org.openvpms.web.component.dialog;

import nextapp.echo2.app.Label;

import org.openvpms.web.component.util.LabelFactory;


/**
 * A modal dialog that prompts the user to select an OK or Cancel button.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ConfirmationDialog extends PopupDialog {

    /**
     * Dialog style name.
     */
    private static final String STYLE = "ConfirmationDialog";

    /**
     * Content label style.
     */
    private static final String LABEL_STYLE = "ConfirmationDialog.Label";


    /**
     * Construct a new <code>ConfirmationDialog</code>
     *
     * @param title the window title
     */
    public ConfirmationDialog(String title, String message) {
        super(title, STYLE, Buttons.OK_CANCEL);
        Label prompt = LabelFactory.create(null, LABEL_STYLE);
        prompt.setText(message);
        getLayout().add(prompt);
    }

}
