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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workspace;

import echopointng.KeyStrokes;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DelegatingContext;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.delete.AbstractIMObjectDeletionListener;
import org.openvpms.web.component.im.delete.ConfirmingDeleter;
import org.openvpms.web.component.im.delete.IMObjectDeleter;
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
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.util.IMObjectCreatorListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.Selection;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.button.ButtonRow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.event.WindowPaneListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.servlet.DownloadServlet;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;
import java.util.function.Consumer;


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
     * The archetypes that this may create.
     */
    private final Archetypes<T> archetypes;

    /**
     * The context.
     */
    private final Context context;

    /**
     * Help context.
     */
    private final HelpContext help;

    /**
     * The object.
     */
    private T object;

    /**
     * The selection path. May be {@code null}
     */
    private List<Selection> selectionPath;

    /**
     * Determines the operations that may be performed on the selected object.
     */
    private IMObjectActions<T> actions;

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
     * Email context.
     */
    private MailContext mailContext;

    /**
     * Constructs an {@code AbstractCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param actions    determines the operations that may be performed on the selected object. If {@code null},
     *                   actions should be registered via {@link #setActions(IMObjectActions)}
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
        this.selectionPath = null;
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
     * Sets the selection path.
     *
     * @param path the path. May be {@code null}
     */
    @Override
    public void setSelectionPath(List<Selection> path) {
        selectionPath = path;
    }

    /**
     * Returns the object's archetype descriptor.
     *
     * @return the object's archetype descriptor or {@code null} if there
     * is no object set
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
     * @return {@code true} if an object exists and there is no edit button or it is enabled
     */
    public boolean canEdit() {
        return canEdit(object);
    }

    /**
     * Edits the current object.
     */
    public void edit() {
        edit(selectionPath);
    }

    /**
     * Edits the current object.
     *
     * @param path the path to view. May be {@code null}
     */
    @Override
    public void edit(List<Selection> path) {
        T object = getObject();
        if (object != null) {
            if (canEdit(object)) {
                if (object.isNew()) {
                    edit(object, path);
                } else {
                    // make sure the latest instance is being used.
                    IMObject previous = object;
                    object = IMObjectHelper.reload(object);
                    if (object == null) {
                        ErrorDialog.show(Messages.format("imobject.noexist",
                                                         DescriptorHelper.getDisplayName(previous)));
                    } else {
                        edit(object, path);
                    }
                }
            } else {
                ErrorDialog.show(Messages.format("imobject.noedit", DescriptorHelper.getDisplayName(object)));
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
            ErrorDialog.show(Messages.format("imobject.noexist", archetypes.getDisplayName()));
            onRefresh(getObject());
        } else if (getActions().canDelete(object)) {
            delete(object);
        } else {
            deleteDisallowed(object);
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
     * <p>
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
     * @param actions the actions
     */
    protected void setActions(IMObjectActions<T> actions) {
        this.actions = actions;
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
     * Returns an {@link IMObjectDeleter} to delete an object.
     * <p>
     * This implementation returns an instance that prompts for confirmation.
     *
     * @return a new deleter
     */
    @SuppressWarnings("unchecked")
    protected IMObjectDeleter<T> getDeleter() {
        return (IMObjectDeleter<T>) ServiceHelper.getBean(ConfirmingDeleter.class);
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
        buttons.setEnabled(NEW_ID, actions.canCreate());
        buttons.setEnabled(EDIT_ID, enable && actions.canEdit(object));
        buttons.setEnabled(DELETE_ID, enable && actions.canDelete(object));
    }

    /**
     * Enables/disables print and print-preview.
     *
     * @param buttons the buttons
     * @param enable  if {@code true}, enable print/print preview, else disable it
     */
    protected void enablePrintPreview(ButtonSet buttons, boolean enable) {
        buttons.setEnabled(PRINT_ID, enable);
        String tooltip = null;
        if (enable) {
            buttons.addKeyListener(KeyStrokes.ALT_MASK | KeyStrokes.VK_V, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onPreview();
                }
            });
            tooltip = Messages.get("print.preview.tooltip");
        } else {
            buttons.removeKeyListener(KeyStrokes.ALT_MASK | KeyStrokes.VK_V);
        }
        Button button = getButtons().getButton(PRINT_ID);
        if (button != null) {
            button.setToolTipText(tooltip);
        }
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
        edit(object, null);
    }

    /**
     * Determines if an object can be edited.
     *
     * @param object the object
     * @return {@code true} if an object exists and there is no edit button or it is enabled
     */
    protected boolean canEdit(T object) {
        boolean edit = false;
        if (actions.canEdit(object)) {
            ButtonSet buttons = getButtons();
            Button button = (buttons != null) ? buttons.getButton(EDIT_ID) : null;
            edit = button != null && button.isEnabled();
        }
        return edit;
    }

    /**
     * Edits an object.
     *
     * @param object the object to edit
     * @param path   the selection path. May be {@code null}
     */
    protected void edit(T object, List<Selection> path) {
        try {
            HelpContext edit = createEditTopic(object);
            LayoutContext context = createLayoutContext(edit);
            IMObjectEditor editor = createEditor(object, context);
            editor.getComponent();
            edit(editor, path);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Deletes an object.
     *
     * @param object the object to delete
     */
    protected void delete(T object) {
        IMObjectDeleter<T> deleter = getDeleter();
        HelpContext delete = getHelpContext().subtopic("delete");
        Context local = new LocalContext(context);
        // nest the context so the global context isn't updated. See OVPMS-2046
        deleter.delete(object, local, delete, new AbstractIMObjectDeletionListener<T>() {
            public void deleted(T object) {
                onDeleted(object);
            }

            public void deactivated(T object) {
                onSaved(object, false);
            }
        });
    }

    /**
     * Invoked if an object may not be deleted.
     * <p>
     * This implementation is a no-op
     *
     * @param object the object
     */
    protected void deleteDisallowed(T object) {
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
    protected EditDialog edit(IMObjectEditor editor) {
        return edit(editor, null);
    }

    /**
     * Edits an object.
     *
     * @param editor the object editor
     * @param path   the selection path. May be {@code null}
     * @return the edit dialog
     */
    @SuppressWarnings("unchecked")
    protected EditDialog edit(final IMObjectEditor editor, List<Selection> path) {
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
        if (path != null) {
            // set the selection path after showing the dialog so the focus moves to the leaf of the selection path
            editor.setSelectionPath(path);
        }
        return dialog;
    }

    /**
     * Invoked when the 'print' button is pressed.
     */
    protected void onPrint() {
        T object = IMObjectHelper.reload(getObject());
        if (object == null) {
            ErrorDialog.show(Messages.format("imobject.noexist", getArchetypes().getDisplayName()));
        } else {
            print(object);
        }
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit.
     * @param context the layout context
     * @return a new editor
     */
    protected IMObjectEditor createEditor(T object, LayoutContext context) {
        return ServiceHelper.getBean(IMObjectEditorFactory.class).create(object, context);
    }

    /**
     * Creates a new edit dialog.
     * <p>
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
            onSaved(editor, isNew);
        } else {
            // cancelled/no changes to save
            onRefresh(editor);
        }
    }

    /**
     * Invoked when the editor is saved and closed.
     *
     * @param editor the editor
     * @param isNew  determines if the object is a new instance
     */
    @SuppressWarnings("unchecked")
    protected void onSaved(IMObjectEditor editor, boolean isNew) {
        onSaved((T) editor.getObject(), isNew);
        setSelectionPath(editor.getSelectionPath());
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
        IMPrinterFactory factory = ServiceHelper.getBean(IMPrinterFactory.class);
        IMPrinter<T> printer = factory.create(object, locator, context);
        HelpContext help = createPrintTopic(object);
        InteractiveIMPrinter<T> interactive = new InteractiveIMPrinter<>(printer, context, help);
        interactive.setMailContext(getMailContext());
        return interactive;
    }

    /**
     * Invoked to preview the current object.
     */
    protected void onPreview() {
        T previous = getObject();
        final T object = IMObjectHelper.reload(previous);
        if (object == null && previous != null) {
            ErrorDialog.show(Messages.format("imobject.noexist", DescriptorHelper.getDisplayName(previous)));
        } else {
            try {
                ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object, context);
                IMPrinterFactory factory = ServiceHelper.getBean(IMPrinterFactory.class);
                IMPrinter<T> printer = factory.create(object, locator, context);
                Document document = printer.getDocument(DocFormats.PDF_TYPE, false);
                DownloadServlet.startDownload(document);
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
    }

    /**
     * Invoked on editor completion, when the object needs to be refreshed.
     *
     * @param editor the editor
     */
    @SuppressWarnings("unchecked")
    protected void onRefresh(IMObjectEditor editor) {
        onRefresh((T) editor.getObject());
        setSelectionPath(editor.getSelectionPath());
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
        return new DefaultLayoutContext(true, new LocationContext(getContext()), help);
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

    /**
     * Creates an action listener that runs an action against the selected object when clicked.
     * <p>
     * The action is run with the latest instance of the selected object, but only if it matches the supplied archetype.
     *
     * @param archetype the archetype. May contain wildcards
     * @param action    the action to execute, when the selected object is an instance of {@code archetype}
     * @param title     the title resource bundle key, used when displaying an error dialog if the action fails
     * @return a new listener
     */
    protected ActionListener action(final String archetype, final Consumer<T> action, final String title) {
        return new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                T object = getObject();
                if (TypeHelper.isA(object, archetype)) {
                    try {
                        T latest = IMObjectHelper.reload(object);
                        if (latest != null) {
                            action.accept(latest);
                        } else {
                            String displayName = DescriptorHelper.getDisplayName(object);
                            ErrorDialog.show(Messages.get(title), Messages.format("imobject.noexist", displayName));
                            onRefresh(object);
                        }
                    } catch (Throwable exception) {
                        String displayName = DescriptorHelper.getDisplayName(object);
                        ErrorHelper.show(Messages.get(title), displayName, object, exception);
                    }
                }
            }
        };
    }

    /**
     * A delegating context where location and stock location changes are not propagated.
     * <p>
     * This is to prevent changes that an editor makes to the location/stock location from propagating to the global
     * context.
     */
    protected static class LocationContext extends DelegatingContext {

        private Party location;

        private Party stockLocation;

        /**
         * Constructs a {@link LocationContext}.
         *
         * @param context the context to delegate to
         */
        public LocationContext(Context context) {
            super(context);
            this.location = context.getLocation();
            this.stockLocation = context.getStockLocation();
        }

        /**
         * Sets the current location.
         *
         * @param location the current location
         */
        @Override
        public void setLocation(Party location) {
            this.location = location;
        }

        /**
         * Returns the current location.
         *
         * @return the current location
         */
        @Override
        public Party getLocation() {
            return location;
        }

        /**
         * Sets the current stock location.
         *
         * @param location the current location
         */
        @Override
        public void setStockLocation(Party location) {
            this.stockLocation = location;
        }

        /**
         * Returns the current stock location.
         *
         * @return the current stock location, or {@code null} if there is no current location
         */
        @Override
        public Party getStockLocation() {
            return stockLocation;
        }
    }

}
