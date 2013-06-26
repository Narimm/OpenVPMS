/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workspace;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditDialogFactory;
import org.openvpms.web.component.im.edit.IMObjectActions;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.util.AbstractIMObjectDeletionListener;
import org.openvpms.web.component.im.util.DefaultIMObjectDeletor;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectCreatorListener;
import org.openvpms.web.component.im.util.IMObjectDeletor;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Abstract implementation of the {@link CRUDWindow} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractCRUDWindow<T extends IMObject> implements CRUDWindow<T> {

    /**
     * Edit button identifier.
     */
    public static final String EDIT_ID = "edit";

    /**
     * New button identifier.
     */
    public static final String NEW_ID = "new";

    /**
     * Delete button identifier.
     */
    public static final String DELETE_ID = "delete";

    /**
     * Print button identifier.
     */
    public static final String PRINT_ID = "print";

    /**
     * The object.
     */
    private T object;

    /**
     * Determines the operations that may be performed on the selected object.
     */
    private final IMObjectActions<T> actions;

    /**
     * The archetypes that this may create.
     */
    private final Archetypes<T> archetypes;

    /**
     * The listener.
     */
    private CRUDWindowListener<T> listener;

    /**
     * The component representing this.
     */
    private Component component;

    /**
     * The buttons.
     */
    private ButtonSet buttons;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Email context.
     */
    private MailContext mailContext;

    /**
     * Help context.
     */
    private final HelpContext help;

    /**
     * Constructs an {@code AbstractCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param actions    determines the operations that may be performed on the selected object
     * @param context    the context
     * @param help       the help context
     */
    public AbstractCRUDWindow(Archetypes<T> archetypes, IMObjectActions<T> actions, Context context, HelpContext help) {
        this.archetypes = archetypes;
        this.actions = actions;
        this.context = context;
        this.help = help;
    }

    /**
     * Sets the event listener.
     *
     * @param listener the event listener.
     */
    public void setListener(CRUDWindowListener<T> listener) {
        this.listener = listener;
    }

    /**
     * Returns the event listener.
     *
     * @return the event listener
     */
    public CRUDWindowListener<T> getListener() {
        return listener;
    }

    /**
     * Returns the component representing this.
     *
     * @return the component
     */
    public Component getComponent() {
        if (component == null) {
            component = doLayout();
        }
        return component;
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be {@code null}
     */
    public void setObject(T object) {
        this.object = object;
        context.setCurrent(object);
        getComponent();
        ButtonSet buttons = getButtons();
        if (buttons != null) {
            if (object != null) {
                enableButtons(buttons, true);
            } else {
                enableButtons(buttons, false);
            }
        }
    }

    /**
     * Returns the object.
     *
     * @return the object, or {@code null} if there is none set
     */
    public T getObject() {
        return object;
    }

    /**
     * Returns the object's archetype descriptor.
     *
     * @return the object's archetype descriptor or {@code null} if there
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
     * Creates and edits a new object.
     */
    public void create() {
        if (actions.canCreate()) {
            onCreate(getArchetypes());
        }
    }

    /**
     * Determines if the current object can be edited.
     *
     * @return {@code true} if an object exists and the edit button is enabled
     */
    public boolean canEdit() {
        boolean edit = false;
        if (actions.canEdit(object)) {
            Button button = getButtons().getButton(EDIT_ID);
            if (button != null && button.isEnabled()) {
                edit = true;
            }
        }
        return edit;
    }

    /**
     * Edits the current object.
     */
    public void edit() {
        T object = getObject();
        if (object != null) {
            if (canEdit()) {
                if (object.isNew()) {
                    edit(object);
                } else {
                    // make sure the latest instance is being used.
                    IMObject previous = object;
                    object = IMObjectHelper.reload(object);
                    if (object == null) {
                        ErrorDialog.show(Messages.get("imobject.noexist", DescriptorHelper.getDisplayName(previous)));
                    } else {
                        edit(object);
                    }
                }
            } else {
                ErrorDialog.show(Messages.get("imobject.noedit", DescriptorHelper.getDisplayName(object)));
            }
        }
    }

    /**
     * Deletes the current object.
     */
    @SuppressWarnings("unchecked")
    public void delete() {
        T object = IMObjectHelper.reload(getObject());
        if (object == null) {
            ErrorDialog.show(Messages.get("imobject.noexist", archetypes.getDisplayName()));
        } else {
            IMObjectDeletor deletor = new DefaultIMObjectDeletor(getContext());
            HelpContext delete = getHelpContext().subtopic("delete");
            deletor.delete(object, delete, new AbstractIMObjectDeletionListener<T>() {
                public void deleted(T object) {
                    onDeleted(object);
                }

                public void deactivated(T object) {
                    onSaved(object, false);
                }
            });
        }
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Sets the mail context.
     * <p/>
     * This is used to determine email addresses when mailing.
     *
     * @param context the mail context. May be {@code null}
     */
    public void setMailContext(MailContext context) {
        this.mailContext = context;
    }

    /**
     * Returns the mail context.
     *
     * @return the mail context. May be {@code null}
     */
    public MailContext getMailContext() {
        return mailContext;
    }

    /**
     * Returns the help context.
     *
     * @return the help context
     */
    public HelpContext getHelpContext() {
        return help;
    }

    /**
     * Sets the buttons.
     *
     * @param buttons the buttons
     */
    public void setButtons(ButtonSet buttons) {
        this.buttons = buttons;
        layoutButtons(buttons);
        enableButtons(buttons, getObject() != null);
    }

    /**
     * Returns the archetypes that this may create.
     *
     * @return the archetypes
     */
    protected Archetypes<T> getArchetypes() {
        return archetypes;
    }

    /**
     * Determines the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    protected IMObjectActions<T> getActions() {
        return actions;
    }

    /**
     * Lays out the component.
     *
     * @return the component
     */
    protected Component doLayout() {
        return layoutButtons();
    }

    /**
     * Lays out the buttons.
     *
     * @return the button container
     */
    protected Component layoutButtons() {
        if (buttons == null) {
            ButtonRow row = new ButtonRow("ControlRow");
            buttons = row.getButtons();
        }
        buttons.setHideDisabled(true);
        layoutButtons(buttons);
        enableButtons(buttons, false);
        return buttons.getContainer();
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(createNewButton());
        buttons.add(createEditButton());
        buttons.add(createDeleteButton());
    }

    /**
     * Helper to create a new button with id {@link #EDIT_ID} linked to {@link #edit()}.
     * Editing will only be invoked if {@link #canEdit} is {@code true}
     *
     * @return a new button
     */
    protected Button createEditButton() {
        return ButtonFactory.create(EDIT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                if (canEdit()) {
                    edit();
                }
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #NEW_ID} linked to {@link #create()}.
     *
     * @return a new button
     */
    protected Button createNewButton() {
        return ButtonFactory.create(NEW_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                create();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #DELETE_ID} linked to {@link #delete()}.
     *
     * @return a new button
     */
    protected Button createDeleteButton() {
        return ButtonFactory.create(DELETE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                delete();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #PRINT_ID} linked to {@link #onPrint()}.
     *
     * @return a new button
     */
    protected Button createPrintButton() {
        return ButtonFactory.create(PRINT_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onPrint();
            }
        });
    }

    /**
     * Returns the button set.
     *
     * @return the button set
     */
    protected ButtonSet getButtons() {
        return buttons;
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        T object = getObject();
        buttons.setEnabled(NEW_ID, enable && actions.canCreate());
        buttons.setEnabled(EDIT_ID, enable && actions.canEdit(object));
        buttons.setEnabled(DELETE_ID, enable && actions.canDelete(object));
    }

    /**
     * Invoked when the 'new' button is pressed.
     *
     * @param archetypes the archetypes
     */
    @SuppressWarnings("unchecked")
    protected void onCreate(Archetypes<T> archetypes) {
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated((T) object);
            }

            public void cancelled() {
                // ignore
            }
        };

        HelpContext help = getHelpContext().subtopic("new");
        IMObjectCreator.create(archetypes, listener, help);
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
     * Edits an object.
     *
     * @param object the object to edit
     */
    protected void edit(T object) {
        try {
            HelpContext edit = createEditTopic(object);
            LayoutContext context = createLayoutContext(edit);
            IMObjectEditor editor = createEditor(object, context);
            edit(editor);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Creates a topic for editing.
     *
     * @param object the object to edit
     * @return the edit topic
     */
    protected HelpContext createEditTopic(T object) {
        return help.topic(object, "edit");
    }

    /**
     * Creates a topic for printing.
     *
     * @param object the object to print
     * @return the print topic
     */
    protected HelpContext createPrintTopic(T object) {
        return help.topic(object, "print");
    }

    /**
     * Edits an object.
     *
     * @param editor the object editor
     * @return the edit dialog
     */
    @SuppressWarnings("unchecked")
    protected EditDialog edit(final IMObjectEditor editor) {
        T object = (T) editor.getObject();
        final boolean isNew = object.isNew();
        EditDialog dialog = createEditDialog(editor);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void onClose(WindowPaneEvent event) {
                onEditCompleted(editor, isNew);
            }
        });
        context.setCurrent(object);
        dialog.show();
        return dialog;
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    protected void onPrint() {
        print(getObject());
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit.
     * @param context the layout context
     * @return a new editor
     */
    protected IMObjectEditor createEditor(T object, LayoutContext context) {
        return IMObjectEditorFactory.create(object, context);
    }

    /**
     * Creates a new edit dialog.
     * <p/>
     * This implementation uses {@link EditDialogFactory#create}.
     *
     * @param editor the editor
     * @return a new edit dialog
     */
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return EditDialogFactory.create(editor, context);
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     * @param isNew  determines if the object is a new instance
     */
    @SuppressWarnings("unchecked")
    protected void onEditCompleted(IMObjectEditor editor, boolean isNew) {
        if (editor.isDeleted()) {
            onDeleted((T) editor.getObject());
        } else if (editor.isSaved()) {
            onSaved((T) editor.getObject(), isNew);
        } else {
            // cancelled
            onRefresh((T) editor.getObject());
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
        if (listener != null) {
            listener.saved(object, isNew);
        }
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    protected void onDeleted(T object) {
        setObject(null);
        if (listener != null) {
            listener.deleted(object);
        }
    }

    /**
     * Creates a new printer.
     *
     * @param object the object to print
     * @return an instance of {@link InteractiveIMPrinter}.
     * @throws OpenVPMSException for any error
     */
    protected IMPrinter<T> createPrinter(T object) {
        ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object, context);
        IMPrinter<T> printer = IMPrinterFactory.create(object, locator, context);
        HelpContext help = createPrintTopic(object);
        InteractiveIMPrinter<T> interactive = new InteractiveIMPrinter<T>(printer, context, help);
        interactive.setMailContext(getMailContext());
        return interactive;
    }

    /**
     * Invoked when the object needs to be refreshed.
     *
     * @param object the object
     */
    protected void onRefresh(T object) {
        setObject(null);
        if (listener != null) {
            listener.refresh(object);
        }
    }

    /**
     * Creates a layout context for editing an object.
     *
     * @param help the help context
     * @return a new layout context.
     */
    protected LayoutContext createLayoutContext(HelpContext help) {
        return new DefaultLayoutContext(true, getContext(), help);
    }

    /**
     * Print an object.
     *
     * @param object the object to print
     */
    protected void print(T object) {
        try {
            IMPrinter<T> printer = createPrinter(object);
            printer.print();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Updates the context for the specified short name. If the supplied object matches the short name, it will be
     * added to the context, otherwise, the context entry will be set null.
     *
     * @param shortName the short name to match
     * @param object    the object. May be {@code null}
     */
    protected void updateContext(String shortName, T object) {
        if (TypeHelper.isA(object, shortName)) {
            context.setObject(shortName, object);
        } else {
            context.setObject(shortName, null);
        }
    }
}
