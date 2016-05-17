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

import echopointng.Separator;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Insets;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;

/**
 * An editor for <em>entity.documentTemplateSMS*</em> entities.
 *
 * @author Tim Anderson
 */
public abstract class SMSTemplateEditor extends AbstractIMObjectEditor {

    /**
     * The template sampler, used to test the template.
     */
    private final SMSTemplateSampler sampler;

    /**
     * Constructs an {@link AbstractIMObjectEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public SMSTemplateEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);

        sampler = createSampler(object, getLayoutContext());

        ModifiableListener listener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                evaluate();
            }
        };
        getProperty("content").addModifiableListener(listener);
        getProperty("contentType").addModifiableListener(listener);
    }

    /**
     * Creates a new sampler.
     *
     * @param template      the template
     * @param layoutContext the sampler
     * @return a new sampler
     */
    protected abstract SMSTemplateSampler createSampler(Entity template, LayoutContext layoutContext);

    /**
     * Evaluates the sampler.
     */
    protected void evaluate() {
        sampler.evaluate();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new LayoutStrategy();
    }


    private class LayoutStrategy extends SMSTemplateLayoutStrategy {

        /**
         * Lay out out the object.
         *
         * @param object     the object to lay out
         * @param properties the object's properties
         * @param parent     the parent object. May be {@code null}
         * @param context    the layout context
         * @return the component
         */
        @Override
        protected Component doLayout(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
            Column container = ColumnFactory.create(Styles.CELL_SPACING);
            doLayout(object, properties, parent, container, context);
            Separator separator = new Separator();
            separator.setInsets(new Insets(0));
            getFocusGroup().add(sampler.getFocusGroup());
            container.add(separator);
            Label label = LabelFactory.create("sms.sample.title", Styles.BOLD);
            label.setLayoutData(ColumnFactory.layout(Alignment.ALIGN_CENTER));
            container.add(label);
            container.add(sampler.getComponent());
            return container;
        }
    }

}
