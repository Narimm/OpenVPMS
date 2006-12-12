/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.create.IMObjectCreatorListener;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMObjectPrinter;
import org.openvpms.web.component.im.print.IMObjectPrinterFactory;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Abstract implementation of the {@link CRUDWindow} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AbstractCRUDWindow<T extends IMObject> implements CRUDWindow<T> {

    /**
     * The object.
     */
    private T _object;

    /**
     * Short names of archetypes that this may create.
     */
    private final ShortNames _shortNames;

    /**
     * Localised type display name (e.g, Customer, Product).
     */
    private final String _type;

    /**
     * The listener.
     */
    private CRUDWindowListener<T> _listener;

    /**
     * The component representing this.
     */
    private Component _component;

    /**
     * The action button row.
     */
    private Row _buttons;

    /**
     * The edit button.
     */
    private Button _edit;

    /**
     * The create button.
     */
    private Button _create;

    /**
     * The delete button.
     */
    private Button _delete;

    /**
     * The print button.
     */
    private Button _print;

    /**
     * Edit button identifier.
     */
    private static final String EDIT_ID = "edit";

    /**
     * New button identifier.
     */
    private static final String NEW_ID = "new";

    /**
     * Delete button identifier.
     */
    private static final String DELETE_ID = "delete";

    /**
     * Print button identifier.
     */
    private static final String PRINT_ID = "print";

    /**
     * Button row style.
     */
    private static final String ROW_STYLE = "ControlRow";


    /**
     * Constructs a new <code>AbstractCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public AbstractCRUDWindow(String type, ShortNames shortNames) {
        _type = type;
        _shortNames = shortNames;
    }

    /**
     * Sets the event listener.
     *
     * @param listener the event listener.
     */
    public void setListener(CRUDWindowListener<T> listener) {
        _listener = listener;
    }

    /**
     * Returns the event listener.
     *
     * @return the event listener
     */
    public CRUDWindowListener<T> getListener() {
        return _listener;
    }

    /**
     * Returns the component representing this.
     *
     * @return the component
     */
    public Component getComponent() {
        if (_component == null) {
            _component = doLayout();
        }
        return _component;
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(T object) {
        _object = object;
        GlobalContext.getInstance().setCurrent(object);
        getComponent();
        if (object != null) {
            enableButtons(true);
        } else {
            enableButtons(false);
        }
    }

    /**
     * Returns the object.
     *
     * @return the object, or <code>null</code> if there is none set
     */
    public T getObject() {
        return _object;
    }

    /**
     * Returns the object's archetype descriptor.
     *
     * @return the object's archetype descriptor or <code>null</code> if there
     *         is no object set
     */
    public ArchetypeDescriptor getArchetypeDescriptor() {
        T object = getObject();
        ArchetypeDescriptor archetype = null;
        if (object != null) {
            archetype = DescriptorHelper.getArchetypeDescriptor(object);
        }
        return archetype;
    }

    /**
     * Invoked when the 'new' button is pressed.
     */
    public void onCreate() {
        onCreate(_type, getShortNames());
    }

    /**
     * Returns display name for the types of objects that this may create.
     *
     * @return the display name for the types of objects that this may create
     */
    protected String getTypeDisplayName() {
        return _type;
    }

    /**
     * Returns the short names of the archetypes that this may create.
     *
     * @return the short names
     */
    protected ShortNames getShortNames() {
        return _shortNames;
    }

    /**
     * Lays out the component.
     */
    protected Component doLayout() {
        _buttons = RowFactory.create(ROW_STYLE);
        layoutButtons(_buttons);
        enableButtons(false);
        return _buttons;
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    protected void layoutButtons(Row buttons) {
        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
    }

    /**
     * Returns the edit button.
     *
     * @return the edit button
     */
    protected Button getEditButton() {
        if (_edit == null) {
            _edit = ButtonFactory.create(EDIT_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onEdit();
                }
            });
        }
        return _edit;
    }

    /**
     * Returns the create button.
     *
     * @return the create button
     */
    protected Button getCreateButton() {
        if (_create == null) {
            _create = ButtonFactory.create(NEW_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onCreate();
                }
            });
        }
        return _create;
    }

    /**
     * Returns the create button.
     *
     * @return the create button
     */
    protected Button getDeleteButton() {
        if (_delete == null) {
            _delete = ButtonFactory.create(DELETE_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onDelete();
                }
            });
        }
        return _delete;
    }

    /**
     * Returns the print button.
     *
     * @return the print button
     */
    protected Button getPrintButton() {
        if (_print == null) {
            _print = ButtonFactory.create(PRINT_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onPrint();
                }
            });
        }
        return _print;
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
     *
     * @param type       localised type display name
     * @param shortNames the short names
     */
    protected void onCreate(String type, ShortNames shortNames) {
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated((T) object);
            }

            public void cancelled() {
                // ignore
            }
        };

        IMObjectCreator.create(type, shortNames.getShortNames(), listener);
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param object the new object
     */
    protected void onCreated(T object) {
        edit(object);
    }

    /**
     * Invoked when the edit button is pressed. This popups up an {@link
     * EditDialog}.
     */
    protected void onEdit() {
        T object = getObject();
        if (object != null) {
            if (object.isNew()) {
                edit(object);
            } else {
                // make sure the latest instance is being used.
                object = IMObjectHelper.reload(object);
                if (object == null) {
                    ErrorDialog.show(Messages.get("imobject.noexist"), _type);
                } else {
                    edit(object);
                }
            }
        }
    }

    /**
     * Invoked when the delete button is pressed.
     */
    protected void onDelete() {
        T object = getObject();
        if (object instanceof Entity) {
            Entity entity = (Entity) object;
            if (!entity.getEntityRelationships().isEmpty()) {
                if (object.isActive()) {
                    confirmDeactivate(object);
                } else {
                    String message = Messages.get(
                            "imobject.delete.deactivate",
                            getArchetypeDescriptor().getDisplayName());
                    ErrorDialog.show(message);
                }
            } else {
                confirmDelete(object);
            }
        } else {
            confirmDelete(object);
        }
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    protected void onPrint() {
        T object = getObject();
        IMObjectPrinter<T> printer = createPrinter(object);
        printer.print(object);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit.
     * @param context the layout context
     * @return a new editor
     */
    protected IMObjectEditor createEditor(T object,
                                          LayoutContext context) {
        return IMObjectEditorFactory.create(object, context);
    }

    /**
     * Creates a new edit dialog.
     *
     * @param editor the editor
     */
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return new EditDialog(editor);
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     * @param isNew  determines if the object is a new instance
     */
    protected void onEditCompleted(IMObjectEditor editor, boolean isNew) {
        if (editor.isDeleted()) {
            onDeleted((T) editor.getObject());
        } else if (editor.isSaved()) {
            onSaved((T) editor.getObject(), isNew);
        } else {
            GlobalContext.getInstance().setCurrent(null);
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    protected void onSaved(T object, boolean isNew) {
        setObject(object);
        if (_listener != null) {
            _listener.saved(object, isNew);
        }
    }

    /**
     * Deletes an object. Invokes {@link #onDeleted} if successful.
     *
     * @param object the object to delete
     */
    protected void delete(T object) {
        try {
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            // make sure deleting the latest version, to avoid hibernate errors
            object = IMObjectHelper.reload(object);
            if (object != null) {
                service.remove(object);
                onDeleted(object);
            }
        } catch (OpenVPMSException exception) {
            String title = Messages.get("imobject.delete.failed.title");
            ErrorHelper.show(title, exception);
        }
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(T object) {
        setObject(null);
        if (_listener != null) {
            _listener.deleted(object);
        }
    }

    /**
     * Creates a new printer.
     *
     * @param object the object to print
     * @return a new printer
     */
    protected IMObjectPrinter<T> createPrinter(T object) {
        return IMObjectPrinterFactory.create(
                object.getArchetypeId().getShortName());
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(T object) {
        setObject(null);
        if (_listener != null) {
            _listener.refresh(object);
        }
    }

    /**
     * Edit an object.
     *
     * @param object the object to edit
     */
    private void edit(T object) {
        try {
            final boolean isNew = object.isNew();

            LayoutContext context = new DefaultLayoutContext(true);
            final IMObjectEditor editor = createEditor(object, context);
            EditDialog dialog = createEditDialog(editor);
            dialog.addWindowPaneListener(new WindowPaneListener() {
                public void windowPaneClosing(WindowPaneEvent event) {
                    onEditCompleted(editor, isNew);
                }
            });

            GlobalContext.getInstance().setCurrent(object);
            dialog.show();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Pops up a dialog prompting if deactivation of an object should proceed,
     * deleting it if OK is selected.
     *
     * @param object the object to delete
     */
    private void confirmDeactivate(final T object) {
        String title = Messages.get("imobject.deactivate.title", _type);
        String message = Messages.get("imobject.deactivate.message",
                                      object.getName());
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    try {
                        IArchetypeService service
                                = ArchetypeServiceHelper.getArchetypeService();
                        object.setActive(false);
                        service.save(object);
                        onSaved(object, false);
                    } catch (OpenVPMSException exception) {
                        ErrorHelper.show(exception);
                    }
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
    private void confirmDelete(final T object) {
        String title = Messages.get("imobject.delete.title", _type);
        String message = Messages.get("imobject.delete.title",
                                      object.getName());
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    delete(object);
                }
            }
        });
        dialog.show();
    }
}
