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

package org.openvpms.web.component.im.view.act;

import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.PreloadedResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.IMObjectTableModelFactory;
import org.openvpms.web.component.im.table.PagedIMObjectTable;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;

import nextapp.echo2.app.Component;

import java.util.ArrayList;
import java.util.List;


/**
 * Layout strategy that displays a collection of {@link ActRelationship}s in a
 * table.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ActRelationshipTableLayoutStrategy
        implements IMObjectLayoutStrategy {

    /**
     * The act relationship short names.
     */
    private final String[] _shortNames;


    /**
     * Construct a new <code>ActRelationshipTableLayoutStrategy</code>.
     *
     * @param descriptor the act relationship collection descriptor
     */
    public ActRelationshipTableLayoutStrategy(NodeDescriptor descriptor) {
        _shortNames = descriptor.getArchetypeRange();
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object     the object to apply
     * @param properties
     * @param context    the layout context
     * @return the component containing the rendered <code>object</code>
     */
    public Component apply(IMObject object, PropertySet properties,
                           LayoutContext context) {
        IMObjectTableModel model
                = IMObjectTableModelFactory.create(_shortNames, context);
        Act act = (Act) object;
        List<IMObject> relationships = new ArrayList<IMObject>();
        relationships.addAll(act.getSourceActRelationships());
        ResultSet set = new PreloadedResultSet<IMObject>(relationships, 25);
        return new PagedIMObjectTable(model, set);
    }

}
