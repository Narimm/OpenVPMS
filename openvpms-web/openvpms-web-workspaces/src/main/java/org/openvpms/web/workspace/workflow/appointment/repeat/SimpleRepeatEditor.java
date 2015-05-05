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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.appointment.repeat;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.apache.commons.lang.WordUtils;
import org.openvpms.web.echo.factory.LabelFactory;

/**
 * An {@link RepeatExpressionEditor} that simply displays the expression type.
 *
 * @author Tim Anderson
 */
class SimpleRepeatEditor implements RepeatExpressionEditor {

    /**
     * The expression.
     */
    private final RepeatExpression expression;

    /**
     * Constructs an {@link SimpleRepeatEditor}.
     *
     * @param expression the expression
     */
    public SimpleRepeatEditor(RepeatExpression expression) {
        this.expression = expression;
    }

    /**
     * Returns the expression.
     *
     * @return the expression
     */
    @Override
    public RepeatExpression getExpression() {
        return expression;
    }

    /**
     * Returns the component representing the editor.
     *
     * @return the component
     */
    @Override
    public Component getComponent() {
        Label result = LabelFactory.create();
        result.setText(WordUtils.capitalizeFully(expression.getType().name()));
        return result;
    }

    /**
     * Determines if the editor is valid.
     *
     * @return {@code true} if the editor is valid
     */
    @Override
    public boolean isValid() {
        return true;
    }
}
