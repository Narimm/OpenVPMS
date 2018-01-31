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

package org.openvpms.web.workspace.admin.job;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static org.openvpms.archetype.rules.doc.DocumentArchetypes.DOCUMENT_TEMPLATE_ACT;

/**
 * An editor for <em>entity.jobDocumentLoader</em>.
 *
 * @author Tim Anderson
 */
public class DocumentLoaderJobConfigurationEditor extends AbstractJobConfigurationEditor {

    /**
     * Constructs a {@link DocumentLoaderJobConfigurationEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public DocumentLoaderJobConfigurationEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
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
        return super.doValidation(validator) && validateDirs(validator) && validateIdPattern(validator)
               && validateArchetypes(validator);
    }

    /**
     * Validates the source, target and error directories.
     *
     * @param validator the validator
     * @return {@code true} if the directories are valid
     */
    private boolean validateDirs(Validator validator) {
        boolean result = false;
        Property source = getProperty("sourceDir");
        Property target = getProperty("targetDir");
        Property error = getProperty("errorDir");
        File sourceDir = new File(source.getString());
        File targetDir = new File(target.getString());
        File errorDir = error.getString() != null ? new File(error.getString()) : null;
        if (validateDir(source, sourceDir, validator) && validateDir(target, targetDir, validator)
            && (errorDir == null || validateDir(error, errorDir, validator))) {
            if (sourceDir.equals(targetDir)) {
                String message = Messages.format("docload.dir.samedirs", source.getDisplayName(),
                                                 target.getDisplayName());
                validator.add(this, new ValidatorError(message));
            } else if (errorDir != null && sourceDir.equals(errorDir)) {
                String message = Messages.format("docload.dir.samedirs", source.getDisplayName(),
                                                 error.getDisplayName());
                validator.add(this, new ValidatorError(message));
            } else {
                result = true;
            }
        }
        return result;
    }

    /**
     * Validates a directory.
     *
     * @param property  the directory property
     * @param file      the file corresponding to the property
     * @param validator the validator
     * @return {@code true} if the file exists and is a directory
     */
    private boolean validateDir(Property property, File file, Validator validator) {
        boolean valid = false;

        if (!file.exists()) {
            validator.add(property, new ValidatorError(property, Messages.format("dir.notfound", file.getPath())));
        } else if (!file.isDirectory()) {
            validator.add(property, new ValidatorError(property, Messages.format("dir.notdir", file.getPath())));
        } else {
            valid = true;
        }
        return valid;
    }

    /**
     * Validates the idPattern property by ensuring it compiles.
     *
     * @param validator the validator
     * @return {@code true} if the pattern is valid
     */
    private boolean validateIdPattern(Validator validator) {
        boolean result = false;
        Property property = getProperty("idPattern");
        String pattern = property.getString();

        try {
            Pattern.compile(pattern);
            result = true;
        } catch (PatternSyntaxException exception) {
            validator.add(property, new ValidatorError(property, exception.getMessage()));
        }
        return result;
    }

    /**
     * Validates the archetype property.
     *
     * @param validator the validator
     * @return {@code true} if the property is valid
     */
    private boolean validateArchetypes(Validator validator) {
        boolean result = true;
        Property property = getProperty("archetypes");
        String[] shortNames = property.getString("archetypes").split(",");
        shortNames = StringUtils.trimArrayElements(shortNames);
        for (String shortName : shortNames) {
            List<ArchetypeDescriptor> descriptors = DescriptorHelper.getArchetypeDescriptors(shortName);
            if (descriptors.isEmpty()) {
                validator.add(property, new ValidatorError(property, Messages.format("docload.archetype.notfound",
                                                                                     shortName)));
                result = false;
                break;
            }
            for (ArchetypeDescriptor descriptor : descriptors) {
                if (DOCUMENT_TEMPLATE_ACT.equals(descriptor.getShortName())
                    || !DocumentAct.class.isAssignableFrom(descriptor.getClazz())
                    || descriptor.getNodeDescriptor("document") == null) {
                    validator.add(property, new ValidatorError(property, Messages.format("docload.archetype.invalid",
                                                                                         descriptor.getShortName())));
                    result = false;
                    break;
                }
            }
        }
        return result;
    }

}
