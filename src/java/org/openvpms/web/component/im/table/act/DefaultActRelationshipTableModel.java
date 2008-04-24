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

package org.openvpms.web.component.im.table.act;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;


/**
 * A table model for {@link ActRelationship}s that models the target acts
 * referred to by the relationships. The model for the target acts is created
 * via {@link IMObjectTableModelFactory} using the union of archetype ranges
 * from each act relationship's target node.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultActRelationshipTableModel
        extends AbstractActRelationshipTableModel<Act> {


    /**
     * Creates a new <code>DefaultActRelationshipTableModel</code>.
     *
     * @param relationshipTypes the act relationship short names
     * @param context           the layout context
     */
    public DefaultActRelationshipTableModel(String[] relationshipTypes,
                                            LayoutContext context) {
        String[] shortNames = ActHelper.getTargetShortNames(relationshipTypes);
        IMObjectTableModel<Act> model
                = IMObjectTableModelFactory.create(shortNames, context);
        setModel(model);
    }

}
