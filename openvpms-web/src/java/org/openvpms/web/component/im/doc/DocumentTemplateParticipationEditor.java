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

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;


/**
 * Editor for <em>participation.documentTemplate</em> participation
 * relationships.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentTemplateParticipationEditor
        extends AbstractIMObjectEditor {

    /**
     * The template editor.
     */
    private final IMObjectReferenceEditor templateEditor;


    /**
     * Constructs a new <code>DocumentTemplateParticipationEditor</code>.
     *
     * @param participation the participation
     * @param parent        the parent act
     * @param context       the layout context. May be <code>null</code>
     */
    public DocumentTemplateParticipationEditor(Participation participation,
                                               DocumentAct parent,
                                               LayoutContext context) {
        super(participation, parent, context);
        Property act = getProperty("act");
        if (act.getValue() == null) {
            act.setValue(parent.getObjectReference());
        }
        Property entity = getProperty("entity");
        templateEditor = new AbstractIMObjectReferenceEditor(entity, parent,
                                                             context) {

            /**
             * Creates a query to select objects.
             *
             * @param name a name to filter on. May be <code>null</code>
             * @param name the name to filter on. May be <code>null</code>
             * @return a new query
             * @throws ArchetypeQueryException if the short names don't match any
             *                                 archetypes
             */
            @Override
            @SuppressWarnings("unchecked")
            protected Query<IMObject> createQuery(String name) {
                Query query = super.createQuery(name);
                String shortname = null;
                if (getParent() != null) {
                    shortname = getParent().getArchetypeId().getShortName();
                }
                if (shortname != null
                        && query instanceof DocumentTemplateQuery) {
                    ((DocumentTemplateQuery) query).setArchetype(shortname);

                }
                return query;
            }
        };

        getEditors().add(templateEditor);
    }

    /**
     * Creates a query to select objects.
     *
     * @return a new query
     */
    protected Query getQuery(Query query) {
        return query;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new IMObjectLayoutStrategy() {
            public ComponentState apply(IMObject object, PropertySet properties,
                                        IMObject parent,
                                        LayoutContext context) {
                return new ComponentState(templateEditor.getComponent(),
                                          templateEditor.getFocusGroup());
            }
        };
    }

}
