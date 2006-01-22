package org.openvpms.web.component.edit;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.component.ButtonRow;


/**
 * A row of edit buttons connected to an {@link Editor}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class EditButtonRow extends ButtonRow {

    /**
     * New button identifier.
     */
    public static final String NEW_ID = "new";

    /**
     * Save button identifier.
     */
    public static final String SAVE_ID = "save";

    /**
     * Apply button identifier.
     */
    public static final String APPLY_ID = "apply";

    /**
     * OK button identifier.
     */
    public static final String OK_ID = "ok";

    /**
     * Delete button identifier.
     */
    public static final String DELETE_ID = "delete";

    /**
     * Cancel button identifier.
     */
    public static final String CANCEL_ID = "cancel";


    /**
     * Used to indicate which buttons to display.
     */
    public static enum Buttons {
        NEW_SAVE_DELETE,
        APPLY_OK_DELETE_CANCEL
    }

    /**
     * The editor.
     */
    private Editor _editor;


    /**
     * Construct a new <code>EditButtonRow</code>.
     *
     * @param editor  the editor
     * @param buttons the buttons to display
     */
    public EditButtonRow(Editor editor, Buttons buttons) {
        _editor = editor;

        if (buttons == Buttons.NEW_SAVE_DELETE) {
            addButton(NEW_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onNew();
                }
            });
            addButton(SAVE_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onSave();
                }
            });
            addButton(DELETE_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onDelete();
                }
            });
        } else {
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
        }
    }

    /**
     * Create a new object.
     */
    protected void onNew() {
        _editor.create();
    }

    /**
     * Save the current object.
     */
    protected void onSave() {
        _editor.save();
    }

    /**
     * Save the current object.
     */
    protected void onApply() {
        onSave();
    }

    /**
     * Save the current object.
     */
    protected void onOK() {
        onSave();
    }

    /**
     * Delete the current object.
     */
    protected void onDelete() {
        _editor.delete();
    }

    /**
     * Discard any unsaved changes.
     */
    protected void onCancel() {
        _editor.cancel();
    }

}
