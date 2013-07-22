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

package org.openvpms.web.workspace.admin.lookup;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.AbstractIMObjectDeletionListener;
import org.openvpms.web.component.im.util.DefaultIMObjectDeletor;
import org.openvpms.web.component.im.util.IMObjectDeletor;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.ErrorDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;


/**
 * CRUD window for lookups.
 *
 * @author Tim Anderson
 */
public class LookupCRUDWindow extends ResultSetCRUDWindow<Lookup> {

    /**
     * The replace button identifier.
     */
    private static final String REPLACE_ID = "replace";


    /**
     * Constructs a {@code LookupCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param query      the query
     * @param lookups    the lookups
     * @param context    the context
     * @param help       the help context
     */
    public LookupCRUDWindow(Archetypes<Lookup> archetypes, Query<Lookup> query, ResultSet<Lookup> lookups,
                            Context context, HelpContext help) {
        super(archetypes, query, lookups, context, help);
    }

    /**
     * Deletes the current object.
     */
    @Override
    public void delete() {
        Lookup object = IMObjectHelper.reload(getObject());
        if (object == null) {
            ErrorDialog.show(Messages.format("imobject.noexist", getArchetypes().getDisplayName()));
        } else {
            IMObjectDeletor deletor = new DefaultIMObjectDeletor(getContext());
            deletor.delete(object, getHelpContext().subtopic("delete"), new LookupDeletorListener());
        }
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);

        Button replace = ButtonFactory.create(REPLACE_ID, new ActionListener() {
            public void onAction(ActionEvent event) {
                replace();
            }
        });

        buttons.add(replace);
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        super.enableButtons(buttons, enable);
        buttons.setEnabled(REPLACE_ID, enable);
    }

    /**
     * Displays a dialog to replace the current lookup with another.
     */
    private void replace() {
        final Lookup lookup = getObject();
        if (lookup != null) {
            String shortName = lookup.getArchetypeId().getShortName();
            Query<Lookup> query = QueryFactory.create(shortName, getContext(), Lookup.class);
            query.setAuto(true);
            HelpContext help = getHelpContext().subtopic("replace");
            DefaultLayoutContext context = new DefaultLayoutContext(getContext(), help);
            final ReplaceLookupBrowser browser = new ReplaceLookupBrowser(query, lookup, context);
            String title = Messages.get("lookup.replace.title");
            BrowserDialog<Lookup> dialog = new BrowserDialog<Lookup>(title, BrowserDialog.OK_CANCEL, browser, help);
            dialog.setCloseOnSelection(false);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    confirmReplace(lookup, browser.getSelected(), browser.deleteLookup());
                }
            });
            dialog.show();
        }
    }

    /**
     * Prompts to replace the source lookup with the target lookup.
     *
     * @param source the source lookup
     * @param target the target lookup
     * @param delete if {@code true} delete the source lookup
     */
    private void confirmReplace(final Lookup source, final Lookup target, final boolean delete) {
        String title = Messages.get("lookup.replace.title");
        String message = Messages.format(delete ? "lookup.replace.confirmDelete" : "lookup.replace.confirm",
                                         source.getName(), target.getName());
        ConfirmationDialog dialog = new ConfirmationDialog(title, message, getHelpContext().subtopic("confirmreplace"));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                LookupReplaceHelper admin = new LookupReplaceHelper();
                admin.replace(source, target, delete);
                onRefresh(source);
            }
        });
        dialog.show();
    }

    private class LookupDeletorListener extends AbstractIMObjectDeletionListener<Lookup> {

        public void deleted(Lookup object) {
            onDeleted(object);
        }

        public void deactivated(Lookup object) {
            onSaved(object, false);
        }

        @Override
        public void failed(Lookup object, Throwable cause) {
            if (lookupInUse(cause)) {
                reportLookupInUse();
            } else {
                super.failed(object, cause);
            }
        }

        @Override
        public void failed(Lookup object, Throwable cause, IMObjectEditor editor) {
            if (lookupInUse(cause)) {
                reportLookupInUse();
            } else {
                super.failed(object, cause, editor);
            }
        }

        /**
         * Determines if the lookup is in used.
         *
         * @param cause the deletion failure cause
         * @return {@code true} if the lookup is in use
         */
        private boolean lookupInUse(Throwable cause) {
            if (cause instanceof ArchetypeServiceException) {
                ArchetypeServiceException exception = (ArchetypeServiceException) cause;
                ArchetypeServiceException.ErrorCode errorCode = exception.getErrorCode();
                return (ArchetypeServiceException.ErrorCode.CannotDeleteLookupInUse.equals(errorCode));
            }
            return false;
        }

        /**
         * Displays an error dialog stating that the lookup couldn't be deleted as its referenced by other objects.
         */
        private void reportLookupInUse() {
            String title = Messages.get("imobject.delete.failed.title");
            String message = Messages.get("lookup.delete.inuse");
            ErrorHelper.show(title, message);
        }

    }
}
