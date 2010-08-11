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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.admin.lookup;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyTransformer;
import org.openvpms.web.component.property.StringPropertyTransformer;


/**
 * Abstract editor for lookups.
 * <p/>
 * For lookups where there is are both code and name nodes, and
 * the code is hidden, this derives the initial value of code from the name.
 * The derived value is the name with letters converted to uppercase, and
 * anything it is not in the range [A-Z,0-9] replaced with underscores.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractLookupEditor extends AbstractIMObjectEditor {

    /**
     * The code component.
     */
    private Component code;

    /**
     * Creates a new <tt>AbstractLookupEditor</tt>.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be <tt>null</tt>
     * @param layoutContext the layout context
     */
    public AbstractLookupEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);

        Property code = getProperty("code");
        if (code != null) {
            // disable macro expansion for the code node to avoid the node expanding itself
            PropertyTransformer transformer = code.getTransformer();
            if (transformer instanceof StringPropertyTransformer) {
                ((StringPropertyTransformer) transformer).setExpandMacros(false);
            }
        }

        if (object.isNew()) {
            Property name = getProperty("name");
            if (code != null && name != null) {
                if (code.isHidden()) {
                    // derive the code when the name changes
                    name.addModifiableListener(new ModifiableListener() {
                        public void modified(Modifiable modifiable) {
                            updateCode();
                        }
                    });
                }
            }
        }

        Editor codeEditor = getEditor("code");
        if (codeEditor != null) {
            this.code = codeEditor.getComponent();
            this.code.setEnabled(object.isNew()); // only enable the code field for new objects
        }
    }

    /**
     * Save any edits.
     *
     * @return <tt>true</tt> if the save was successful
     */
    @Override
    public boolean save() {
        boolean saved = super.save();
        if (saved && code != null) {
            code.setEnabled(false);
        }
        return saved;
    }

    /**
     * Updates the lookup code, if the object is new.
     * <p/>
     * This uses the code produced by {@link #createCode()}.
     */
    protected void updateCode() {
        if (getObject().isNew()) {
            Property property = getProperty("code");
            if (property != null) {
                String code = createCode();
                property.setValue(code);
            }
        }
    }

    /**
     * Creates a code for the lookup.
     * <p/>
     * This must be unique for lookups of the same archetype to avoid duplicate errors on save.
     * <p/>
     * This implementation creates a code from the name node.
     *
     * @return a new code
     */
    protected String createCode() {
        String code = null;
        String name = (String) getProperty("name").getValue();
        if (name != null) {
            code = name.toUpperCase();
            code = code.replaceAll("[^A-Z0-9]+", "_");
        }
        return code;
    }

}
