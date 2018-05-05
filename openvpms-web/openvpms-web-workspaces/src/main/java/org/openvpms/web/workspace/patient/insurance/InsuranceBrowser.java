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

package org.openvpms.web.workspace.patient.insurance;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.IMTableModel;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.TableComponentFactory;

/**
 * Insurance act browser.
 *
 * @author Tim Anderson
 */
public class InsuranceBrowser extends IMObjectTableBrowser<Act> {
    /**
     * Constructs a {@code InsuranceBrowser} that queries acts using the specified query.
     *
     * @param query   the query
     * @param context the layout context
     */
    public InsuranceBrowser(Query<Act> query, LayoutContext context) {
        super(query, context);
    }

    /**
     * Creates a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMTableModel<Act> createTableModel(LayoutContext context) {
        IMTableModel<Act> model;
        Query<Act> query = getQuery();
        String[] shortNames = query.getShortNames();
        context = new DefaultLayoutContext(context);
        if (!(context.getComponentFactory() instanceof TableComponentFactory)) {
            IMObjectComponentFactory factory = new TableComponentFactory(context);
            context.setComponentFactory(factory);
        }
        if (shortNames.length == 1) {
            model = IMObjectTableModelFactory.create(shortNames, query, context);
        } else {
            model = new InsuranceTableModel(shortNames, context);
        }
        return model;
    }
}
