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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;


/**
 * A table model for {@link IMObjectRelationship}s that models the target objects referred to by the relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultDelegatingIMObjectRelationshipTargetTableModel
    extends DelegatingIMObjectRelationshipTargetTableModel<IMObjectRelationship, IMObject> {

    /**
     * Constructs a <tt>DefaultDelegatingIMObjectRelationshipTargetTableModel</tt>.
     * <p/>
     * The model is determined by the archetype short names supported by the "target" node.
     *
     * @param relationshipTypes the act relationship short names
     * @param context           the layout context
     */
    public DefaultDelegatingIMObjectRelationshipTargetTableModel(String[] relationshipTypes, LayoutContext context) {
        String[] shortNames = getTargetShortNames(relationshipTypes);
        IMObjectTableModel<IMObject> model = IMObjectTableModelFactory.create(shortNames, context);
        setModel(model);
    }

    /**
     * Constructs a <tt>DefaultDelegatingIMObjectRelationshipTargetTableModel</tt>.
     *
     * @param model the model to delegate to
     */
    public DefaultDelegatingIMObjectRelationshipTargetTableModel(IMObjectTableModel<IMObject> model) {
        setModel(model);
    }

}