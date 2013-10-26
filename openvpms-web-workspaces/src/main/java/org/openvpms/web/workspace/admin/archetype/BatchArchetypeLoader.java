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

import org.openvpms.archetype.component.processor.AbstractAsynchronousBatchProcessor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptors;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.tools.archetype.comparator.ArchetypeChange;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Loads a batch of {@link ArchetypeDescriptor}s, providing prompting to
 * replace duplicates, and error handling.
 *
 * @author Tim Anderson
 */
public class BatchArchetypeLoader extends AbstractAsynchronousBatchProcessor<ArchetypeDescriptor> {


    /**
     * Tracks the archetype changes.
     */
    private List<ArchetypeChange> changes = new ArrayList<ArchetypeChange>();


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
    public List<ArchetypeChange> getChanges() {
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
        IArchetypeService service = ServiceHelper.getArchetypeService();
        service.validateObject(descriptor);
        String shortName = descriptor.getShortName();
        ArchetypeDescriptor existing = getArchetypeDescriptor(shortName, service);
        if (existing != null) {
            promptOnReplace(descriptor, existing);
        } else {
            // Check if there is a cached version - these aren't deleted when an archetype descriptor is deleted
            ArchetypeDescriptor cached = service.getArchetypeDescriptor(shortName);
            save(descriptor, cached, service);
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
     * @param existing   the existing instance. May be {@code null}
     * @param service    the archetype service
     */
    private void save(ArchetypeDescriptor descriptor, ArchetypeDescriptor existing, IArchetypeService service) {
        service.save(descriptor);
        changes.add(new ArchetypeChange(existing, descriptor));
    }

    /**
     * Prompts to replace a descriptor if it already exists.
     *
     * @param descriptor the new instance
     * @param existing   the existing instance
     */
    private void promptOnReplace(final ArchetypeDescriptor descriptor, final ArchetypeDescriptor existing) {
        String[] buttons;
        if (getIterator().hasNext()) {
            buttons = ConfirmationDialog.OK_SKIP_CANCEL;
        } else {
            buttons = ConfirmationDialog.OK_CANCEL;
        }
        String title = Messages.get("archetype.import.replace.title");
        String message = Messages.format("archetype.import.replace.message", descriptor.getShortName());
        final ConfirmationDialog dialog = new ConfirmationDialog(title, message, buttons);
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
    private void replace(ArchetypeDescriptor descriptor, ArchetypeDescriptor existing) {
        SaveHelper.replace(existing, descriptor);
        changes.add(new ArchetypeChange(existing, descriptor));
    }

    /**
     * Returns the archetype descriptor corresponding to the short name.
     * <p/>
     * If an archetype descriptor with the same short name has been deleted, it will still be present in the cache, so
     * need to check the database to determine if an existing archetype descriptor should be replaced.
     *
     * @param shortName the descriptor short name
     * @param service   the archetype service
     * @return the corresponding archetype descriptor, or {@code null} if none is found
     */
    private ArchetypeDescriptor getArchetypeDescriptor(String shortName, IArchetypeService service) {
        ArchetypeDescriptor result = service.getArchetypeDescriptor(shortName);
        if (result != null) {
            result = (ArchetypeDescriptor) service.get(result.getObjectReference());
        }
        return result;
    }
}
