package org.openvpms.web.component.edit;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.component.dialog.PopupWindow;


/**
 * A popup window that displays an {@link IMObjectEditor}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class EditDialog extends PopupWindow {

    /**
     * The editor.
     */
    private final IMObjectEditor _editor;

    /**
     * The editor component.
     */
    private Component _component;

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
    private static final String STYLE = "EditDialog";


    /**
     * Construct a new <code>EditDialog</code>.
     *
     * @param editor the editor
     */
    public EditDialog(IMObjectEditor editor) {
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
        _component = _editor.getComponent();
        getLayout().add(_component);
        _editor.setLayoutListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onLayout();
            }
        });
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
        if (_editor.save()) {
            close();
        }
    }

    /**
     * Delete the current object, and close the editor.
     */
    protected void onDelete() {
        if (_editor.delete()) {
            close();
        }
    }

    /**
     * Close the editor, discarding any unsaved changes.
     */
    protected void onCancel() {
        _editor.cancel();
        close();
    }

    /**
     * Invoked when the layout changes.
     */
    protected void onLayout() {
        getLayout().remove(_component);
        _component = _editor.getComponent();
        getLayout().add(_component);
    }

}
