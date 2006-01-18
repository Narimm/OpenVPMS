package org.openvpms.web.app.subsystem;

import java.util.List;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.app.editor.IMObjectEditor;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.ButtonRow;
import org.openvpms.web.component.ColumnFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.SelectionDialog;
import org.openvpms.web.component.edit.EditButtonRow;
import org.openvpms.web.component.edit.EditButtonRow.Buttons;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.query.Browser;
import org.openvpms.web.component.query.BrowserDialog;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.util.Messages;


/**
 * Abstract CRUD pane.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public abstract class AbstractCRUDPane extends SplitPane {

    /**
     * The archetype reference model name, used to query objects.
     */
    private final String _refModelName;

    /**
     * The archetype entity name, used to query objects. May be
     * <code>null</code>.
     */
    private final String _entityName;

    /**
     * The archetype concept name, used to query objects. May be
     * <code>null</code>.
     */
    private final String _conceptName;

    /**
     * Localisation identifier.
     */
    private final String _id;

    /**
     * Heading label.
     */
    private Label _heading;

    /**
     * Selected customer's description.
     */
    private Label _description;

    /**
     * The editor.
     */
    private DelegatingEditor _editor;

    /**
     * The edit pane.
     */
    private SplitPane _editPane;


    /**
     * Construct a new <code>AbstractCRUDPane</code>.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @param id           the localisation identfifier
     */
    public AbstractCRUDPane(String refModelName, String entityName,
                            String conceptName,
                            String id) {
        super(ORIENTATION_VERTICAL);
        _refModelName = refModelName;
        _entityName = entityName;
        _conceptName = conceptName;
        _id = id;

        doLayout();
    }

    /**
     * Lay out the components.
     */
    protected void doLayout() {
        _heading = LabelFactory.create();
        _heading.setText(getHeading());
        Row heading = RowFactory.create("CRUDPane.Title", _heading);

        String key = _id + ".select";
        Button button = ButtonFactory.create(key, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSelect();
            }
        });

        _description = LabelFactory.create();
        Row control = RowFactory.create("CRUDPane.ControlRow", button, _description);
        Column top = ColumnFactory.create(heading, control);
        add(top);

        _editor = new DelegatingEditor();
        ButtonRow buttons = new EditButtonRow(_editor, Buttons.NEW_SAVE_DELETE);
        _editPane = new SplitPane(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                new Extent(32));              // @todo - stylehseet
        _editPane.add(buttons);
        add(_editPane);
    }

    /**
     * Returns the heading. This is diplayed at the top of the pane.
     *
     * @return the heading
     */
    protected String getHeading() {
        String result;
        if (_editor == null) {
            result = Messages.get("workspace." + _id);
        } else {
            result = _editor.getObject().getName();
            if (result == null) {
                result = Messages.get("workspace." + _id + ".new");
            }
        }
        return result;
    }

    /**
     * Create a new object. This delegates to {@link #create(String)} or {@link
     * #create(List<String>)} if if the archetype query criteria matches more
     * than one archetype
     */
    protected void create() {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<String> shortNames = service.getArchetypeShortNames(
                _refModelName, _entityName, _conceptName, true);
        if (shortNames.isEmpty()) {
            ErrorDialog.show("Cannot create object",
                    "No archetypes match reference model="
                            + _refModelName + ", entity=" + _entityName
                            + ", concept=" + _conceptName);
        } else if (shortNames.size() > 1) {
            create(shortNames);
        } else {
            create(shortNames.get(0));
        }
    }

    /**
     * Create a new object of the specified archetype, and make it the current
     * object for editing.
     *
     * @param shortName the archetype shortname
     */
    protected void create(String shortName) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        IMObject object = (IMObject) service.create(shortName);
        setObject(object);
    }

    /**
     * Create a new object, selected from a list. This implementation pops up a
     * selection dialog.
     *
     * @param shortNames the archetype shortnames
     */
    protected void create(List<String> shortNames) {
        final SelectionDialog dialog = new SelectionDialog("Select Archetype",
                "Select the type of object to create", shortNames);
        dialog.addActionListener(SelectionDialog.OK_ID, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String selected = (String) dialog.getSelected();
                if (selected != null) {
                    create(selected);
                }
            }
        });
        dialog.show();
    }

    /**
     * Invoked when the select button is pressed. This pops up an {@link
     * Browser} to select an object for editiing.
     */
    protected void onSelect() {
        final Browser browser = new Browser(_refModelName, _entityName,
                _conceptName);
        String title = Messages.get("label." + _id + ".select");
        final BrowserDialog popup = new BrowserDialog(title, browser);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                IMObject selected = popup.getSelected();
                if (selected != null) {
                    setObject(selected);
                }
            }
        });

        popup.show();
    }

    /**
     * Sets the object.
     *
     * @param object the object
     */
    protected void setObject(IMObject object) {
        _description.setText(object.getDescription());
        _editPane.remove(_editor.getComponent());
        _editor.setEditor(new IMObjectEditor(object));
        _editPane.add(_editor.getComponent());
        _heading.setText(getHeading());
    }

    /**
     * Helper class for delegating methods from an {@link EditButtonRow} to the
     * appropriate editor.
     */
    private class DelegatingEditor implements Editor {

        /**
         * The editor to delegate to.
         */
        private Editor _editor;

        /**
         * Set the editor to delegate to.
         */
        public void setEditor(Editor editor) {
            _editor = editor;
        }

        /**
         * Returns a title for the editor.
         *
         * @return a title for the editor
         */
        public String getTitle() {
            return (_editor != null) ? _editor.getTitle() : null;
        }

        /**
         * Returns the editing component.
         *
         * @return the editing component
         */
        public Component getComponent() {
            return (_editor != null) ? _editor.getComponent() : null;
        }

        /**
         * Returns the object being edited.
         *
         * @return the object being edited
         */
        public IMObject getObject() {
            return (_editor != null) ? _editor.getObject() : null;
        }

        /**
         * Create a new object.
         */
        public void create() {
            AbstractCRUDPane.this.create();
        }

        /**
         * Save any edits.
         */
        public void save() {
            if (_editor != null) {
                _editor.save();
            }
        }

        /**
         * Delete the current object.
         */
        public void delete() {
            if (_editor != null) {
                _editor.delete();
            }
        }

        /**
         * Cancel any edits.
         */
        public void cancel() {
            if (_editor != null) {
                _editor.cancel();
            }
        }

    }

}
