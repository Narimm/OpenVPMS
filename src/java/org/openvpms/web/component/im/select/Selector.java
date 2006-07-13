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

import org.openvpms.web.component.button.ShortcutHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;

import org.openvpms.component.business.domain.im.common.IMObject;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.layout.RowLayoutData;


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
     * Selected object's summary.
     */
    private Label _summary;

    /**
     * Deactivated label.
     */
    private Label _deactivated;

    /**
     * Determines the layout of the 'select' button.
     */
    private ButtonStyle _buttonStyle;

    /**
     * The presentation format.
     */
    private Format _format = Format.SUMMARY;

    /**
     * The component.
     */
    private Row _component;


    /**
     * Construct a new <code>Selector</code>.
     */
    public Selector() {
        this(ButtonStyle.LEFT);
    }

    /**
     * Construct a new <code>Selector</code>.
     *
     * @param style determines the layout of the 'select' button
     */
    public Selector(ButtonStyle style) {
        _buttonStyle = style;
    }


    /**
     * Returns the selector component.
     *
     * @return the selector component
     */
    public Component getComponent() {
        if (_component == null) {
            doLayout();
        }
        return _component;
    }

    /**
     * Returns the 'select' button.
     *
     * @return the 'select' button
     */
    public Button getSelect() {
        getComponent();
        return _select;
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    public void setObject(IMObject object) {
        getComponent(); // layout component if required.
        if (object != null) {
            String value = null;
            if (_format == Format.NAME) {
                value = Messages.get("imobject.name", object.getName());
            } else if (_format == Format.DESCRIPTION) {
                value = Messages.get("imobject.description",
                                     object.getDescription());
            } else if (_format == Format.SUMMARY) {
                value = Messages.get("imobject.summary", object.getName(),
                                     object.getDescription());
            }

            _summary.setText(value);
            if (!object.isActive()) {
                _deactivated.setText(Messages.get("imobject.deactivated"));
            } else {
                _deactivated.setText(null);
            }
        } else {
            _summary.setText(null);
            _deactivated.setText(null);
        }
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
     */
    protected void doLayout() {
        _component = RowFactory.create("Selector.ControlRow");
        doLayout(_component);
    }

    /**
     * Lay out the component.
     *
     * @param container the container
     */
    protected void doLayout(Row container) {
        if (_buttonStyle == ButtonStyle.RIGHT
                || _buttonStyle == ButtonStyle.RIGHT_NO_ACCEL) {
            // button on the right. The 'wrapper' forces the summary+deactivated
            // labels to take up as much space as possible, ensuring that the
            // button is displayed hard on the right.
            // Seems more successful than using aligments
            Row wrapper = RowFactory.create(getSummary(), getDeactivated());
            RowLayoutData layout = new RowLayoutData();
            layout.setWidth(new Extent(100, Extent.PERCENT));
            wrapper.setLayoutData(layout);
            Button button = getButton();
            container.add(wrapper);
            container.add(button);
        } else {
            container.add(getSummary());
            container.add(getDeactivated());
            if (_buttonStyle == ButtonStyle.LEFT) {
                container.add(getButton(), 0);
            }
        }
    }

    /**
     * Returns the 'select' button, creating it if needed.
     *
     * @return the select button
     */
    protected Button getButton() {
        if (_select == null) {
            if (_buttonStyle == ButtonStyle.LEFT_NO_ACCEL
                    || _buttonStyle == ButtonStyle.RIGHT_NO_ACCEL) {
                String text = ShortcutHelper.getLocalisedText("button.select");
                _select = ButtonFactory.create();
                _select.setText(text);
            } else {
                _select = ButtonFactory.create("select");
            }
        }
        return _select;
    }

    /**
     * Returns the summary label, creating it if needed.
     *
     * @return the summary label
     */
    protected Label getSummary() {
        if (_summary == null) {
            _summary = LabelFactory.create();
        }
        return _summary;
    }

    /**
     * Returns the 'deactivated' label, creating it if needed.
     *
     * @return the deactivated label
     */
    protected Label getDeactivated() {
        if (_deactivated == null) {
            _deactivated = LabelFactory.create(null, "Selector.Deactivated");
        }
        return _deactivated;
    }

}
