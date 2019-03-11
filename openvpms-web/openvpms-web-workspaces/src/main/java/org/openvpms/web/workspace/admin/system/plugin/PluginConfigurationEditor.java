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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.system.plugin;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Editor for <em>entity.pluginConfiguration</em>.
 *
 * @author Tim Anderson
 */
public class PluginConfigurationEditor extends AbstractIMObjectEditor {

    /**
     * Constructs a {@link PluginConfigurationEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public PluginConfigurationEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }


    /**
     * Validates the object.
     *
     * @param validator the validator
     * @return {@code true} if the object and its descendants are valid otherwise {@code false}
     */
    @Override
    protected boolean doValidation(Validator validator) {
        return super.doValidation(validator) && validatePath(validator);
    }

    /**
     * Verifies that the plugin path exists, is a directory, and can be written to.
     *
     * @param validator the validator
     * @return {@code true} if the path is valid
     */
    private boolean validatePath(Validator validator) {
        boolean valid = false;
        Property property = getProperty("path");
        String value = property.getString();
        try {
            Path path = Paths.get(value);
            if (!Files.exists(path)) {
                validator.add(property, new ValidatorError(Messages.format("dir.notfound", path)));
            } else if (!Files.isDirectory(path)) {
                validator.add(property, new ValidatorError(Messages.format("dir.notdir", path)));
            } else if (!Files.isWritable(path)) {
                validator.add(property, new ValidatorError(Messages.format("dir.notwritable", path)));
            } else {
                valid = true;
            }
        } catch (InvalidPathException exception) {
            validator.add(property, new ValidatorError(Messages.format("dir.invalid", value)));
        }
        return valid;
    }


}
