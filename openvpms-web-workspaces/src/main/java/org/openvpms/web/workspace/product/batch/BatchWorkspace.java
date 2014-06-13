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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.batch;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.workspace.ResultSetCRUDWorkspace;

/**
 * Product batch workspace.
 *
 * @author Tim Anderson
 */
public class BatchWorkspace extends ResultSetCRUDWorkspace<Entity> {

    /**
     * Constructs a {@link BatchWorkspace}.
     *
     * @param context the context
     */
    public BatchWorkspace(Context context) {
        super("product", "batch", context);
        setArchetypes(Entity.class, "entity.productBatch");
    }

}
