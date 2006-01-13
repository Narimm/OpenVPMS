package org.openvpms.web.component.dialog;

import nextapp.echo2.app.Label;

import org.openvpms.web.component.LabelFactory;


/**
 * A generic modal dialog that displays a message.
 */
public abstract class MessageDialog extends PopupDialog {

    /**
     * Dialog style name.
     */
    private static final String STYLE = "MessageDialog";

    /**
     * Content label style.
     */
    private static final String LABEL_STYLE = "MessageDialog.Label";


    /**
     * Creates a new <code>MessageDialog</code>.
     *
     * @param title   the dialog title
     * @param message the message to display
     * @param buttons the buttons to display
     */
    public MessageDialog(String title, String message, Buttons buttons) {
        super(title, STYLE, buttons);
        setClosable(false);
        setModal(true);

        Label content = LabelFactory.create(null, LABEL_STYLE);
        content.setText(message);
        getLayout().add(content);
    }

}
