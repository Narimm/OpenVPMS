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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.doc;

import java.util.ArrayList;
import java.util.List;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.web.component.im.query.PreloadedResultSet;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.query.TableBrowser;

/**
 *Documnet Template Table Browser to filter Documenttemplates by archetype name
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */

public class DocumentTemplateTableBrowser<T extends IMObject> extends TableBrowser {

    /**
     * Maximum no. of rows to display.
     */
    private static final int ROWS = 15;

    /**
     * The shortname to filter the query too.  If null then no filtering.
     */
    private String _shortName;
    
    /**
     * @param query
     */
    public DocumentTemplateTableBrowser(Query query, String shortName) {
        super(query);
        _shortName = shortName;
    }

    /**
     * Performs the query.
     * Gets the Documnet Templates and filters them.
     *
     * @return the query result set
     */
    @Override
    protected ResultSet<T> doQuery() {
        ResultSet result;
        if(_shortName == null || _shortName.length() == 0)
            result = getQuery().query(null);
        else {
            ArchetypeQuery query = new ArchetypeQuery("entity.documentTemplate",
                    false, true);
            query.setFirstRow(0);
            query.setNumOfRows(ArchetypeQuery.ALL_ROWS);
            IArchetypeService service = ArchetypeServiceHelper.getArchetypeService();
            List<IMObject> rows = service.get(query).getRows();
            List<IMObject> objects = new ArrayList<IMObject>();
            for (IMObject object : rows) {
                EntityBean bean = new EntityBean((Entity) object);
                String archetype = bean.getString("archetype");
                if (archetype == null || archetype.length() == 0 || TypeHelper.matches(_shortName, archetype)) {
                    objects.add(object);
                }
            }
            result = new PreloadedResultSet<IMObject>(objects, getQuery().getMaxRows());
        }
        
        return result;
    }


}
