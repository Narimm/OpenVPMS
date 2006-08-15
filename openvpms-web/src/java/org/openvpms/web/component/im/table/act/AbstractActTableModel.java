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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.DescriptorTableModel;

import java.util.List;


/**
 * Table model for displaying {@link Act}s. Any "items" nodes are filtered..
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractActTableModel extends DescriptorTableModel {

    /**
     * Creates a new <code>AbstractActTableModel</code>.
     *
     * @param shortNames the act archetype short names
     * @param context    the layout context
     */
    public AbstractActTableModel(String[] shortNames, LayoutContext context) {
        super(shortNames, context);
    }

    /**
     * Determines if selection should be enabled. This implementation returns
     * <code>true</code> if in edit mode.
     *
     * @return <code>true</code> if selection should be enabled; otherwise
     *         <code>false</code>
     */
    // @Override
    public boolean xgetEnableSelection() {
        return getLayoutContext().isEdit();
    }

    /**
     * Returns a filtered list of descriptors for an archetype.
     *
     * @param archetype the archetype
     * @param context   the layout context
     * @return a filtered list of descriptors for the archetype
     */
    @Override
    protected List<NodeDescriptor> getDescriptors(ArchetypeDescriptor archetype,
                                                  LayoutContext context) {
        return filter(archetype.getSimpleNodeDescriptors(), context);
    }

    /**
     * Filters descriptors using the context's default node filter.
     *
     * @param descriptors the column descriptors
     * @param context     the layout context
     * @return the filtered descriptors
     */
    /*  @Override
    protected List<NodeDescriptor> filter(List<NodeDescriptor> descriptors,
                                          LayoutContext context) {
        NodeFilter filter = FilterHelper.chain(new NamedNodeFilter("items"),
                                               context.getDefaultNodeFilter());
        return FilterHelper.filter(null, filter, descriptors);
    }*/

}
