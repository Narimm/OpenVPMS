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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.edit.IMObjectProperty;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.edit.PropertySet;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.Collection;


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
     * Property to update the document reference via. Used to enable
     * modifications to the parent act to be updated.
     */
    private Property _document;


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
        _templateEditor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                onSelect();
            }
        });
        getEditors().add(_templateEditor);
    }

    /**
     * Sets the property to update document references.
     *
     * @param property the property
     */
    public void setDocument(Property property) {
        _document = property;
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

    /**
     * Invoked when the entity changes.
     */
    private void onSelect() {
        Property entity = getProperty("entity");
        IMObjectReference entityRef = (IMObjectReference) entity.getValue();
        Entity template = (Entity) IMObjectHelper.getObject(entityRef);
        if (template != null) {
            DocumentAct act = getDocumentAct(template);
            IMObjectReference docRef
                    = (act != null) ? act.getDocReference() : null;
            setDocument(docRef);
        }
    }

    private DocumentAct getDocumentAct(Entity template) {
        // @todo - need to access participations via IMObjectProperty as a
        // workaround to OBF-105
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(template);
        IMObjectProperty prop = new IMObjectProperty(
                template, archetype.getNodeDescriptor("template"));
        Collection participations = prop.getValues();
        if (!participations.isEmpty()) {
            Participation p = (Participation) participations.toArray()[0];
            return (DocumentAct) IMObjectHelper.getObject(p.getAct());
        }
        return null;
    }

    private void setDocument(IMObjectReference ref) {
        if (_document != null) {
            _document.setValue(ref);
        } else {
            DocumentAct parent = (DocumentAct) getParent();
            parent.setDocReference(ref);
        }
    }

}
