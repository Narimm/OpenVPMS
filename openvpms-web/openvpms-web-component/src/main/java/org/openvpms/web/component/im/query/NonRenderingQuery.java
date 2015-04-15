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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;


/**
 * A query that doesn't render a visible component.
 *
 * @author Tim Anderson
 */
public abstract class NonRenderingQuery<T> extends AbstractQuery<T> {

    /**
     * The query component.
     */
    private Label component;

    /**
     * The focus group.
     */
    private FocusGroup group;


    /**
     * Constructs a {@link NonRenderingQuery} that queries objects with the specified primary short names.
     *
     * @param shortNames the archetype short names
     * @param type       the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public NonRenderingQuery(String[] shortNames, Class type) {
        this(shortNames, true, type);
    }

    /**
     * Constructs a {@link NonRenderingQuery} that queries objects with the specified short names.
     *
     * @param shortNames  the archetype short names
     * @param primaryOnly if {@code true} only include primary archetypes
     * @param type        the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public NonRenderingQuery(String[] shortNames, boolean primaryOnly, Class type) {
        super(shortNames, primaryOnly, type);
        group = new FocusGroup("NonRenderingQuery");
    }

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    public Component getComponent() {
        if (component == null) {
            component = LabelFactory.create();
        }
        return component;
    }

    /**
     * Returns the focus group for the component.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return group;
    }
}
