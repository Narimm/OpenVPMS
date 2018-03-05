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

package org.openvpms.web.workspace.admin.archetype;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Iterator;

/**
 * Editor for <em>descriptor.archetype</em>.
 * <p/>
 * This prevents deletion if the archetype is in use.
 *
 * @author Tim Anderson
 */
public class ArchetypeEditor extends AbstractIMObjectEditor {

    /**
     * Constructs an {@link ArchetypeEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public ArchetypeEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Deletes the object.
     *
     * @throws OpenVPMSException if the delete fails
     */
    @Override
    protected void doDelete() {
        ArchetypeDescriptor descriptor = (ArchetypeDescriptor) getObject();
        String shortName = descriptor.getShortName();
        ArchetypeQuery query = new ArchetypeQuery(shortName);
        query.setMaxResults(1);
        query.add(new NodeSelectConstraint("id"));
        Iterator iterator = new ObjectSetQueryIterator(query);
        if (iterator.hasNext()) {
            throw new ArchetypeEditorException(Messages.get("archetype.delete.inuse"));
        }
        super.doDelete();
    }
}
