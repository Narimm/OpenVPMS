package org.openvpms.web.component;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.util.Messages;


/**
 * Factory for {@link Button}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public final class ButtonFactory extends ComponentFactory {

    /**
     * Component type.
     */
    private static final String TYPE = "button";


    /**
     * Create a new button.
     *
     * @param key the resource bundle key
     * @return a new button
     */
    public static Button create(String key) {
        Button button = new Button(getString(TYPE, key));
        setDefaults(button);
        return button;
    }

    /**
     * Create a new button.
     *
     * @param key      the resource bundle key
     * @param listener the listener
     * @return a new button
     */
    public static Button create(String key, ActionListener listener) {
        Button button = create(key);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Helper to return localised text for a button.
     *
     * @param id the component identifier
     * @return the localised string corresponding to <code>id</code>
     */
    public static String getString(String id) {
        return Messages.getString(TYPE + "." + id);
    }

}
