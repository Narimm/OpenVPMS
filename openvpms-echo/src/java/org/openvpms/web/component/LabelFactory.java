package org.openvpms.web.component;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.ImageReference;
import org.openvpms.web.component.ComponentFactory;


/**
 * Factory for {@link Label}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public final class LabelFactory extends ComponentFactory {

    /**
     * Component type.
     */
    private static final String TYPE = "label";

    /**
     * Create a new label.
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
     * Create a new label with localised text.
     *
     * @param key the resource bundle key
     * @return a new label
     */
    public static Label create(String key) {
        Label label = create();
        label.setText(getString(TYPE, key));
        return label;
    }

}
