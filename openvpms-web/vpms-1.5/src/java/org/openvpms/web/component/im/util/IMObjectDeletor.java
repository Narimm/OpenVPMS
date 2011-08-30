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

package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.dialog.PopupDialogListener;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * {@link IMObject} deletor.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public final class IMObjectDeletor {

    /**
     * Prevent construction.
     */
    private IMObjectDeletor() {
    }

    /**
     * Attempts to delete an object.
     *
     * @param object   the object to delete
     * @param listener the listener to notify
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> void delete(
            T object, IMObjectDeletionListener<T> listener) {
        try {
            if (object instanceof Entity) {
                Entity entity = (Entity) object;
                if (hasParticipations(
                        entity) && !entity.getArchetypeId().getShortName().equals(
                        "entity.documentTemplate")) {
                    if (object.isActive()) {
                        confirmDeactivate(object, listener);
                    } else {
                        String message = Messages.get(
                                "imobject.delete.deactivated",
                                DescriptorHelper.getDisplayName(object),
                                entity.getName());
                        ErrorDialog.show(message);
                    }
                } else if (hasRelationships(entity)) {
                    confirmDelete(object, listener,
                                  "imobject.delete.withrelationships.message");
                } else {
                    confirmDelete(object, listener, "imobject.delete.message");
                }
            } else {
                confirmDelete(object, listener, "imobject.delete.message");
            }
        } catch (Throwable exception) {
            listener.failed(object, exception);
        }
    }

    /**
     * Determines if an entity has any participations.
     *
     * @param entity the entity
     * @return <code>true</code> if the entity has participations, otherwise
     *         <code>false</code>
     * @throws ArchetypeServiceException for any error
     */
    private static boolean hasParticipations(Entity entity) {
        ArchetypeQuery query
                = new ArchetypeQuery("participation.*", false, false);
        query.add(new ObjectRefNodeConstraint("entity",
                                              entity.getObjectReference()));
        query.setMaxResults(1);
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        return !service.get(query).getResults().isEmpty();
    }

    /**
     * Determines if an entity has any relationships where it is the source
     *
     * @param entity the entity
     * @return <code>true</code> if the entity has relationships where it is
     *         the source, ortherwise <code>false</code>
     */
    private static boolean hasRelationships(Entity entity) {
        IMObjectReference ref = entity.getObjectReference();
        for (EntityRelationship r : entity.getEntityRelationships()) {
            if (r.getSource() != null && r.getSource().equals(ref)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Pops up a dialog prompting if deletion of an object should proceed,
     * deleting it if OK is selected.
     *
     * @param object     the object to delete
     * @param listener   the listener to notify
     * @param messageKey the message resource bundle key
     */
    @SuppressWarnings("unchecked")
    private static <T extends IMObject> void confirmDelete(
            final T object,
            final IMObjectDeletionListener<T> listener, String messageKey) {
        String type = DescriptorHelper.getDisplayName(object);
        String title = Messages.get("imobject.delete.title", type);
        String name = (object.getName() != null) ? object.getName() : type;
        String message = Messages.get(messageKey, name);
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message, true);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                doDelete(object, listener);
            }
        });
        dialog.show();
    }

    /**
     * Performs the deletion in a transaction context.
     *
     * @param object   the object to delete
     * @param listener the listener to notify
     * @return <tt>true</tt> if the object was deleted successfully
     */
    private static <T extends IMObject> boolean doDelete(final T object,
                                                         final IMObjectDeletionListener<T> listener) {
        boolean deleted = false;
        try {
            DefaultLayoutContext context = new DefaultLayoutContext(true);
            context.setDeletionListener(new DeletionListenerAdapter<T>(object, listener));
            final IMObjectEditor editor = IMObjectEditorFactory.create(object, context);
            TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
            Object result = template.execute(new TransactionCallback() {
                public Object doInTransaction(TransactionStatus status) {
                    return editor.delete();
                }
            });
            deleted = (result != null) && (Boolean) result;
            if (deleted) {
                listener.deleted(object);
            }
        } catch (Throwable exception) {
            listener.failed(object, exception);
        }
        return deleted;
    }

    /**
     * Pops up a dialog prompting if deactivation of an object should proceed,
     * deactivating it if OK is selected.
     *
     * @param object   the object to deactivate
     * @param listener the listener to notify
     */
    @SuppressWarnings("unchecked")
    private static <T extends IMObject> void confirmDeactivate(
            final T object, final IMObjectDeletionListener<T> listener) {
        String type = DescriptorHelper.getDisplayName(object);
        String title = Messages.get("imobject.deactivate.title", type);
        String message = Messages.get("imobject.deactivate.message",
                                      object.getName());
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message, true);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                object.setActive(false);
                if (SaveHelper.save(object)) {
                    listener.deactivated(object);
                }
            }
        });
        dialog.show();
    }

    /**
     * Deletion listener that ensures type parameters aren't violated.
     */
    private static class DeletionListenerAdapter<T extends IMObject>
            implements IMObjectDeletionListener<IMObject> {

        /**
         * The object to pass to the listener.
         */
        private T object;

        /**
         * The listener to delegate to.
         */
        private IMObjectDeletionListener<T> listener;

        /**
         * Constructs a <tt>DeletionListenerAdapter</tt>.
         *
         * @param object   the object to pass to the listener
         * @param listener the listener to delegate to
         */
        public DeletionListenerAdapter(T object, IMObjectDeletionListener<T> listener) {
            this.object = object;
            this.listener = listener;
        }

        /**
         * Notifies that an object has been deleted.
         *
         * @param object the deleted object
         */
        public void deleted(IMObject object) {
            listener.deleted(this.object);
        }

        /**
         * Notifies that an object has been deactivated.
         *
         * @param object the deactivated object
         */
        public void deactivated(IMObject object) {
            listener.deactivated(this.object);
        }

        /**
         * Notifies that an object has failed to be deleted.
         *
         * @param object the object that failed to be deleted
         * @param cause  the reason for the failure
         */
        public void failed(IMObject object, Throwable cause) {
            listener.failed(this.object, cause);
        }

        /**
         * Notifies that an object has failed to be deleted.
         *
         * @param object the object that failed to be deleted
         * @param cause  the reason for the failure
         * @param editor the editor that performed the deletion
         */
        public void failed(IMObject object, Throwable cause, IMObjectEditor editor) {
            listener.failed(this.object, cause, editor);
        }
    }
}
