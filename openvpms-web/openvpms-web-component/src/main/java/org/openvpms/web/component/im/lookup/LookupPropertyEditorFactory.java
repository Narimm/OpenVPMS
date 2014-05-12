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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.lookup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertion;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionFactory;
import org.openvpms.component.business.service.archetype.helper.lookup.RemoteLookup;
import org.openvpms.web.component.im.archetype.ArchetypeHandler;
import org.openvpms.web.component.im.archetype.ArchetypeHandlers;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.system.ServiceHelper;

/**
 * Factory for {@link LookupPropertyEditor} instances.
 *
 * @author Tim Anderson
 */
public class LookupPropertyEditorFactory {

    /**
     * Editor implementations.
     */
    private static ArchetypeHandlers<LookupPropertyEditor> editors;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(LookupPropertyEditorFactory.class);

    /**
     * Prevent construction.
     */
    private LookupPropertyEditorFactory() {
    }

    /**
     * Creates a new editor.
     *
     * @param property the property to edit
     * @param parent   the parent object
     * @param context  the layout context
     * @return an editor for {@code object}
     */
    public static LookupPropertyEditor create(Property property, IMObject parent, LayoutContext context) {
        LookupPropertyEditor result = null;
        ArchetypeHandler<LookupPropertyEditor> handler = null;
        String shortName = null;
        if (property.getDescriptor() != null) {
            LookupAssertion assertion = LookupAssertionFactory.create(property.getDescriptor(),
                                                                      ServiceHelper.getArchetypeService(),
                                                                      ServiceHelper.getLookupService());
            if (assertion instanceof RemoteLookup) {
                shortName = ((RemoteLookup) assertion).getShortName();
                handler = getEditors().getHandler(shortName);
            }
        }

        if (handler != null) {
            try {
                try {
                    result = handler.create(new Object[]{shortName, property, parent, context});
                } catch (NoSuchMethodException ignore) {
                    try {
                        result = handler.create(new Object[]{property, parent, context});
                    } catch (NoSuchMethodException ignore2) {
                        result = handler.create(new Object[]{property, parent});
                    }
                }
            } catch (Throwable throwable) {
                log.error(throwable, throwable);
            }
        }
        if (result == null) {
            result = new DefaultLookupPropertyEditor(property, parent);
        }
        return result;
    }

    /**
     * Returns the editors.
     *
     * @return the editors
     */
    private static synchronized ArchetypeHandlers<LookupPropertyEditor> getEditors() {
        if (editors == null) {
            editors = new ArchetypeHandlers<LookupPropertyEditor>("LookupPropertyEditorFactory.properties",
                                                                  LookupPropertyEditor.class);
        }
        return editors;
    }

}
