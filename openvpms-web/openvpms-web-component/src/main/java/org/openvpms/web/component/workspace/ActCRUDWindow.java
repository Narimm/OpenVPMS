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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectActions;
import org.openvpms.web.component.im.print.IMPrinter;
import org.openvpms.web.component.im.print.InteractiveIMPrinter;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.print.BasicPrinterListener;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;

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
     * Determines if the current act is posted or not.
     */
    private boolean posted;


    /**
     * Constructs an {@link ActCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param actions    determines the operations that may be performed on the selected object. If {@code null},
     *                   actions should be registered via {@link #setActions(IMObjectActions)}
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
     * Invoked if an object may not be deleted.
     *
     * @param object the object
     */
    @Override
    protected void deleteDisallowed(T object) {
        showStatusError(object, "act.nodelete.title", "act.nodelete.message");
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
        T previous = getObject();
        final T act = IMObjectHelper.reload(previous);
        if (act == null && previous != null) {
            ErrorDialog.show(Messages.format("imobject.noexist", DescriptorHelper.getDisplayName(previous)));
        } else if (act != null && getActions().canPost(act)) {
            confirmPost(act, new Runnable() {
                @Override
                public void run() {
                    boolean saved = post(act);
                    if (saved) {
                        // act was saved. Need to refresh
                        saved(act);
                        onPosted(act);
                    } else {
                        onRefresh(act);
                    }
                }
            });
        }
    }

    /**
     * Confirms that the user wants to post the act.
     *
     * @param act      the act to post
     * @param callback the callback to handle the posting, if the user confirms it
     */
    protected void confirmPost(final Act act, final Runnable callback) {
        final ConfirmationDialog dialog = createPostConfirmationDialog(act);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                try {
                    callback.run();
                } catch (OpenVPMSException exception) {
                    ErrorHelper.show(exception);
                }
            }
        });
        dialog.show();
    }

    /**
     * Creates a dialog to confirm posting an act.
     *
     * @param act the act to be posted
     * @return a new dialog
     */
    protected ConfirmationDialog createPostConfirmationDialog(Act act) {
        HelpContext help = getHelpContext().subtopic("post");
        String displayName = DescriptorHelper.getDisplayName(act);
        String title = Messages.format("act.post.title", displayName);
        String message = Messages.format("act.post.message", displayName);
        return new ConfirmationDialog(title, message, help);
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
     * <p>
     * This implementation does nothing.
     *
     * @param act the act
     */
    protected void onPosted(T act) {

    }

    /**
     * Print an object.
     *
     * @param object the object to print
     */
    @Override
    protected void print(T object) {
        ActActions<T> actions = getActions();
        if (actions.isUnfinalised(object) && actions.warnWhenPrintingUnfinalisedAct()) {
            String displayName = DescriptorHelper.getDisplayName(object);
            String title = Messages.format("print.unfinalised.title", displayName);
            String message = Messages.format("print.unfinalised.message", displayName);
            ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                @Override
                public void onYes() {
                    ActCRUDWindow.super.print(object);
                }
            });
        } else {
            super.print(object);
        }
    }

    /**
     * Mail an object.
     *
     * @param object the object to mail
     */
    @Override
    protected void mail(T object) {
        ActActions<T> actions = getActions();
        if (actions.isUnfinalised(object) && actions.warnWhenPrintingUnfinalisedAct()) {
            String displayName = DescriptorHelper.getDisplayName(object);
            String title = Messages.format("mail.unfinalised.title", displayName);
            String message = Messages.format("mail.unfinalised.message", displayName);
            ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                @Override
                public void onYes() {
                    ActCRUDWindow.super.mail(object);
                }
            });
        } else {
            super.mail(object);
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
        printer.setListener(new BasicPrinterListener() {
            public void printed(String printer) {
                if (getActions().setPrinted(object)) {
                    saved(object);
                }
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
        ArchetypeDescriptor descriptor = getArchetypeDescriptor();
        String name = (descriptor != null) ? descriptor.getDisplayName() : act.getArchetypeId().getShortName();
        String status = act.getStatus();
        String title = Messages.format(titleKey, name);
        String message = Messages.format(messageKey, name, status);
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
