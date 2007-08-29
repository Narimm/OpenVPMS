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

import echopointng.KeyStrokeListener;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.layout.RowLayoutData;
import org.apache.commons.lang.ClassUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.button.KeyStrokeHandler;
import org.openvpms.web.component.button.ShortcutButton;
import org.openvpms.web.component.focus.FocusGroup;
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
public abstract class Selector<T extends IMObject> {

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
    private Button select;

    /**
     * Selected object's label. Null if the selector is editable.
     */
    private Label objectLabel;

    /**
     * Selected object's text. Null if the selector is not editable.
     */
    private TextField objectText;

    /**
     * Determines the layout of the 'select' button.
     */
    private final ButtonStyle buttonStyle;

    /**
     * Determines if the selector may be edited.
     */
    private final boolean editable;

    /**
     * The presentation format.
     */
    private Format format = Format.SUMMARY;

    /**
     * The component.
     */
    private Component component;

    /**
     * The focus group.
     */
    private final FocusGroup focusGroup;

    /**
     * The keystroke listener.
     */
    private KeyStrokeListener listener;


    /**
     * Construct a new <tt>Selector</tt>.
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
        buttonStyle = style;
        this.editable = editable;
        component = new SelectorRow();
        focusGroup = new FocusGroup(ClassUtils.getShortClassName(getClass()));
        if (style != ButtonStyle.LEFT_NO_ACCEL
                && style != ButtonStyle.RIGHT_NO_ACCEL) {
            listener = new KeyStrokeListener();
            listener.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    onSelected(event);
                }
            });
        }
    }

    /**
     * Returns the selector component.
     *
     * @return the selector component
     */
    public Component getComponent() {
        if (component.getComponentCount() == 0) {
            if (listener != null) {
                component.add(listener);
            }
            Component layout = doLayout(null, null);
            component.add(layout);
        }
        return component;
    }

    /**
     * Returns the focus group.
     *
     * @return the focus group
     */
    public FocusGroup getFocusGroup() {
        return focusGroup;
    }

    /**
     * Returns the 'select' button.
     *
     * @return the 'select' button
     */
    public Button getSelect() {
        if (select == null) {
            if (buttonStyle == ButtonStyle.LEFT_NO_ACCEL
                    || buttonStyle == ButtonStyle.RIGHT_NO_ACCEL) {
                select = ButtonFactory.create(null, "select");
            } else {
                select = ButtonFactory.create("select");
                if (select instanceof ShortcutButton) {
                    ShortcutButton button = (ShortcutButton) select;
                    listener.addKeyCombination(button.getKeyCode());
                }
            }
        }
        return select;
    }

    /**
     * Returns the editable text field.
     *
     * @return the editable text field, or <tt>null</tt> if this is not an
     *         editable selector
     */
    public TextField getText() {
        getObjectComponent();
        return objectText;
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    public void setObject(T object) {
        if (object != null) {
            setObject(object.getName(), object.getDescription(),
                      object.isActive());
        } else {
            setObject(null, null, true);
        }
    }

    /**
     * Sets the presentation format.
     *
     * @param format the presentation format
     */
    public void setFormat(Format format) {
        this.format = format;
    }

    /**
     * Sets the current object details.
     *
     * @param name        the object name. May be <tt>null</tt>
     * @param description the object description. May be <tt>null</tt>
     * @param active      determines if the object is active
     */
    protected void setObject(String name, String description, boolean active) {
        String text = null;
        String deactivated = null;
        if (name != null || description != null) {
            if (format == Format.NAME) {
                text = Messages.get("imobject.name", name);
            } else if (format == Format.DESCRIPTION) {
                text = Messages.get("imobject.description", description);
            } else if (format == Format.SUMMARY) {
                text = Messages.get("imobject.summary", name, description);
            }

            if (!active) {
                deactivated = Messages.get("imobject.deactivated");
            }
        }
        component.removeAll();
        Component layout = doLayout(text, deactivated);
        if (listener != null) {
            component.add(listener);
        }
        component.add(layout);
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
        if (buttonStyle == ButtonStyle.RIGHT) {
            // button on the right. The 'wrapper' forces the summary+deactivated
            // labels to take up as much space as possible, ensuring that the
            // button is displayed hard on the right.
            // Seems more successful than using alignments
            Row wrapper = RowFactory.create("CellSpacing",
                                            getObjectComponent());
            if (deactivated != null) {
                addDeactivated(wrapper, deactivated);
            }
            RowLayoutData layout = new RowLayoutData();
            layout.setWidth(new Extent(100, Extent.PERCENT));
            wrapper.setLayoutData(layout);
            Button button = getSelect();
            component = RowFactory.create(wrapper, button);
        } else if (buttonStyle == ButtonStyle.RIGHT_NO_ACCEL) {
            Row wrapper = RowFactory.create(getObjectComponent(), getSelect());
            if (deactivated != null) {
                component = RowFactory.create("CellSpacing", wrapper);
                addDeactivated(component, deactivated);
            } else {
                component = wrapper;
            }
        } else {
            component = RowFactory.create("CellSpacing", getObjectComponent());
            if (deactivated != null) {
                addDeactivated(component, deactivated);
            }
            if (buttonStyle == ButtonStyle.LEFT
                    || buttonStyle == ButtonStyle.LEFT_NO_ACCEL) {
                component.add(getSelect(), 0);
            }
        }
        if (objectText != null) {
            objectText.setText(text);
            focusGroup.add(objectText);
            if (buttonStyle == ButtonStyle.LEFT
                    || buttonStyle == ButtonStyle.LEFT_NO_ACCEL) {
                focusGroup.add(0, getSelect());
            } else if (buttonStyle == ButtonStyle.RIGHT
                    || buttonStyle == ButtonStyle.RIGHT_NO_ACCEL) {
                focusGroup.add(getSelect());
            }
        } else {
            objectLabel.setText(text);
        }
        return component;
    }

    /**
     * Returns the object component, creating it if needed.
     *
     * @return the object component
     */
    protected Component getObjectComponent() {
        Component component;
        if (editable) {
            if (objectText == null) {
                objectText = TextComponentFactory.create();
            }
            component = objectText;
        } else {
            if (objectLabel == null) {
                objectLabel = LabelFactory.create();
            }
            component = objectLabel;
        }
        return component;
    }

    private void addDeactivated(Component container, String deactivated) {
        Label label = LabelFactory.create(null, "Selector.Deactivated");
        label.setText(deactivated);
        container.add(label);
    }


    private void onSelected(ActionEvent event) {
        getSelect().fireActionPerformed(event);
    }

    private class SelectorRow extends Row implements KeyStrokeHandler {

        /**
         * Re-registers keystroke listeners.
         */
        public void reregisterKeyStrokeListeners() {
            if (listener != null && select instanceof ShortcutButton) {
                ShortcutButton button = (ShortcutButton) select;
                int keyCode = button.getKeyCode();
                if (keyCode != -1) {
                    listener.removeKeyCombination(keyCode);
                    listener.addKeyCombination(keyCode);
                }
            }
        }
    }

}
