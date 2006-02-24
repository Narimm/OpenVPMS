package org.openvpms.web.app.subsystem;

import echopointng.GroupBox;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.SelectionDialog;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.create.IMObjectCreatorListener;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.resource.util.Messages;

/**
 * Generic CRUD window.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class CRUDWindow {

    /**
     * The reference model name of the archetypes that this window may create.
     * May be <code>null</code>.
     */
    private final String _refModelName;

    /**
     * The entity name of the archetypes that this window may create. May be
     * <code>null</code>.
     */
    private final String _entityName;

    /**
     * The concept name of the archetypes that this window may create. May be
     * <code>null</code>.
     */
    private final String _conceptName;

    /**
     * Localised type display name (e.g, Customer, Product).
     */
    private final String _type;

    /**
     * The listener.
     */
    private CRUDWindowListener _listener;

    /**
     * The component representing this.
     */
    private Component _component;

    /**
     * The object viewer.
     */
    private IMObjectViewer _viewer;

    /**
     * The selected object container.
     */
    private Component _objectContainer;

    /**
     * The action button row.
     */
    private Row _buttons;

    /**
     * The edit button.
     */
    private Button _edit;

    /**
     * The delete button.
     */
    private Button _delete;


    /**
     * Edit button identifier.
     */
    protected static final String EDIT_ID = "edit";

    /**
     * New button identifier.
     */
    protected static final String NEW_ID = "new";

    /**
     * Delete button identifier.
     */
    protected static final String DELETE_ID = "delete";

    /**
     * The style name.
     */
    private static final String STYLE = "CRUDWindow";

    /**
     * Button row style.
     */
    private static final String ROW_STYLE = "ControlRow";


    /**
     * Create a new <code>CRUDWindow</code>.
     *
     * @param type         display name for the types of objects that this may
     *                     create
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public CRUDWindow(String type, String refModelName, String entityName,
                      String conceptName) {
        _type = type;
        _refModelName = refModelName;
        _entityName = entityName;
        _conceptName = conceptName;
    }

    /**
     * Sets the event listener.
     *
     * @param listener the event listener.
     */
    public void setListener(CRUDWindowListener listener) {
        _listener = listener;
    }

    /**
     * Returns the component representing this.
     *
     * @return the component
     */
    public Component getComponent() {
        if (_component == null) {
            doLayout();
        }
        return _component;
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(IMObject object) {
        if (object != null) {
            if (_objectContainer != null) {
                _objectContainer.remove(_viewer.getComponent());
            } else {
                _objectContainer = new GroupBox();
                _component.add(_objectContainer);
            }
            _viewer = new IMObjectViewer(object);
            _objectContainer.add(_viewer.getComponent());
            enableButtons(true);
        } else {
            if (_viewer != null) {
                _component.remove(_objectContainer);
                _objectContainer = null;
                _viewer = null;
            }
            enableButtons(false);
        }
    }

    /**
     * Lays out the component.
     */
    protected void doLayout() {
        _buttons = RowFactory.create(ROW_STYLE);
        layoutButtons(_buttons);
        enableButtons(false);
        _component = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                STYLE, _buttons);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    protected void layoutButtons(Row buttons) {
        _edit = ButtonFactory.create(EDIT_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onEdit();
            }
        });
        Button create = ButtonFactory.create(NEW_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onCreate();
            }
        });
        _delete = ButtonFactory.create(DELETE_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onDelete();
            }
        });
        buttons.add(_edit);
        buttons.add(create);
        buttons.add(_delete);
    }

    /**
     * Returns the button row.
     *
     * @return the button row
     */
    protected Row getButtons() {
        return _buttons;
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    protected void enableButtons(boolean enable) {
        if (enable) {
            if (_buttons.indexOf(_edit) == -1) {
                _buttons.add(_edit);
            }
            if (_buttons.indexOf(_delete) == -1) {
                _buttons.add(_delete);
            }
        } else {
            _buttons.remove(_edit);
            _buttons.remove(_delete);
        }
    }

    /**
     * Invoked when the 'new' button is pressed.
     */
    public void onCreate() {
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated(object);
            }
        };

        IMObjectCreator.create(_type, _refModelName, _entityName, _conceptName,
                listener);
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param object the new object
     */
    protected void onCreated(IMObject object) {
        edit(object, false);
    }

    /**
     * Invoked when the edit button is pressed. This popups up an {@link
     * EditDialog}.
     */
    protected void onEdit() {
        if (_viewer != null) {
            IMObject object = _viewer.getObject();
            if (object.isNew()) {
                edit(object, true);
            } else {
                // make sure the latest instance is being used.
                IArchetypeService service
                        = ServiceHelper.getArchetypeService();
                object = service.getById(object.getArchetypeId(),
                        object.getUid());
                if (object == null) {
                    ErrorDialog.show(_type + " has been deleted");
                } else {
                    edit(object, true);
                }
            }
        }
    }

    /**
     * Invoked when the delete button is pressed.
     */
    protected void onDelete() {
        if (_viewer != null) {
            IMObject object = _viewer.getObject();
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
     * @param isNew  determines if the object is a new instance
     */
    protected void onEditCompleted(IMObjectEditor editor, boolean isNew) {
        Context.getInstance().setEdited(null);
        if (editor.isDeleted()) {
            onDeleted(editor.getObject());
        } else if (editor.isSaved()) {
            onSaved(editor.getObject(), isNew);
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(IMObject object, boolean isNew) {
        setObject(object);
        if (_listener != null) {
            _listener.saved(object, isNew);
        }
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(IMObject object) {
        setObject(null);
        if (_listener != null) {
            _listener.deleted(object);
        }
    }

    /**
     * Edit an IMObject.
     *
     * @param object  the object to edit
     * @param showAll if <code>true</code> show optional and required fields;
     *                otherwise show required fields.
     */
    private void edit(IMObject object, boolean showAll) {
        final boolean isNew = object.isNew();
        final IMObjectEditor editor = IMObjectEditorFactory.create(object, showAll);
        EditDialog dialog = new EditDialog(editor);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                onEditCompleted(editor, isNew);
            }
        });

        Context.getInstance().setEdited(object);
        dialog.show();
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
                    onSaved(object, false);
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
