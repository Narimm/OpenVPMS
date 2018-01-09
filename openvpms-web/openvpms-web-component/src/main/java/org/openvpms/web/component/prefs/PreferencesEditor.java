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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.prefs;

import org.openvpms.archetype.rules.prefs.PreferenceService;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;

/**
 * User preferences editor .
 *
 * @author Tim Anderson
 */
public class PreferencesEditor extends AbstractIMObjectEditor {

    /**
     * The party to edit preferences for.
     */
    private final Party party;

    /**
     * If non-null, specifies the source to copy preferences from if the party has none.
     */
    private final Party source;

    /**
     * The preference group collection editor.
     */
    private final PreferenceGroupCollectionEditor groups;

    /**
     * Constructs an {@link PreferencesEditor}.
     *
     * @param party         the party to edit preferences for
     * @param source        if non-null, specifies the source to copy preferences from if the party has none
     * @param layoutContext the layout context
     */
    public PreferencesEditor(Party party, Party source, LayoutContext layoutContext) {
        super(ServiceHelper.getBean(PreferenceService.class).getEntity(party, source), null, layoutContext);
        this.party = party;
        this.source = source;
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
        return new PreferencesEditor(party, source, getLayoutContext());
    }

    /**
     * Returns the help context for the editor.
     * <p/>
     * This returns the context for the selected tab.
     *
     * @return the help context
     */
    @Override
    public HelpContext getHelpContext() {
        IMObjectEditor selected = groups.getSelected();
        return selected != null ? selected.getHelpContext() : super.getHelpContext();
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


