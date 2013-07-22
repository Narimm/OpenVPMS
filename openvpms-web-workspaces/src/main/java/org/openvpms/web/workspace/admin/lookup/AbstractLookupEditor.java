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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */
package org.openvpms.web.workspace.admin.lookup;

import nextapp.echo2.app.Component;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertyTransformer;
import org.openvpms.web.component.property.StringPropertyTransformer;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * Abstract editor for lookups.
 * <p/>
 * For lookups where there is both code and name nodes, and
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
     * Constructs an <tt>AbstractLookupEditor</tt>.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be <tt>null</tt>
     * @param layoutContext the layout context
     */
    public AbstractLookupEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);

        disableMacroExpansion("code");

        if (object.isNew() && getProperty("code") != null) {
            initCode();
        }

        Editor codeEditor = getEditor("code");
        if (codeEditor != null) {
            code = codeEditor.getComponent();
            code.setEnabled(object.isNew()); // only enable the code field for new objects
        }
    }

    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return <code>true</code> if the object and its descendents are valid
     *         otherwise <code>false</code>
     */
    @Override
    public boolean validate(Validator validator) {
        boolean valid = super.validate(validator);
        if (valid) {
            valid = validateCode(validator);
        }
        return valid;
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
     * Initialises the code node.
     * <p/>
     * This is only invoked if the lookup is new.
     * <p/>
     * This implementation registers a listener to invoke {@link #updateCode()} when the name node changes.
     * If the lookup doesn't have name node, this implementation is a no-op.
     */
    protected void initCode() {
        Property name = getProperty("name");
        if (name != null) {
            Property code = getProperty("code");
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

    /**
     * Validates the lookup code.
     * <p/>
     * If the lookup is new, this implementation verifies that the code is unique within the lookup's archetype to
     * avoid duplicate lookup errors.
     *
     * @param validator the validator
     * @return <tt>true</tt> if the code is valid
     */
    protected boolean validateCode(Validator validator) {
        boolean result = true;

        Lookup lookup = (Lookup) getObject();
        if (lookup.isNew()) {
            String code = lookup.getCode();
            if (!StringUtils.isEmpty(code)) {
                String node = "code";
                Property property = getProperty(node);
                String name = (property != null) ? property.getDisplayName() : node;
                String archetype = lookup.getArchetypeId().getShortName();
                if (ServiceHelper.getLookupService().getLookup(archetype, code) != null) {
                    String message = Messages.format("lookup.validation.duplicate", getDisplayName(), name, code);
                    validator.add(this, new ValidatorError(archetype, node, message));
                    result = false;
                }
            }
        }
        return result;
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

    /**
     * Disables macro expansion of a node, to avoid it expanding itself.
     *
     * @param name the node name
     */
    protected void disableMacroExpansion(String name) {
        Property property = getProperty(name);
        if (property != null) {
            PropertyTransformer transformer = property.getTransformer();
            if (transformer instanceof StringPropertyTransformer) {
                ((StringPropertyTransformer) transformer).setExpandMacros(false);
            }
        }
    }

}
