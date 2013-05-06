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

package org.openvpms.web.app.subsystem;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.IMPrinterFactory;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.report.ContextDocumentTemplateLocator;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.print.PrinterListener;
import org.openvpms.web.component.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.component.subsystem.CRUDWindowListener;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.servlet.DownloadServlet;

import static org.openvpms.archetype.rules.act.ActStatus.POSTED;


/**
 * CRUD Window for acts.
 *
 * @author Tim Anderson
 */
public abstract class ActCRUDWindow<T extends Act> extends AbstractViewCRUDWindow<T> {

    /**
     * Post button identifier.
     */
    protected static final String POST_ID = "post";

    /**
     * Preview button identifier.
     */
    protected static final String PREVIEW_ID = "preview";

    /**
     * Determines if the current act is posted or not.
     */
    private boolean posted;


    /**
     * Constructs an {@code ActCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param actions    determines the operations that may be performed on the selected object
     * @param context    the context
     * @param help       the help context
     */
    public ActCRUDWindow(Archetypes<T> archetypes, ActActions<T> actions, Context context, HelpContext help) {
        super(archetypes, actions, context, help);
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(T object) {
        posted = (object != null) && POSTED.equals(object.getStatus());
        super.setObject(object);
    }

    /**
     * Invoked when the edit button is pressed. This popups up an {@link EditDialog}.
     */
    @Override
    public void edit() {
        T act = getObject();
        if (act != null) {
            if (getActions().canEdit(act)) {
                super.edit();
            } else {
                showStatusError(act, "act.noedit.title", "act.noedit.message");
            }
        }
    }

    /**
     * Deletes the current object.
     */
    @Override
    public void delete() {
        T act = getObject();
        if (act != null) {
            if (getActions().canDelete(act)) {
                super.delete();
            } else {
                showStatusError(act, "act.nodelete.title", "act.nodelete.message");
            }
        }
    }

    /**
     * Returns the actions that may be performed on the selected object.
     *
     * @return the actions
     */
    @Override
    protected ActActions<T> getActions() {
        return (ActActions<T>) super.getActions();
    }

    /**
     * Invoked when the 'post' button is pressed.
     */
    protected void onPost() {
        final T act = getObject();
        if (act != null && getActions().canPost(act)) {
            try {
                HelpContext help = getHelpContext().subtopic("post");
                String displayName = getArchetypes().getDisplayName();
                String title = Messages.get("act.post.title", displayName);
                String message = Messages.get("act.post.message", displayName);
                final ConfirmationDialog dialog = new ConfirmationDialog(title, message, help);
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        try {
                            boolean saved = post(act);
                            if (saved) {
                                // act was saved. Need to refresh
                                saved(act);
                                onPosted(act);
                            } else {
                                onRefresh(act);
                            }
                        } catch (OpenVPMSException exception) {
                            ErrorHelper.show(exception);
                        }
                    }
                });
                dialog.show();
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(T object, boolean isNew) {
        boolean prevPosted = posted && !isNew;
        super.onSaved(object, isNew);
        String status = object.getStatus();
        if (!prevPosted && POSTED.equals(status)) {
            onPosted(object);
        }
    }

    /**
     * Invoked when posting of an act is complete, either by saving the act
     * with <em>POSTED</em> status, or invoking {@link #onPost()}.
     * <p/>
     * This implementation does nothing.
     *
     * @param act the act
     */
    protected void onPosted(T act) {

    }

    /**
     * Invoked when the 'preview' button is pressed.
     */
    protected void onPreview() {
        try {
            T object = getObject();
            ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object, getContext());
            IMPrinter<T> printer = IMPrinterFactory.create(object, locator, getContext());
            Document document = printer.getDocument();
            DownloadServlet.startDownload(document);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Creates a new printer.
     *
     * @param object the object to print
     * @return an instance of {@link InteractiveIMPrinter}.
     * @throws OpenVPMSException for any error
     */
    @Override
    protected IMPrinter<T> createPrinter(final T object) {
        InteractiveIMPrinter<T> printer = (InteractiveIMPrinter<T>) super.createPrinter(object);
        printer.setListener(new PrinterListener() {
            public void printed(String printer) {
                if (getActions().setPrinted(object)) {
                    saved(object);
                }
            }

            public void cancelled() {
            }

            public void skipped() {
            }

            public void failed(Throwable cause) {
                ErrorHelper.show(cause);
            }
        });
        return printer;
    }

    /**
     * Helper to show a status error.
     *
     * @param act        the act
     * @param titleKey   the error dialog title key
     * @param messageKey the error messsage key
     */
    protected void showStatusError(Act act, String titleKey,
                                   String messageKey) {
        String name = getArchetypeDescriptor().getDisplayName();
        String status = act.getStatus();
        String title = Messages.get(titleKey, name);
        String message = Messages.get(messageKey, name, status);
        ErrorDialog.show(title, message);
    }

    /**
     * Helper to create a new button with id {@link #POST_ID} linked to {@link #onPost()}.
     *
     * @return a new button
     */
    protected Button createPostButton() {
        return ButtonFactory.create(POST_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onPost();
            }
        });
    }

    /**
     * Helper to create a new button with id {@link #PREVIEW_ID} linked to {@link #onPreview()}.
     *
     * @return a new button
     */
    protected Button createPreviewButton() {
        return ButtonFactory.create(PREVIEW_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                onPreview();
            }
        });
    }

    /**
     * Posts the act. This changes the act's status to POSTED, and saves it.
     *
     * @param act the act to post
     * @return {@code true} if the act was saved
     */
    protected boolean post(T act) {
        ActActions<T> operations = getActions();
        return operations.canPost(act) && operations.post(act);
    }

    /**
     * Invoked when an act is saved. Refreshes the window and notifies any
     * registered listener.
     *
     * @param act the act
     */
    private void saved(T act) {
        setObject(act);
        CRUDWindowListener<T> listener = getListener();
        if (listener != null) {
            listener.saved(act, false);
        }
    }

}
