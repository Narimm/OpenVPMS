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
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.im.edit.ActOperations;
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
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
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
     * Constructs an <tt>ActCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     * @param operations determines the operations that may be performed on the selected object
     */
    public ActCRUDWindow(Archetypes<T> archetypes, ActOperations<T> operations) {
        super(archetypes, operations);
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be <tt>null</tt>
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
            if (getOperations().canEdit(act)) {
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
            if (getOperations().canDelete(act)) {
                super.delete();
            } else {
                showStatusError(act, "act.nodelete.title", "act.nodelete.message");
            }
        }
    }

    /**
     * Returns the operations that may be performed on the selected object.
     *
     * @return the operations
     */
    @Override
    protected ActOperations<T> getOperations() {
        return (ActOperations<T>) super.getOperations();
    }

    /**
     * Invoked when the 'post' button is pressed.
     */
    protected void onPost() {
        final T act = getObject();
        if (act != null && getOperations().canPost(act)) {
            try {
                String displayName = getArchetypes().getDisplayName();
                String title = Messages.get("act.post.title", displayName);
                String message = Messages.get("act.post.message", displayName);
                final ConfirmationDialog dialog = new ConfirmationDialog(title, message);
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
            ContextDocumentTemplateLocator locator = new ContextDocumentTemplateLocator(object,
                                                                                        GlobalContext.getInstance());
            IMPrinter<T> printer = IMPrinterFactory.create(object, locator);
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
                if (getOperations().printed(object)) {
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
     * @return <tt>true</tt> if the act was saved
     */
    protected boolean post(T act) {
        ActOperations<T> operations = getOperations();
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
