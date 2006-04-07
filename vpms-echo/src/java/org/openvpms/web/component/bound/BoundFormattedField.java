package org.openvpms.web.component.bound;

import java.text.Format;
import java.text.ParseException;

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.text.TextComponent;

import org.openvpms.web.component.edit.Property;


/**
 * Binds a {@link Property} to a <code>TextField</code>, providing formatting.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class BoundFormattedField extends TextField {

    /**
     * The formatter.
     */
    private final Format _format;


    /**
     * Construct a new <code>BoundFormattedField</code>.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display
     * @param format   the formatter
     */
    public BoundFormattedField(Property property, int columns,
                               Format format) {
        setWidth(new Extent(columns, Extent.EX));
        _format = format;
        Binder binder = new FormattingBinder(this, property);
        binder.setField();
    }

    private class FormattingBinder extends TextComponentBinder {

        /**
         * Construct a new <code>FormattingtBinder</code>.
         *
         * @param component the component to bind
         * @param property  the property to bind
         */
        public FormattingBinder(TextComponent component, Property property) {
            super(component, property);
        }

        /**
         * Returns the value of the field.
         *
         * @return the value of the field
         */
        @Override
        protected Object getFieldValue() {
            Object result = null;
            String value = (String) super.getFieldValue();
            if (value != null) {
                try {
                    result = _format.parseObject(value);
                } catch (ParseException exception) {
                    // failed to parse, so return the field unchanged
                    result = value;
                }
            }
            return result;
        }

        /**
         * Sets the value of the field.
         *
         * @param value the value to set
         */
        @Override
        protected void setFieldValue(Object value) {
            if (value != null) {
                try {
                    value = _format.format(value);
                } catch (IllegalArgumentException ignore) {
                    // failed to format, so set the field unchanged
                }
            }
            super.setFieldValue(value);
        }
    }

}
