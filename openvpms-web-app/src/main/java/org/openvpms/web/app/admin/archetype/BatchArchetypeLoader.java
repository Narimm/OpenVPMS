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

import org.openvpms.archetype.component.processor.AbstractAsynchronousBatchProcessor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.tools.archetype.loader.Change;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.i18n.Messages;

import java.util.ArrayList;
import java.util.List;


/**
 * Loads a batch of {@link ArchetypeDescriptor}s, providing prompting to
 * replace duplicates, and error handling.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class BatchArchetypeLoader
    extends AbstractAsynchronousBatchProcessor<ArchetypeDescriptor> {


    /**
     * Tracks the archetype changes.
     */
    private List<Change> changes = new ArrayList<Change>();


    /**
     * Constructs a new <tt>BatchLoader</tt>.
     *
     * @param descriptors the descriptor to load
     */
    public BatchArchetypeLoader(ArchetypeDescriptors descriptors) {
        super(descriptors.getArchetypeDescriptors().values().iterator());
    }

    /**
     * Returns the archetypes that have changed.
     *
     * @return the archetypes the have changed
     */
    public List<Change> getChanges() {
        return changes;
    }

    /**
     * Processes an object.
     *
     * @param descriptor the object to process
     * @throws org.openvpms.component.business.service.archetype.ArchetypeServiceException
     *          if the descriptor fails to validate
     */
    protected void process(ArchetypeDescriptor descriptor) {
        IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
        service.validateObject(descriptor);
        String shortName = descriptor.getShortName();
        ArchetypeDescriptor existing = service.getArchetypeDescriptor(shortName);
        if (existing != null) {
            promptOnReplace(descriptor, existing);
        } else {
            save(descriptor, service);
        }
    }

    /**
     * Invoked if an error occurs processing the batch.
     * Notifies any listener.
     *
     * @param exception the cause
     */
    @Override
    protected void notifyError(Throwable exception) {
        setSuspend(true);
        super.notifyError(exception);
    }

    /**
     * Saves an archetype descriptor.
     *
     * @param descriptor the descriptor to save
     * @param service    the archetype service
     */
    private void save(ArchetypeDescriptor descriptor,
                      IArchetypeService service) {
        service.save(descriptor);
        changes.add(new Change(descriptor));
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
        if (getIterator().hasNext()) {
            buttons = ConfirmationDialog.OK_SKIP_CANCEL;
        } else {
            buttons = ConfirmationDialog.OK_CANCEL;
        }
        String title = Messages.get("archetype.import.replace.title");
        String message = Messages.get("archetype.import.replace.message",
                                      descriptor.getShortName());
        final ConfirmationDialog dialog = new ConfirmationDialog(title, message,
                                                                 buttons);
        setSuspend(true);
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                replace(descriptor, existing);
                process(); // process the next descriptor
            }

            @Override
            public void onSkip() {
                // skip the descriptor and process the next
                process();
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
        SaveHelper.replace(existing, descriptor);
        changes.add(new Change(existing, descriptor));
    }
}
