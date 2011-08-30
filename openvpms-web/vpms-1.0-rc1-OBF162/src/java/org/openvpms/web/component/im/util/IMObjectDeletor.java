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

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


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
     * @param object the object to delete
     */
    @SuppressWarnings("unchecked")
    public static <T extends IMObject> void delete(
            T object, IMObjectDeletorListener<T> listener) {
        try {
            if (object instanceof Entity) {
                Entity entity = (Entity) object;
                if (hasParticipations(entity)) {
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
        } catch (OpenVPMSException exception) {
            String title = Messages.get("imobject.delete.failed.title");
            ErrorHelper.show(title, exception);
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
            final IMObjectDeletorListener<T> listener, String messageKey) {
        String type = DescriptorHelper.getDisplayName(object);
        String title = Messages.get("imobject.delete.title", type);
        String message = Messages.get(messageKey, object.getName());
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message, true);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    try {
                        IMObjectEditor editor = IMObjectEditorFactory.create(
                                object, new DefaultLayoutContext(true));
                        if (editor.delete()) {
                            listener.deleted(object);
                        }
                    } catch (OpenVPMSException exception) {
                        String title = Messages.get(
                                "imobject.delete.failed.title");
                        ErrorHelper.show(title, exception);
                    }
                }
            }
        });
        dialog.show();
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
            final T object, final IMObjectDeletorListener<T> listener) {
        String type = DescriptorHelper.getDisplayName(object);
        String title = Messages.get("imobject.deactivate.title", type);
        String message = Messages.get("imobject.deactivate.message",
                                      object.getName());
        final ConfirmationDialog dialog
                = new ConfirmationDialog(title, message, true);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent e) {
                if (ConfirmationDialog.OK_ID.equals(dialog.getAction())) {
                    object.setActive(false);
                    if (SaveHelper.save(object)) {
                        listener.deactivated(object);
                    }
                }
            }
        });
        dialog.show();
    }

}
