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

package org.openvpms.web.workspace.supplier.vet;

import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.EntityObjectSetQuery;
import org.openvpms.web.component.im.query.ResultSet;

/**
 * Queries <em>party.supplierVeterinarian</em> parties, returning their names, descriptions and current practices.
 *
 * @author Tim Anderson
 * @see VetResultSet
 */
public class VetObjectSetQuery extends EntityObjectSetQuery {

    /**
     * Constructs an {@link VetObjectSetQuery}.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public VetObjectSetQuery(String[] shortNames) {
        super(shortNames);
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
        return new VetResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), sort, getMaxResults(),
                                isDistinct());
    }
}
