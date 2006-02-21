package org.openvpms.web.component.util;

import echopointng.ButtonEx;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.web.resource.util.Messages;


/**
 * Factory for {@link Button}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public final class ButtonFactory extends ComponentFactory {

    /**
     * Component type.
     */
    private static final String TYPE = "button";


    /**
     * Create a new button with the default style.
     *
     * @return a new button
     */
    public static Button create() {
        Button button = new Button();
        setDefaults(button);
        return button;
    }

    /**
     * Create a new button with a localised text message, and default style.
     *
     * @param key the resource bundle key. May be <code>null</code>
     * @return a new button
     */
    public static Button create(String key) {
        Button button;
        if (key != null) {
            String accellerator = getString(TYPE, key + ".key", true);
            String text = getString(TYPE, key, false);
            if (accellerator != null) {
                ButtonEx ext = new ButtonEx(text);
                ext.setAccessKey(accellerator);
                button = ext;
            } else {
                button = new Button(getString(TYPE, key, false));
            }

            setDefaults(button);
        } else {
            button = create();
        }
        return button;
    }

    /**
     * Create a new button with a localised text message, default style, and
     * listener.
     *
     * @param key      the resource bundle key. May be <code>null</code>
     * @param listener the listener
     * @return a new button
     */
    public static Button create(String key, ActionListener listener) {
        Button button = create(key);
        button.addActionListener(listener);
        return button;
    }

    /**
     * Create a new button with a localised text message, specific style, and
     * listener.
     *
     * @param key      the resource bundle key. May be <code>null</code>
     * @param style    the style name
     * @param listener the listener
     * @return a new button
     */
    public static Button create(String key, String style,
                                ActionListener listener) {
        Button button = create(key);
        button.setStyleName(style);
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
        return Messages.get(TYPE + "." + id);
    }

}
