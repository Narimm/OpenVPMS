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

package org.openvpms.web.workspace.admin.archetype;

import org.openvpms.archetype.component.processor.BatchProcessorListener;
import org.openvpms.tools.archetype.comparator.ArchetypeChange;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;

import java.util.List;

/**
 * Confirms updates nodes of objects associated with changed archetype descriptors.
 *
 * @author Tim Anderson
 */
class ConfirmingBatchArchetypeUpdater {

    /**
     * Prompts to update nodes of objects associated with the supplied archetype changes.
     *
     * @param changes the archetype changes
     */
    public void confirmUpdate(final List<ArchetypeChange> changes) {
        StringBuffer names = new StringBuffer();
        for (ArchetypeChange change : changes) {
            if (names.length() != 0) {
                names.append(", ");
            }
            names.append(change.getNewVersion().getDisplayName());
        }

        String title = Messages.get("archetype.update.title");
        String message = Messages.format("archetype.update.message", names);
        final ConfirmationDialog dialog = new ConfirmationDialog(title, message);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                update(changes);
            }
        });
        dialog.show();
    }

    /**
     * Updates nodes of objects associated with the supplied changed archetype descriptors.
     *
     * @param changes the changed archetypes
     */
    private void update(List<ArchetypeChange> changes) {
        BatchArchetypeUpdater updater = new BatchArchetypeUpdater(changes);
        updater.setListener(new BatchProcessorListener() {
            public void completed() {
            }

            public void error(Throwable exception) {
                ErrorHelper.show(exception);
            }
        });
        updater.process();
    }
}
