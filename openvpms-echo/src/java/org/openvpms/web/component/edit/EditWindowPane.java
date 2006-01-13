package org.openvpms.web.component.edit;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.component.dialog.PopupWindow;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class EditWindowPane extends PopupWindow {

    /**
     * The editor.
     */
    private final Editor _editor;

    /**
     * Apply button identifier.
     */
    private static final String APPLY_ID = "apply";

    /**
     * OK button identifier.
     */
    private static final String OK_ID = "ok";

    /**
     * Delete button identifier.
     */
    private static final String DELETE_ID = "delete";

    /**
     * Cancel button identifier.
     */
    private static final String CANCEL_ID = "cancel";

    /**
     * Edit window style name.
     */
    private static final String STYLE = "EditWindowPane";


    /**
     * Construct a new <code>EditWindowPane</code>.
     *
     * @param editor the editor
     */
    public EditWindowPane(Editor editor) {
        super(editor.getTitle(), STYLE);
        _editor = editor;
        setModal(true);

        addButton(APPLY_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onApply();
            }
        });
        addButton(OK_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onOK();
            }
        });
        addButton(DELETE_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        });
        addButton(CANCEL_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onCancel();
            }
        });

        getLayout().add(editor.getComponent());

        show();
    }


    /**
     * Save the current object.
     */
    protected void onApply() {
        _editor.save();
    }

    /**
     * Save the current object, and close the editor.
     */
    protected void onOK() {
        _editor.save();
        close();
    }

    /**
     * Delete the current object, and close the editor.
     */
    protected void onDelete() {
        _editor.delete();
        close();
    }

    /**
     * Close the editor, discarding any unsaved changes.
     */
    protected void onCancel() {
        _editor.cancel();
        close();
    }

}
