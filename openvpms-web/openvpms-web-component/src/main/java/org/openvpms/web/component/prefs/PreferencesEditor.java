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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.prefs;

import org.openvpms.archetype.rules.prefs.PreferenceService;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.system.ServiceHelper;

/**
 * User preferences editor .
 *
 * @author Tim Anderson
 */
public class PreferencesEditor extends AbstractIMObjectEditor {

    /**
     * The user to edit preferences for.
     */
    private final User user;

    /**
     * The preference group collection editor.
     */
    private final PreferenceGroupCollectionEditor groups;

    /**
     * Constructs an {@link PreferencesEditor}.
     *
     * @param user          the user to edit preferences for
     * @param layoutContext the layout context
     */
    public PreferencesEditor(User user, LayoutContext layoutContext) {
        super(ServiceHelper.getBean(PreferenceService.class).getEntity(user), null, layoutContext);
        this.user = user;
        groups = new PreferenceGroupCollectionEditor(getCollectionProperty("groups"), (Entity) getObject(),
                                                     getLayoutContext());
        getEditors().add(groups);
    }

    /**
     * Returns a title for the editor.
     *
     * @return a title for the editor
     */
    @Override
    public String getTitle() {
        return getDisplayName();
    }

    /**
     * Creates a new instance of the editor, with the latest instance of the object to edit.
     *
     * @return a new instance
     * @throws OpenVPMSException if a new instance cannot be created
     */
    @Override
    public IMObjectEditor newInstance() {
        return new PreferencesEditor(user, getLayoutContext());
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
            public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
                return new ComponentState(groups);
            }
        };
    }
}


