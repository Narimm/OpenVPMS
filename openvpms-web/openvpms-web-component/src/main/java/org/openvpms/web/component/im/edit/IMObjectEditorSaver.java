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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.edit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.web.component.error.ErrorFormatter;
import org.openvpms.web.component.error.ExceptionHelper;
import org.openvpms.web.component.property.DefaultValidator;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.error.ErrorHandler;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Helper to manage saving editors within transactions.
 *
 * @author Tim Anderson
 */
public class IMObjectEditorSaver {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(IMObjectEditorSaver.class);

    /**
     * Saves an editor.
     *
     * @param editor the editor to save
     * @return {@code true} if the editor was successfully saved
     */
    public boolean save(final IMObjectEditor editor) {
        boolean result;
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        try {
            // perform validation and save in the one transaction
            result = template.execute(new TransactionCallback<Boolean>() {
                @Override
                public Boolean doInTransaction(TransactionStatus transactionStatus) {
                    Validator validator = createValidator();
                    boolean result = editor.validate(validator);
                    if (result) {
                        save(editor, transactionStatus);
                    } else {
                        showError(validator);
                    }
                    return result;
                }
            });
        } catch (Throwable exception) {
            result = false;
            log(editor, exception);

            // attempt to reload the editor
            if (reload(editor)) {
                // reloaded the editor to the last saved state
                String displayName = editor.getDisplayName();
                String message = isModifiedExternally(ExceptionHelper.getRootCause(exception))
                                 ? Messages.format("imobject.save.reverted.modified", displayName)
                                 : Messages.format("imobject.save.reverted.error", displayName);
                String title = Messages.format("imobject.save.failed", displayName);
                reloaded(title, message);
            } else {
                failed(editor, exception);
            }
        }
        return result;
    }

    /**
     * Creates a validator to validate an editor.
     *
     * @return a new validator
     */
    protected Validator createValidator() {
        return new DefaultValidator();
    }

    /**
     * Displays validation errors.
     *
     * @param validator the validator
     */
    protected void showError(Validator validator) {
        ValidationHelper.showError(validator);
    }

    /**
     * Saves the editor.
     *
     * @param editor the editor to save
     * @param status the transaction status
     */
    protected void save(IMObjectEditor editor, TransactionStatus status) {
        editor.save();
    }

    /**
     * Invoked to reload the object if the save fails.
     *
     * @param editor the editor
     * @return {@code true} if the object was reloaded. This implementation always returns {@code false}
     */
    protected boolean reload(IMObjectEditor editor) {
        return false;
    }

    /**
     * Invoked to display a message that saving failed, and the editor has been reverted.
     *
     * @param title   the message title
     * @param message the message
     */
    protected void reloaded(String title, String message) {
        ErrorHandler.getInstance().error(title, message, null, null);
    }

    /**
     * Invoked when saving fails.
     * <p/>
     * This implementation displays the error.
     *
     * @param editor    the editor
     * @param exception the cause of the failure
     */
    protected void failed(IMObjectEditor editor, Throwable exception) {
        String displayName = editor.getDisplayName();
        String title = Messages.format("imobject.save.failed", displayName);
        ErrorHelper.show(title, displayName, editor.getObject(), exception);
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return a new instance, or {@code null} if new instances are not supported
     */
    protected IMObjectEditor newInstance(IMObjectEditor editor) {
        IMObjectEditor newEditor = null;
        try {
            newEditor = editor.newInstance();
        } catch (Throwable exception) {
            log.error("Failed to create a new editor instance", exception);
        }
        return newEditor;
    }

    /**
     * Determines if an exception indicates that the object being edited (or a related object) was modified externally.
     *
     * @param exception the exception
     * @return {@code true} if the object was modified externally
     */
    protected boolean isModifiedExternally(Throwable exception) {
        return ExceptionHelper.isModifiedExternally(exception);
    }

    /**
     * Logs a save error.
     *
     * @param editor    the editor
     * @param exception the exception
     */
    protected void log(IMObjectEditor editor, Throwable exception) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null) ? authentication.getName() : null;

        String context = Messages.format("logging.error.editcontext", editor.getObject().getObjectReference(),
                                         editor.getClass().getName(), user);
        String message = ErrorFormatter.format(exception, editor.getDisplayName());
        log.error(Messages.format("logging.error.messageandcontext", message, context), exception);
    }

}
