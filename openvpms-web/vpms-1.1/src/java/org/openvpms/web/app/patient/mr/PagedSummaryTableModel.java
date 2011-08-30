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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.mr;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.PagedIMObjectTableModel;

import java.util.ArrayList;
import java.util.List;


/**
 * Paged act summary table model.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PagedSummaryTableModel extends PagedIMObjectTableModel<Act> {

    /**
     * The archetype short names or the child acts to display.
     */
    private String[] shortNames;


    /**
     * Construct a new <code>PagedSummaryTableModel</code>.
     *
     * @param model      the underlying table model
     * @param shortNames the archetype short names of the child acts to display
     */
    public PagedSummaryTableModel(IMObjectTableModel<Act> model,
                                  String[] shortNames) {
        super(model);
        this.shortNames = shortNames;
    }

    /**
     * Sets the archetype short names of the child acts to display.
     *
     * @param shortNames the archetype short names of the child acts to display
     */
    public void setShortNames(String[] shortNames) {
        this.shortNames = shortNames;
    }

    /**
     * Sets the objects for the current page.
     *
     * @param objects the objects to set
     */
    @Override
    protected void setPage(List<Act> objects) {
        IterableSummary summary = new IterableSummary(objects, shortNames);
        List<Act> acts = new ArrayList<Act>();
        for (Act act : summary) {
            acts.add(act);
        }
        getModel().setObjects(acts);
    }

}
