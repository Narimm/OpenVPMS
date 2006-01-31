package org.openvpms.web.app.subsystem;

import java.util.List;

import nextapp.echo2.app.Alignment;
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
import nextapp.echo2.app.layout.RowLayoutData;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.ButtonFactory;
import org.openvpms.web.component.ColumnFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.RowFactory;
import org.openvpms.web.component.SplitPaneFactory;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.SelectionDialog;
import org.openvpms.web.component.edit.DefaultIMObjectEditor;
import org.openvpms.web.component.edit.EditDialog;
import org.openvpms.web.component.edit.IMObjectEditor;
import org.openvpms.web.component.model.ArchetypeShortNameListModel;
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
     * Localised type display name (e.g, Customer, Product).
     */
    private final String _type;

    /**
     * The listener.
     */
    private CRUDWindowListener _listener;

    /**
     * Selected object's summary.
     */
    private Label _summary;

    /**
     * Deactivated label.
     */
    private Label _deactivated;

    /**
     * The object browser.
     */
    private IMObjectBrowser _browser;

    /**
     * Container for the selected object and edit buttons.
     */
    private SplitPane _container;

    /**
     * The edit button.
     */
    private Button _edit;

    /**
     * The delete button.
     */
    private Button _delete;


    /**
     * Construct a new <code>AbstractCRUDPane</code>.
     *
     * @param subsystemId  the subsystem localisation identifier
     * @param workspaceId  the workspace localisation identfifier
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public AbstractCRUDPane(String subsystemId, String workspaceId,
                            String refModelName, String entityName,
                            String conceptName) {
        super(ORIENTATION_VERTICAL);
        _subsystemId = subsystemId;
        _id = _subsystemId + "." + workspaceId;
        _refModelName = refModelName;
        _entityName = entityName;
        _conceptName = conceptName;
        _type = Messages.get(_id + ".type");

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
        _summary = LabelFactory.create();
        _deactivated = LabelFactory.create();
        RowLayoutData right = new RowLayoutData();
        right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.DEFAULT));
        _deactivated.setLayoutData(right);
        Row control = RowFactory.create("CRUDPane.ControlRow", select, _summary,
                _deactivated);

        Column top = ColumnFactory.create(headingRow, control);
        add(top);
        _edit = ButtonFactory.create("edit", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onEdit();
            }
        });
        _edit.setEnabled(false);
        Button create = ButtonFactory.create("new", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onNew();
            }
        });
        _delete = ButtonFactory.create("delete", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        });
        _delete.setEnabled(false);
        Row buttons = RowFactory.create("CRUDPane.ControlRow", create, _edit,
                _delete);
        _container = SplitPaneFactory.create(ORIENTATION_VERTICAL_BOTTOM_TOP,
                "CRUDPane.Container", buttons);
        add(_container);

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
        final ArchetypeShortNameListModel model
                = new ArchetypeShortNameListModel(shortNames, false);
        String title = Messages.get("imobject.new.title", _type);
        String message = Messages.get("imobject.new.message", _type);
        final SelectionDialog dialog
                = new SelectionDialog(title, message, model);
        dialog.addActionListener(SelectionDialog.OK_ID, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selected = dialog.getSelectedIndex();
                if (selected != -1) {
                    create(model.getShortName(selected));
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
        final DefaultIMObjectEditor editor = new DefaultIMObjectEditor(object, showAll);
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
        String title = Messages.get("imobject.select.title", _type);
        final BrowserDialog popup = new BrowserDialog(title, browser, true);

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
        if (_browser != null) {
            edit(_browser.getObject(), true);
        }
    }

    /**
     * Invoked when the delete button is pressed.
     */
    protected void onDelete() {
        if (_browser != null) {
            IMObject object = _browser.getObject();
            if (object instanceof Entity) {
                Entity entity = (Entity) object;
                if (!entity.getEntityRelationships().isEmpty()) {
                    if (object.isActive()) {
                        confirmDeactivate(object);
                    } else {
                        ErrorDialog.show("Delete",
                                "Cannot delete deactivated instance");
                    }
                } else {
                    confirmDelete(object);
                }
            } else {
                confirmDelete(object);
            }
        }
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     */
    protected void onEditCompleted(IMObjectEditor editor) {
        if (editor.isDeleted()) {
            onDeleted(editor.getObject());
        } else if (editor.isModified()) {
            onSaved(editor.getObject());
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     */
    protected void onSaved(IMObject object) {
        setObject(object);
        if (_listener != null) {
            _listener.saved(object);
        }
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
        clearObject();
        if (_listener != null) {
            _listener.deleted(object);
        }
    }

    /**
     * Sets the object.
     *
     * @param object the object
     */
    protected void setObject(IMObject object) {
        final String summaryKey = "imobject.summary";
        String summary = Messages.get(summaryKey, object.getName(),
                object.getDescription());
        _summary.setText(summary);
        if (!object.isActive()) {
            _deactivated.setText(Messages.get("imobject.deactivated"));
        }

        if (_browser != null) {
            _container.remove(_browser.getComponent());
        }
        _browser = new IMObjectBrowser(object);
        _container.add(_browser.getComponent());
        _edit.setEnabled(true);
        _delete.setEnabled(true);
    }

    /**
     * Clears the current object.
     */
    protected void clearObject() {
        if (_browser != null) {
            _container.remove(_browser.getComponent());
            _browser = null;
        }
        _summary.setText(null);
        _deactivated.setText(null);
        _edit.setEnabled(false);
        _delete.setEnabled(false);
    }


    /**
     * Pops up a dialog prompting if deactivation of an object should proceed,
     * deleting it if OK is selected.
     *
     * @param object the object to delete
     */
    private void confirmDeactivate(final IMObject object) {
        String title = Messages.get("imobject.deactivate.title", _type);
        String message = Messages.get("imobject.deactivate.message", object.getName());
        ConfirmationDialog dialog = new ConfirmationDialog(title, message);
        dialog.addActionListener(SelectionDialog.OK_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                try {
                    object.setActive(false);
                    service.save(object);
                    onSaved(object);
                } catch (ArchetypeServiceException exception) {
                    ErrorDialog.show(exception);
                }
            }
        });
        dialog.show();
    }

    /**
     * Pops up a dialog prompting if deletion of an object should proceed,
     * deleting it if OK is selected.
     *
     * @param object the object to delete
     */
    private void confirmDelete(final IMObject object) {
        String title = Messages.get("imobject.delete.title", _type);
        String message = Messages.get("imobject.delete.title", object.getName());
        ConfirmationDialog dialog = new ConfirmationDialog(title, message);
        dialog.addActionListener(SelectionDialog.OK_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                try {
                    service.remove(object);
                    onDeleted(object);
                } catch (ArchetypeServiceException exception) {
                    ErrorDialog.show(exception);
                }
            }
        });
        dialog.show();
    }


}
