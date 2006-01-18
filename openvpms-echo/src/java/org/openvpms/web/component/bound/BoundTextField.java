package org.openvpms.web.component.bound;

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.TextField;
import nextapp.echo2.app.event.DocumentEvent;
import nextapp.echo2.app.event.DocumentListener;
import org.apache.commons.jxpath.Pointer;


/**
 * Bounds a text field to a {@link Pointer}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class BoundTextField extends TextField {

    /**
     * The bound field.
     */
    private final Pointer _pointer;


    /**
     * Construct a new <code>BoundTextField</code>.
     *
     * @param pointer the field to bind
     * @param columns the no. of columns to display
     */
    public BoundTextField(Pointer pointer, int columns) {
        _pointer = pointer;

        setText(getValue());
        setWidth(new Extent(columns, Extent.EX));

        getDocument().addDocumentListener(new DocumentListener() {
            public void documentUpdate(DocumentEvent event) {
                setValue(getText());
            }
        });
    }

    /**
     * Returns the stringified value of the pointer.
     *
     * @return the value of the pointer, or <code>null</code> if it has no
     *         value
     */
    protected String getValue() {
        Object value = _pointer.getValue();
        return (value != null) ? value.toString() : null;
    }

    /**
     * Sets the value of the pointer.
     *
     * @param text the value to set
     */
    protected void setValue(String text) {
        _pointer.setValue(text);
    }

}
