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
 */

package org.openvpms.web.workspace.admin.lookup;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.DefaultLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;

import java.util.UUID;


/**
 * Editor for <em>lookup.durationformat</em> lookups.
 *
 * @author Tim Anderson
 */
public class DurationFormatLookupEditor extends AbstractLookupEditor {

    /**
     * Determines if the name node should be displayed.
     */
    private boolean showName = true;


    /**
     * Constructs a {@link DurationFormatLookupEditor}.
     *
     * @param lookup        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context. May be {@code null}.
     */
    public DurationFormatLookupEditor(Lookup lookup, IMObject parent, LayoutContext layoutContext) {
        super(lookup, parent, layoutContext);
    }

    /**
     * Determines if the name node should be displayed.
     * <p/>
     * Defaults to {@code true}.
     *
     * @param show if {@code true} show the name node, otherwise hide it
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
        ArchetypeNodes nodes = (showName) ? AbstractLayoutStrategy.DEFAULT_NODES : new ArchetypeNodes().exclude("name");
        return new DefaultLayoutStrategy(nodes);
    }
}
