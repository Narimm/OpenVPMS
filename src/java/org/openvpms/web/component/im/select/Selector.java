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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.select;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.TextComponentFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Component that provides a 'select' button and object summary.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Selector {

    /**
     * Determines the layout of the 'select' button.
     */
    public enum ButtonStyle {
        LEFT, RIGHT, HIDE, LEFT_NO_ACCEL, RIGHT_NO_ACCEL}

    /**
     * Determines how the object is displayed.
     */
    public enum Format {
        NAME, DESCRIPTION, SUMMARY}

    /**
     * The 'select' button.
     */
    private Button _select;

    /**
     * Selected object's label. Null if the selector is editable.
     */
    private Label _objectLabel;

    /**
     * Selected object's text. Null if the selector is not editable.
     */
    private TextField _objectText;

    /**
     * Determines the layout of the 'select' button.
     */
    private final ButtonStyle _buttonStyle;

    /**
     * Determines if the selector may be edited.
     */
    private final boolean _editable;

    /**
     * The presentation format.
     */
    private Format _format = Format.SUMMARY;

    /**
     * The component.
     */
    private Component _component;


    /**
     * Construct a new <code>Selector</code>.
     */
    public Selector() {
        this(ButtonStyle.LEFT, false);
    }

    /**
     * Construct a new <code>Selector</code>.
     *
     * @param style    determines the layout of the 'select' button
     * @param editable determines if the selector is editable
     */
    public Selector(ButtonStyle style, boolean editable) {
        _buttonStyle = style;
        _editable = editable;
        _component = RowFactory.create();
    }

    /**
     * Returns the selector component.
     *
     * @return the selector component
     */
    public Component getComponent() {
        if (_component.getComponentCount() == 0) {
            Component layout = doLayout(null, null);
            _component.add(layout);
        }
        return _component;
    }

    /**
     * Returns the 'select' button.
     *
     * @return the 'select' button
     */
    public Button getSelect() {
        if (_select == null) {
            if (_buttonStyle == ButtonStyle.LEFT_NO_ACCEL
                    || _buttonStyle == ButtonStyle.RIGHT_NO_ACCEL) {
                _select = ButtonFactory.create(null, "select");
            } else {
                _select = ButtonFactory.create("select");
            }
        }
        return _select;
    }

    /**
     * Returns the editable text field.
     *
     * @return the editable text field. Null if this is not an editable selector
     */
    public TextField getText() {
        getObject();
        return _objectText;
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(IMObject object) {
        String text = null;
        String deactivated = null;
        if (object != null) {
            if (_format == Format.NAME) {
                text = Messages.get("imobject.name", object.getName());
            } else if (_format == Format.DESCRIPTION) {
                text = Messages.get("imobject.description",
                                    object.getDescription());
            } else if (_format == Format.SUMMARY) {
                text = Messages.get("imobject.summary", object.getName(),
                                    object.getDescription());
            }

            if (!object.isActive()) {
                deactivated = Messages.get("imobject.deactivated");
            }
        }
        _component.removeAll();
        Component layout = doLayout(text, deactivated);
        _component.add(layout);
    }

    /**
     * Sets the presentation format.
     *
     * @param format the presentation format
     */
    public void setFormat(Format format) {
        _format = format;
    }

    /**
     * Create the component.
     *
     * @param text        the object text. May be <code>null</code>
     * @param deactivated the deactivated text. May be <code>null</code>
     * @return the component
     */
    protected Component doLayout(String text, String deactivated) {
        Component component;
        if (_buttonStyle == ButtonStyle.RIGHT) {
            // button on the right. The 'wrapper' forces the summary+deactivated
            // labels to take up as much space as possible, ensuring that the
            // button is displayed hard on the right.
            // Seems more successful than using alignments
            Row wrapper = RowFactory.create("CellSpacing", getObject());
            if (deactivated != null) {
                addDeactivated(wrapper, deactivated);
            }
            RowLayoutData layout = new RowLayoutData();
            layout.setWidth(new Extent(100, Extent.PERCENT));
            wrapper.setLayoutData(layout);
            Button button = getSelect();
            component = RowFactory.create(wrapper, button);
        } else if (_buttonStyle == ButtonStyle.RIGHT_NO_ACCEL) {
            Row wrapper = RowFactory.create(getObject(), getSelect());
            if (deactivated != null) {
                component = RowFactory.create("CellSpacing", wrapper);
                addDeactivated(component, deactivated);
            } else {
                component = wrapper;
            }
        } else {
            component = RowFactory.create("CellSpacing", getObject());
            if (deactivated != null) {
                addDeactivated(component, deactivated);
            }
            if (_buttonStyle == ButtonStyle.LEFT
                    || _buttonStyle == ButtonStyle.LEFT_NO_ACCEL) {
                component.add(getSelect(), 0);
            }
        }
        if (_objectText != null) {
            _objectText.setText(text);
        } else {
            _objectLabel.setText(text);
        }
        return component;
    }

    /**
     * Returns the object component, creating it if needed.
     *
     * @return the object component
     */
    protected Component getObject() {
        Component component;
        if (_editable) {
            if (_objectText == null) {
                _objectText = TextComponentFactory.create();
            }
            component = _objectText;
        } else {
            if (_objectLabel == null) {
                _objectLabel = LabelFactory.create();
            }
            component = _objectLabel;
        }
        return component;
    }

    private void addDeactivated(Component container, String deactivated) {
        Label label = LabelFactory.create(null, "Selector.Deactivated");
        label.setText(deactivated);
        container.add(label);
    }

}
