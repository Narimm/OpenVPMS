package org.openvpms.web.component.bound;

import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.text.TextComponent;

import org.openvpms.web.component.edit.Property;


/**
 * Helper to bind a property to a text component..
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
class TextComponentBinder extends Binder {

    /**
     * The text component to bind to.
     */
    private final TextComponent _component;


    /**
     * Construct a new <code>TextComponentBinder</code>.
     *
     * @param component the component to bind
     * @param property  the property to bind
     */
    public TextComponentBinder(TextComponent component, Property property) {
        super(property);
        _component = component;
        setField();

        _component.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setProperty();
            }
        });
    }

    /**
     * Returns the value of the field.
     *
     * @return the value of the field
     */
    protected Object getFieldValue() {
        return _component.getText();
    }

    /**
     * Sets the value of the field.
     *
     * @param value the value to set
     */
    protected void setFieldValue(Object value) {
        String text = (value != null) ? value.toString() : null;
        _component.setText(text);
    }
}
