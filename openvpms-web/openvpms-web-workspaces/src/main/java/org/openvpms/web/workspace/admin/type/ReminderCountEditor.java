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

package org.openvpms.web.workspace.admin.type;

import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.reminder.ReminderRule;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.EditableIMObjectCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

/**
 * An editor for <em>entity.reminderCount</em>.
 * <p>
 * This ensures that the selected document template has the required documents for each rule.
 *
 * @author Tim Anderson
 */
public class ReminderCountEditor extends AbstractIMObjectEditor {

    /**
     * Constructs a {@link ReminderCountEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public ReminderCountEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
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
        return super.doValidation(validator) && checkRules(validator);
    }

    /**
     * Verifies that there a template for each rule that requires one.
     * <p>
     * Note that if 'contact' is selected, the template must be present and have email, SMS templates linked.
     *
     * @param validator the validator
     * @return {@code true} if the rules are valid, otherwise {@code false}
     */
    private boolean checkRules(Validator validator) {
        boolean valid = true;
        EditableIMObjectCollectionEditor rules = (EditableIMObjectCollectionEditor) getEditor("rules");
        DocumentTemplate template = getTemplate();

        if (rules != null) {
            IArchetypeRuleService service = ServiceHelper.getArchetypeService();
            for (IMObject object : rules.getCurrentObjects()) {
                ReminderRule rule = new ReminderRule(object, service);
                if (rule.canPrint() && template == null) {
                    addTemplateRequired(validator);
                    valid = false;
                    break;
                } else if (rule.canEmail() && (template == null || template.getEmailTemplate() == null)) {
                    if (template == null) {
                        addTemplateRequired(validator);
                    } else {
                        addTemplateRequired(template, "reminder.count.email.required", validator);
                    }
                    valid = false;
                    break;
                } else if (rule.canSMS() && (template == null || template.getSMSTemplate() == null)) {
                    if (template == null) {
                        addTemplateRequired(validator);
                    } else {
                        addTemplateRequired(template, "reminder.count.sms.required", validator);
                    }
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    /**
     * Adds a validation error indicating the template is required.
     *
     * @param validator the validator
     */
    private void addTemplateRequired(Validator validator) {
        Property property = getProperty("template");
        reportRequired(property, validator);
    }

    /**
     * Adds a validation error indicating that an email or SMS template is required.
     *
     * @param template  the parent template
     * @param key       the message key
     * @param validator the validator
     */
    private void addTemplateRequired(DocumentTemplate template, String key, Validator validator) {
        Property property = getProperty("template");
        String message = Messages.format(key, template.getName());
        validator.add(property, new ValidatorError(property, message));
    }

    /**
     * Returns the document template.
     * TODO - this could be cached, although it would prevent users editing the template in another window without
     * doing version checking.
     *
     * @return the document template, or {@code null} if none is selected
     */
    private DocumentTemplate getTemplate() {
        IMObjectBean bean = new IMObjectBean(getObject());
        Entity template = (Entity) bean.getNodeTargetObject("template");
        return (template != null) ? new DocumentTemplate(template, ServiceHelper.getArchetypeService()) : null;
    }
}
