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

package org.openvpms.smartflow.event.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.smartflow.model.InventoryItem;
import org.openvpms.smartflow.model.InventoryItems;
import org.openvpms.smartflow.model.event.InventoryImportedEvent;

import java.util.List;

/**
 * Processor for {@link InventoryImportedEvent}.
 *
 * @author Tim Anderson
 */
public class InventoryImportedEventProcessor extends EventProcessor<InventoryImportedEvent> {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(InventoryImportedEventProcessor.class);

    /**
     * Constructs an {@link InventoryImportedEventProcessor}.
     *
     * @param service the archetype service
     */
    public InventoryImportedEventProcessor(IArchetypeService service) {
        super(service);
    }

    /**
     * Processes an event.
     *
     * @param event the event
     */
    @Override
    public void process(InventoryImportedEvent event) {
        InventoryItems object = event.getObject();
        if (object != null && object.getInventoryitems() != null) {
            imported(object.getInventoryitems());
        }
    }

    /**
     * Process imported inventory items
     *
     * @param items the imported items
     */
    private void imported(List<InventoryItem> items) {
        for (InventoryItem item : items) {
            if (item.getAsyncOperationStatus() != null && item.getAsyncOperationStatus() < 0) {
                log.error("Failed to synchronize product=[id=" + item.getId() + ", name=" + item.getName() + "]: " +
                          item.getAsyncOperationMessage());
            } else {
                log.info("Synchronized product=[id=" + item.getId() + ", name=" + item.getName() + "]");
            }
        }
    }
}
