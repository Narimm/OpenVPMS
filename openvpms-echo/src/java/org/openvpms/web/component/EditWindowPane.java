package org.openvpms.web.component;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.WindowPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.app.OpenVPMSApp;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class EditWindowPane extends WindowPane {

    /**
     * The editor.
     */
    private final Editor _editor;


    /**
     * Construct a new <code>EditWindowPane</code>.
     *
     * @param editor the editor
     */
    public EditWindowPane(Editor editor) {
        _editor = editor;
        setStyleName("EditWindowPane");
        setTitle(_editor.getTitle());
        setModal(true);

        Button apply;
        Button ok;
        Button delete;
        Button cancel;

        apply = ButtonFactory.create("apply", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onApply();
            }
        });
        ok = ButtonFactory.create("ok", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onOK();
            }
        });
        delete = ButtonFactory.create("delete", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        });
        cancel = ButtonFactory.create("cancel", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        Row row = RowFactory.create(apply, ok, delete, cancel);
        row.setStyleName("EditWindowPane.row");

        SplitPane pane = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                row, editor.getComponent());
        pane.setSeparatorPosition(new Extent(32));
        add(pane);

        OpenVPMSApp.getInstance().getContent().add(this);
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

    protected void close() {
        OpenVPMSApp.getInstance().getContent().remove(this);
    }

}
