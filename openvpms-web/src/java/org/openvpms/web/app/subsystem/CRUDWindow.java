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

import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.SelectionDialog;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.create.IMObjectCreatorListener;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.view.IMObjectViewer;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

/**
 * Generic CRUD window.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CRUDWindow {

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
     * The create button.
     */
    private Button _create;

    /**
     * The delete button.
     */
    private Button _delete;

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
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create.
     *                   If <code>null</code> subclass must override
     *                   {@link #getShortNames}
     */
    public CRUDWindow(String type, ShortNames shortNames) {
        _type = type;
        _shortNames = shortNames;
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
     * Returns the event listener.
     *
     * @return the event listener
     */
    public CRUDWindowListener getListener() {
        return _listener;
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
        Context.getInstance().setCurrent(object);
        getComponent();
        _objectContainer.removeAll();
        if (object != null) {
            _viewer = new IMObjectViewer(object);
            _objectContainer.add(_viewer.getComponent());
            enableButtons(true);
        } else {
            _viewer = null;
            enableButtons(false);
        }
    }

    /**
     * Returns the object.
     *
     * @return the object, or <code>null</code> if there is none set
     */
    public IMObject getObject() {
        return (_viewer != null) ? _viewer.getObject() : null;
    }

    /**
     * Returns the object's archetype descriptor.
     *
     * @return the object's archetype descriptor or <code>null</code> if there
     *         is no object set
     */
    public ArchetypeDescriptor getArchetypeDescriptor() {
        IMObject object = getObject();
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
    protected void doLayout() {
        _buttons = RowFactory.create(ROW_STYLE);
        layoutButtons(_buttons);
        enableButtons(false);
        _objectContainer = ColumnFactory.create();
        _component = SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                STYLE, _buttons, _objectContainer);
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
                onCreated(object);
            }
        };

        IMObjectCreator.create(type, shortNames.getShortNames(), listener);
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param object the new object
     */
    protected void onCreated(IMObject object) {
        edit(object);
    }

    /**
     * Invoked when the edit button is pressed. This popups up an {@link
     * EditDialog}.
     */
    protected void onEdit() {
        if (_viewer != null) {
            IMObject object = _viewer.getObject();
            if (object.isNew()) {
                edit(object);
            } else {
                // make sure the latest instance is being used.
                IArchetypeService service
                        = ServiceHelper.getArchetypeService();
                object = ArchetypeQueryHelper.getByObjectReference(
                        service, object.getObjectReference());
                if (object == null) {
                    ErrorDialog.show("imobject.noexist", _type);
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
        IMObject object = getObject();
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
     * Creates a new editor.
     *
     * @param object the object to edit.
     * @param context
     * @return a new editor
     */
    protected IMObjectEditor createEditor(IMObject object,
                                          LayoutContext context) {
        return IMObjectEditorFactory.create(object, context);
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     * @param isNew  determines if the object is a new instance
     */
    protected void onEditCompleted(IMObjectEditor editor, boolean isNew) {
        if (editor.isDeleted()) {
            onDeleted(editor.getObject());
        } else if (editor.isSaved()) {
            onSaved(editor.getObject(), isNew);
        } else {
            Context.getInstance().setCurrent(null);
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
     * Deletes an object. Invokes {@link #onDeleted} if successful.
     *
     * @param object the object to delete
     */
    protected void delete(IMObject object) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        try {
            service.remove(object);
            onDeleted(object);
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
    protected void onDeleted(IMObject object) {
        setObject(null);
        if (_listener != null) {
            _listener.deleted(object);
        }
    }

    /**
     * Edit an IMObject.
     *
     * @param object the object to edit
     */
    private void edit(IMObject object) {
        final boolean isNew = object.isNew();

        LayoutContext context = new DefaultLayoutContext(true);
        final IMObjectEditor editor = createEditor(object, context);
        EditDialog dialog = new EditDialog(editor, context);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                onEditCompleted(editor, isNew);
            }
        });

        Context.getInstance().setCurrent(object);
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
        String message = Messages.get("imobject.deactivate.message",
                                      object.getName());
        ConfirmationDialog dialog = new ConfirmationDialog(title, message);
        dialog.addActionListener(SelectionDialog.OK_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                try {
                    object.setActive(false);
                    service.save(object);
                    onSaved(object, false);
                } catch (OpenVPMSException exception) {
                    ErrorHelper.show(exception);
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
        String message = Messages.get("imobject.delete.title",
                                      object.getName());
        ConfirmationDialog dialog = new ConfirmationDialog(title, message);
        dialog.addActionListener(SelectionDialog.OK_ID, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                delete(object);
            }
        });
        dialog.show();
    }

}
