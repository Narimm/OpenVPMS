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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.admin.lookup;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.app.subsystem.AbstractViewCRUDWindow;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.dialog.SelectionDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.lookup.ArchetypeLookupQuery;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.LookupFilter;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.util.AbstractIMObjectDeletionListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.im.util.IMObjectDeletor;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * CRUD window for lookups.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupCRUDWindow extends AbstractViewCRUDWindow<Lookup> {

    /**
     * The 'replace' button.
     */
    private Button replace;

    /**
     * The replace button identifier.
     */
    private static final String REPLACE_ID = "replace";


    /**
     * Constructs a <tt>LookupCRUDWindow</tt>.
     *
     * @param archetypes the archetypes that this may create
     */
    public LookupCRUDWindow(Archetypes<Lookup> archetypes) {
        super(archetypes);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        enableButtons(buttons, true);
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
        Button button = getReplaceButton();
        if (enable) {
            if (!buttons.contains(button)) {
                buttons.add(button);
            }
        } else {
            buttons.remove(button);
        }
    }

    /**
     * Invoked when the delete button is pressed.
     */
    @Override
    protected void onDelete() {
        Lookup object = IMObjectHelper.reload(getObject());
        if (object == null) {
            ErrorDialog.show(Messages.get("imobject.noexist"), getArchetypes().getDisplayName());
        } else {
            IMObjectDeletor.delete(object, new LookupDeletorListener());
        }
    }

    /**
     * Displays a dialog to replace the current lookup with another.
     */
    private void replace() {
        final Lookup lookup = getObject();
        if (lookup != null) {
            LookupQuery query = new ArchetypeLookupQuery(lookup.getArchetypeId().getShortName());
            LookupFilter filter = new LookupFilter(query, false, lookup.getCode());
            final LookupField field = LookupFieldFactory.create(filter);
            String title = Messages.get("lookup.replace.title");
            String message = Messages.get("lookup.replace.message", lookup.getName(), lookup.getCode());
            final SelectionDialog dialog = new SelectionDialog(title, message, field);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onAction(String action) {
                    Lookup selected = field.getSelected();
                    if (selected != null) {
                        confirmReplace(lookup, selected);
                    }
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
     */
    private void confirmReplace(final Lookup source, final Lookup target) {
        String title = Messages.get("lookup.replace.title");
        String message = Messages.get("lookup.replace.confirm", source.getName(), source.getCode(),
                                      target.getName(), target.getCode());
        ConfirmationDialog dialog = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doReplace(source, target);
                onRefresh(source);
            }
        });
        dialog.show();
    }

    /**
     * Replaces references to the source lookup with the target lookup, and deletes the source lookup.
     *
     * @param source the source lookup
     * @param target the target lookup
     */
    private void doReplace(final Lookup source, final Lookup target) {
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        template.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                IArchetypeService service = ServiceHelper.getArchetypeService();
                ILookupService lookupService = ServiceHelper.getLookupService();
                lookupService.replace(source, target);
                service.remove(source);
                return null;
            }
        });
    }

    /**
     * Returns the 'replace' button.
     *
     * @return the replace button.
     */
    private Button getReplaceButton() {
        if (replace == null) {
            replace = ButtonFactory.create(REPLACE_ID, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    replace();
                }
            });
        }
        return replace;
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
         * @return <tt>true</tt> if the lookup is in use
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
