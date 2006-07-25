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

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;


/**
 * Participation editor for document templates, where the parent object
 * is an {@link DocumentAct}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocTemplateParticipationActEditor extends AbstractIMObjectEditor {

    /**
     * The template editor.
     */
    private final IMObjectReferenceEditor _templateEditor;


    /**
     * Constructs a new <code>DocTemplateParticipationActEditor</code>.
     *
     * @param participation the participation
     * @param parent        the parent act
     * @param context       the layout context. May be <code>null</code>
     */
    public DocTemplateParticipationActEditor(Participation participation,
                                             DocumentAct parent,
                                             LayoutContext context) {
        super(participation, parent, context);
        Property act = getProperty("act");
        if (act.getValue() == null) {
            act.setValue(parent.getObjectReference());
        }
        Property entity = getProperty("entity");
        _templateEditor = new IMObjectReferenceEditor(entity, context);
        getEditors().add(_templateEditor);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new IMObjectLayoutStrategy() {
            public Component apply(IMObject object, PropertySet properties,
                                   LayoutContext context) {
                return _templateEditor.getComponent();
            }
        };
    }

}
