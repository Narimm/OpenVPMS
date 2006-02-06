package org.openvpms.web.component;

import nextapp.echo2.app.ImageReference;
import nextapp.echo2.app.Label;


/**
 * Factory for {@link Label}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public final class LabelFactory extends ComponentFactory {

    /**
     * Component type.
     */
    private static final String TYPE = "label";


    /**
     * Create a new label, with the default style.
     *
     * @return a new label
     */
    public static Label create() {
        Label label = new Label();
        setDefaults(label);
        return label;
    }

    /**
     * Create a new label with an image.
     *
     * @param image the image
     * @return a new label.
     */
    public static Label create(ImageReference image) {
        Label label = create();
        label.setIcon(image);
        return label;
    }

    /**
     * Create a new label with localised text, and default style.
     *
     * @param key the resource bundle key. May be <code>null</code>
     * @return a new label
     */
    public static Label create(String key) {
        Label label = create();
        if (key != null) {
            label.setText(getString(TYPE, key, false));
        }
        return label;
    }

    /**
     * Create a new label with localised text, and specific style.
     *
     * @param key the resource bundle key. May be <code>null</code>
     * @return a new label
     */
    public static Label create(String key, String style) {
        Label label = create(key);
        label.setStyleName(style);
        return label;
    }

}
