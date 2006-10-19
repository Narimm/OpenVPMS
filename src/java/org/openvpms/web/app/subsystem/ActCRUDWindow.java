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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.print.IMObjectPrinter;
import org.openvpms.web.component.im.print.IMObjectPrinterListener;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;


/**
 * CRUD Window for acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ActCRUDWindow extends AbstractViewCRUDWindow {

    /**
     * Act in progress status type.
     */
    protected static final String INPROGRESS_STATUS = "In Progress";

    /**
     * Act completed status type.
     */
    protected static final String COMPLETED_STATUS = "Completed";

    /**
     * Act posted status type. Acts with this status can't be edited or
     * deleted.
     */
    protected static final String POSTED_STATUS = "Posted";


    /**
     * Create a new <code>ActCRUDWindow</code>.
     *
     * @param type       display name for the types of objects that this may
     *                   create
     * @param shortNames the short names of archetypes that this may create
     */
    public ActCRUDWindow(String type, ShortNames shortNames) {
        super(type, shortNames);
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the act
     * @return <code>true</code> if the act can be edited, otherwise
     *         <code>false</code>
     */
    protected boolean canEdit(Act act) {
        String status = act.getStatus();
        return !POSTED_STATUS.equals(status);
    }

    /**
     * Determines if an act can be deleted.
     *
     * @param act the act
     * @return <code>true</code> if the act can be deleted, otherwise
     *         <code>false</code>
     */
    protected boolean canDelete(Act act) {
        String status = act.getStatus();
        return !POSTED_STATUS.equals(status);
    }

    /**
     * Invoked when the edit button is pressed. This popups up an {@link
     * EditDialog}.
     */
    @Override
    protected void onEdit() {
        IMObject object = getObject();
        if (object != null) {
            Act act = (Act) object;
            if (canEdit(act)) {
                super.onEdit();
            } else {
                showStatusError(act, "act.noedit.title", "act.noedit.message");
            }
        }
    }

    /**
     * Invoked when the delete button is pressed.
     */
    @Override
    protected void onDelete() {
        Act act = (Act) getObject();
        if (canDelete(act)) {
            super.onDelete();
        } else {
            showStatusError(act, "act.nodelete.title", "act.nodelete.message");
        }
    }

    /**
     * Creates a new printer.
     *
     * @param object the object to print
     * @return a new printer
     */
    @Override
    protected IMObjectPrinter createPrinter(IMObject object) {
        IMObjectPrinter printer = super.createPrinter(object);
        printer.setListener(new IMObjectPrinterListener() {
            public void printed(IMObject object) {
                ActCRUDWindow.this.printed(object);
            }

            public void cancelled(IMObject object) {
                // no-op
            }

            public void failed(IMObject object, Throwable cause) {
                // no-op
            }
        });
        return printer;
    }

    /**
     * Invoked when an object has been successfully printed.
     *
     * @param object the object
     */
    protected void printed(IMObject object) {
        Act act = (Act) object;
        try {
            String status = act.getStatus();
            if (!POSTED_STATUS.equals(status)) {
                act.setStatus(POSTED_STATUS);
                setPrintStatus(act, true);
                SaveHelper.save(act);
                setObject(act);
                CRUDWindowListener listener = getListener();
                if (listener != null) {
                    listener.saved(act, false);
                }
            }
        } catch (Throwable exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Sets the print status.
     *
     * @param act     the act
     * @param printed the print status
     */
    protected void setPrintStatus(Act act, boolean printed) {
        ArchetypeDescriptor archetype = getArchetypeDescriptor();
        NodeDescriptor descriptor = archetype.getNodeDescriptor("printed");
        if (descriptor != null) {
            descriptor.setValue(act, printed);
        }
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

}
