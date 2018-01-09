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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.component.model.act;

import org.openvpms.component.model.object.PeriodRelationship;

/**
 * Describes a relationship between two {@link Act}s.
 *
 * @author Tim Anderson
 * @author Jim Alateras
 */
public interface ActRelationship extends PeriodRelationship {

    /**
     * Determines if this is a parent/child relationship between two acts.
     * <p>
     * If {@code true} it indicates that the parent act is the owner of the
     * relationship and is responsible for managing its lifecycle. When the
     * parent act is deleted, then the child act must also be deleted.
     *
     * @return {@code true} if this is a parent/child relationship
     */
    boolean isParentChildRelationship();

    /**
     * Determines if this is a parent/child relationship between two acts.
     *
     * @param parentChildRelationship if {@code true</true> it is a parent/child relationship
     */
    void setParentChildRelationship(boolean parentChildRelationship);
}
