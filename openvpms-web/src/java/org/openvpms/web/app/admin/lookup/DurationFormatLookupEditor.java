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
 *  $Id: $
 */

package org.openvpms.web.app.admin.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;

import java.util.UUID;


/**
 * Editor for <em>lookup.durationformat</em> lookups.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class DurationFormatLookupEditor extends AbstractLookupEditor {

    /**
     * Determines if the name node should be displayed.
     */
    private boolean showName = true;


    /**
     * Constructs a <tt>DurationFormatLookupEditor</tt>.
     *
     * @param lookup        the object to edit
     * @param parent        the parent object. May be <tt>null</tt>
     * @param layoutContext the layout context. May be <tt>null</tt>.
     */
    public DurationFormatLookupEditor(Lookup lookup, IMObject parent, LayoutContext layoutContext) {
        super(lookup, parent, layoutContext);
    }

    /**
     * Determines if the name node should be displayed.
     * <p/>
     * Defaults to <tt>true</tt>
     *
     * @param show if <tt>true</tt> show the name node, otherwise hide it
     */
    public void setShowName(boolean show) {
        showName = show;
    }

    /**
     * Initialises the code node.
     */
    @Override
    protected void initCode() {
        getProperty("code").setValue(createCode());
    }

    /**
     * Creates a code for the lookup.
     * <p/>
     * This must be unique for lookups of the same archetype to avoid duplicate errors on save.
     *
     * @return a new code
     */
    @Override
    protected String createCode() {
        return UUID.randomUUID().toString();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new AbstractLayoutStrategy() {
            @Override
            protected NodeFilter getNodeFilter(IMObject object, LayoutContext context) {
                if (!showName) {
                    return FilterHelper.chain(context.getDefaultNodeFilter(), new NamedNodeFilter("name"));
                }
                return super.getNodeFilter(object, context);
            }
        };
    }
}
