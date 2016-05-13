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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.template;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;

/**
 * An editor for <em>entity.documentTemplateSMSReminder</em>.
 *
 * @author Tim Anderson
 */
public class SMSReminderTemplateEditor extends SMSTemplateEditor {

    /**
     * Constructs an {@link SMSReminderTemplateEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public SMSReminderTemplateEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        evaluate();
    }

    /**
     * Creates a new sampler.
     *
     * @param template      the template
     * @param layoutContext the sampler
     * @return a new sampler
     */
    @Override
    protected SMSTemplateSampler createSampler(Entity template, LayoutContext layoutContext) {
        return new SMSReminderTemplateSampler(template, getLayoutContext());
    }

}
