package org.openvpms.web.component.bound;

import nextapp.echo2.app.TextField;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.event.ActionListener;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.DocumentListener;
import nextapp.echo2.app.event.DocumentEvent;
import org.apache.commons.jxpath.Pointer;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class BoundTextArea extends TextArea {

    /**
     * The bound field.
     */
    private final Pointer _pointer;


    /**
     * Construct a new <code>BoundTextField</code>.
     *
     * @param pointer the field to bind
     */
    public BoundTextArea(Pointer pointer) {
        _pointer = pointer;
        setText(getValue());

        getDocument().addDocumentListener(new DocumentListener() {
            public void documentUpdate(DocumentEvent event) {
                setValue(getText());
            }
        });
    }

    protected String getValue() {
        Object value = _pointer.getValue();
        return (value != null) ? value.toString() : null;
    }

    protected void setValue(String text) {
        _pointer.setValue(getText());
    }

}
