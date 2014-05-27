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

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.AbstractFilteredResultSet;
import org.openvpms.web.component.im.query.AbstractIMObjectQuery;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.query.ResultSet;

import java.util.List;


/**
 * Query for <em>entity.documentTemplate</em>s.
 *
 * @author Tim Anderson
 */
public class DocumentTemplateQuery extends AbstractIMObjectQuery<Entity> {

    /**
     * The types to filter on.
     */
    private String[] types = {};


    /**
     * Constructs a {@link DocumentTemplateQuery}.
     *
     * @throws ArchetypeQueryException if the short name don't match any archetypes
     */
    public DocumentTemplateQuery() {
        super(new String[]{DocumentArchetypes.DOCUMENT_TEMPLATE}, Entity.class);
        setDefaultSortConstraint(NAME_SORT_CONSTRAINT);
        QueryFactory.initialise(this);
    }

    /**
     * Sets the document types to filter on.
     *
     * @param types the types to filter on. If empty, queries all types
     */
    public void setTypes(String... types) {
        this.types = types;
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be {@code null}
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<Entity> query(SortConstraint[] sort) {
        ResultSet<Entity> result = super.query(sort);

        if (types.length != 0) {
            result = new AbstractFilteredResultSet<Entity>(result) {
                @Override
                protected void filter(Entity object, List<Entity> results) {
                    IMObjectBean bean = new IMObjectBean(object);
                    String archetype = bean.getString("archetype");
                    if (archetype != null) {
                        for (String type : types) {
                            if (TypeHelper.matches(archetype, type)) {
                                results.add(object);
                            }
                        }
                    }
                }
            };
        }
        return result;
    }

    /**
     * Determines if the query selects a particular object reference.
     *
     * @param reference the object reference to check
     * @return {@code true} if the object reference is selected by the query
     */
    @Override
    public boolean selects(IMObjectReference reference) {
        if (types.length == 0) {
            return super.selects(reference);
        }
        return QueryHelper.selects(query(), reference);
    }
}
