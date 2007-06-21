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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.admin.archetype;

import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.dialog.ConfirmationDialog;
import org.openvpms.web.component.property.ValidationHelper;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;

import java.util.Iterator;
import java.util.List;


/**
 * Loads a batch of {@link ArchetypeDescriptor}, providing prompting to
 * replace duplicates, and error handling.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BatchLoader {

    public interface Listener {

        /**
         * Invoked when the load successfully completes.
         *
         * @param descriptor the first successfully loaded descriptor.
         *                   May be <tt>null</tt>
         */
        void completed(ArchetypeDescriptor descriptor);
    }

    /**
     * Iterator over the descriptors to load.
     */
    private Iterator<ArchetypeDescriptor> iterator;

    /**
     * The listener to notify on completion. May be <tt>null</tt>
     */
    private final Listener listener;

    /**
     * The first descriptor successfully loaded. May be <tt>null</tt>
     */
    private ArchetypeDescriptor first;


    /**
     * Constructs a new <tt>BatchLoader</tt>.
     *
     * @param descriptors the descriptor to load
     * @param listener    the listener to notify on completion.
     *                    May be <tt>null</tt>
     */
    public BatchLoader(ArchetypeDescriptors descriptors,
                       Listener listener) {
        iterator = descriptors.getArchetypeDescriptors().values().iterator();
        this.listener = listener;
    }

    /**
     * Initiates loading of the descriptors.
     */
    public void load() {
        loadNext();
    }

    /**
     * Loads the next descriptor, not notifies the listener of completion if
     * there is no next descriptor.
     */
    private void loadNext() {
        if (iterator.hasNext()) {
            ArchetypeDescriptor descriptor = iterator.next();
            try {
                load(descriptor);
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        } else if (listener != null) {
            listener.completed(first);
        }
    }

    /**
     * Loads an archetype descriptor.
     *
     * @param descriptor the descriptor to load
     */
    private void load(ArchetypeDescriptor descriptor) {
        try {
            IArchetypeService service
                    = ArchetypeServiceHelper.getArchetypeService();
            List<ValidatorError> errors = ValidationHelper.validate(descriptor,
                                                                    service);
            if (errors == null) {
                String shortName = descriptor.getShortName();
                ArchetypeDescriptor existing
                        = service.getArchetypeDescriptor(shortName);
                if (existing != null) {
                    promptOnReplace(descriptor, existing);
                } else {
                    save(descriptor, service);
                }
            }
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Saves an archetype descriptor, and triggers loading any others.
     *
     * @param descriptor the descriptor to save
     * @param service    the archetype service
     */
    private void save(ArchetypeDescriptor descriptor,
                      IArchetypeService service) {
        try {
            service.save(descriptor);
            if (first == null) {
                first = descriptor;
            }
            loadNext();
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }

    /**
     * Prompts to replace a descriptor if it already exists.
     *
     * @param descriptor the new instance
     * @param existing   the existing instance
     */
    private void promptOnReplace(final ArchetypeDescriptor descriptor,
                                 final ArchetypeDescriptor existing) {
        String[] buttons;
        if (iterator.hasNext()) {
            buttons = ConfirmationDialog.OK_SKIP_CANCEL;
        } else {
            buttons = ConfirmationDialog.OK_CANCEL;
        }
        String title = Messages.get("archetype.import.replace.title");
        String message = Messages.get("archetype.import.replace.message",
                                      descriptor.getShortName());
        final ConfirmationDialog dialog = new ConfirmationDialog(title, message,
                                                                 buttons);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                String action = dialog.getAction();
                if (ConfirmationDialog.OK_ID.equals(action)) {
                    replace(descriptor, existing);
                } else if (ConfirmationDialog.SKIP_ID.equals(action)) {
                    loadNext();
                }
            }
        });
        dialog.show();
    }

    /**
     * Replaces an existing descriptor.
     *
     * @param descriptor the new instance
     * @param existing   the existing instance
     */
    private void replace(ArchetypeDescriptor descriptor,
                         ArchetypeDescriptor existing) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        try {
            service.remove(existing);
            save(descriptor, service);
        } catch (OpenVPMSException exception) {
            ErrorHelper.show(exception);
        }
    }
}
