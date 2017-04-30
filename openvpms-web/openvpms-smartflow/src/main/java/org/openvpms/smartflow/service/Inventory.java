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

package org.openvpms.smartflow.service;

import org.openvpms.smartflow.model.InventoryItem;
import org.openvpms.smartflow.model.InventoryItems;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Smart Flow Sheet inventory API.
 *
 * @author Tim Anderson
 */
public interface Inventory {

    @GET
    @Path("/inventoryitems")
    @Produces({MediaType.APPLICATION_JSON})
    List<InventoryItem> getInventory();

    /**
     * Adds or updates inventory items.
     *
     * @param items the items to add or update
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Path("/inventoryitems")
    void update(InventoryItems items);

    /**
     * Removes an inventory item, given its identifier.
     *
     * @param id the inventory item identifier
     */
    @DELETE
    @Path("/inventoryitem/{id}")
    void remove(@PathParam("id") String id);

}
