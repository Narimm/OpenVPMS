package org.openvpms.web.app.subsystem;

import java.util.List;

import echopointng.GroupBox;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.ColumnFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.SelectionDialog;
import org.openvpms.web.component.edit.EditDialog;
import org.openvpms.web.component.edit.IMObjectEditor;
import org.openvpms.web.component.query.Browser;
import org.openvpms.web.component.query.BrowserDialog;
import org.openvpms.web.component.query.IMObjectBrowser;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.util.Messages;


/**
 * Abstract implementation of the {@link CRUDWindow} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class AbstractCRUDPane extends SplitPane implements CRUDWindow {

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
     * The subsystem localisation identifier.
     */
    private final String _subsystemId;

    /**
     * The fully qualified localisation identifier: &lt;subsystemId&gt;.&lt;workspaceId&gt;
     */
    private final String _id;

    /**
     * The listener.
     */
    private CRUDWindowListener _listener;

    /**
     * Selected object's summary.
     */
    private Label _summary;

    /**
     * The object browser.
     */
    private IMObjectBrowser _browser;

    /**
     * Container for the selected object and edit button.
     */
    private Column _viewContainer;

    /**
     * Container for the selected object.
     */
    private GroupBox _container;


    /**
     * Construct a new <code>AbstractCRUDPane</code>.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public AbstractCRUDPane(String subsystemId, String workspaceId, String refModelName,
                            String entityName, String conceptName) {
        super(ORIENTATION_VERTICAL);
        _subsystemId = subsystemId;
        _id = _subsystemId + "." + workspaceId;
        _refModelName = refModelName;
        _entityName = entityName;
        _conceptName = conceptName;

        doLayout();
    }

    /**
     * Sets a listener for events.
     *
     * @param listener the listener
     */
    public void setCRUDPaneListener(CRUDWindowListener listener) {
        _listener = listener;
    }

    /**
     * Returns the CRUD component.
     *
     * @return the CRUD component
     */
    public Component getComponent() {
        return this;
    }

    /**
     * Lay out the components.
     */
    protected void doLayout() {
        Label heading = LabelFactory.create();
        heading.setText(getHeading());
        Row headingRow = RowFactory.create("CRUDPane.Title", heading);

        Button select = ButtonFactory.create("select", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onSelect();
            }
        });
        Button create = ButtonFactory.create("new", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onNew();
            }
        });
        _summary = LabelFactory.create();
        Row control = RowFactory.create("CRUDPane.ControlRow", select, create,
                _summary);

        Column top = ColumnFactory.create(headingRow, control);
        add(top);
    }

    /**
     * Returns the heading. This is diplayed at the top of the pane.
     *
     * @return the heading
     */
    protected String getHeading() {
        String subsystem = Messages.get("subsystem." + _subsystemId);
        String workspace = Messages.get("workspace." + _id);
        return subsystem + " - " + workspace;
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
        edit(object, false);
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
     * Edit an IMObject.
     *
     * @param object  the object to edit
     * @param showAll if <code>true</code> show optional and required fields;
     *                otherwise show required fields.
     */
    protected void edit(IMObject object, boolean showAll) {
        final IMObjectEditor editor = new IMObjectEditor(object, showAll);
        EditDialog dialog = new EditDialog(editor);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                onEditCompleted(editor);
            }
        });
        dialog.show();
    }

    /**
     * Invoked when the select button is pressed. This pops up an {@link
     * Browser} to select an object.
     */
    protected void onSelect() {
        final Browser browser = new Browser(_refModelName, _entityName,
                _conceptName);
        String title = Messages.get("label." + _id + ".select");
        final BrowserDialog popup = new BrowserDialog(title, browser);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (popup.createNew()) {
                    onNew();
                } else {
                    IMObject object = popup.getSelected();
                    if (object != null) {
                        onSelected(object);
                    }
                }
            }
        });

        popup.show();
    }

    /**
     * Invoked when the new button is pressed. This popups up an {@link
     * Editable}.
     */
    protected void onNew() {
        create();
    }

    /**
     * Invoked when an object is selected.
     *
     * @param object the selected object
     */
    protected void onSelected(IMObject object) {
        setObject(object);
        if (_listener != null) {
            _listener.selected(object);
        }
    }

    /**
     * Invoked when the edit button is pressed. This popups up an {@link
     * EditDialog}.
     */
    protected void onEdit() {
        edit(_browser.getObject(), true);
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     */
    protected void onEditCompleted(IMObjectEditor editor) {
        if (editor.isDeleted()) {
            onDelete(editor);
        } else if (editor.isModified()) {
            onSave(editor);
        }
    }

    /**
     * Invoked when the object is saved.
     *
     * @param editor the editor
     */
    protected void onSave(IMObjectEditor editor) {
        setObject(editor.getObject());
        if (_listener != null) {
            _listener.saved(editor.getObject());
        }
    }

    /**
     * Invoked when the delete button is pressed.
     *
     * @param editor the editor
     */
    protected void onDelete(IMObjectEditor editor) {
        clearObject();
        if (_listener != null) {
            _listener.deleted(editor.getObject());
        }
    }

    /**
     * Sets the object.
     *
     * @param object the object
     */
    protected void setObject(IMObject object) {
        String key = _id + ".summary";
        String summary = Messages.get(key, object.getName(),
                object.getDescription());
        _summary.setText(summary);

        if (_viewContainer == null) {
            Button edit = ButtonFactory.create("edit", new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onEdit();
                }
            });
            Row control = RowFactory.create("CRUDPane.ControlRow", edit);
            _container = new GroupBox();
            _viewContainer = ColumnFactory.create(_container, control);
            add(_viewContainer);
        } else {
            _container.remove(_browser.getComponent());
        }

        _browser = new IMObjectBrowser(object);
        _container.add(_browser.getComponent());
    }

    /**
     * Clears the current object.
     */
    protected void clearObject() {
        remove(_viewContainer);
        _viewContainer = null;
        _container = null;
        _browser = null;
        _summary.setText(null);
    }

}
