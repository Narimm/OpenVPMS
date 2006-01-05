package org.openvpms.web.component;

import nextapp.echo2.app.TextField;
import nextapp.echo2.app.PasswordField;
import nextapp.echo2.app.TextArea;
import nextapp.echo2.app.text.TextComponent;

import org.openvpms.web.component.ComponentFactory;
import org.openvpms.web.component.Styles;
import org.openvpms.web.component.bound.BoundTextArea;
import org.openvpms.web.component.bound.BoundTextField;

import org.apache.commons.jxpath.Pointer;


/**
 * Factory for {@link TextComponent}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public class TextComponentFactory extends ComponentFactory {

    /**
     * Create a new text field.
     *
     * @return a new text field
     */
    public static TextField create() {
        TextField text = new TextField();
        text.setStyleName(Styles.DEFAULT);
        return text;
    }

    /**
     * Create a new bound text field.
     *
     * @param pointer a pointer to the field to update
     * @return a new bound text field
     */
    public static TextField create(Pointer pointer) {
        TextField text = new BoundTextField(pointer);
        text.setStyleName(Styles.DEFAULT);
        return text;
    }

    /**
     * Create a new text area.
     *
     * @return a new text area
     */
    public static TextArea createTextArea() {
        TextArea text = new TextArea();
        text.setStyleName(Styles.DEFAULT);
        return text;
    }

    /**
     * Create a new bound text area.
     *
     * @param pointer a pointer to the field to update
     * @return a new bound text field
     */
    public static TextArea createTextArea(Pointer pointer) {
        TextArea text = new BoundTextArea(pointer);
        text.setStyleName(Styles.DEFAULT);
        return text;
    }

    /**
     * Create a new password field.
     *
     * @return a new password field
     */
    public static TextField createPassword() {
        TextField password = new PasswordField();
        setDefaults(password);
        return password;
    }
}
