package org.openvpms.web.component.im;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * {@link IMObject} viewer.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class IMObjectViewer {

    /**
     * The object to display.
     */
    private final IMObject _object;

    /**
     * The renderer.
     */
    private final IMObjectLayoutStrategy _layout;

    /**
     * The component produced by the renderer.
     */
    private Component _component;


    /**
     * Construct a new <code>IMObjectViewer</code>.
     *
     * @param object the object to display
     * @param layout the layout strategy
     */
    public IMObjectViewer(IMObject object, IMObjectLayoutStrategy layout) {
        _object = object;
        _layout = layout;
    }

    /**
     * Returns the object being viewed.
     *
     * @return the object being viewed
     */
    public IMObject getObject() {
        return _object;
    }

    /**
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    public Component getComponent() {
        if (_component == null) {
            _component = _layout.apply(_object, getComponentFactory());
        }
        return _component;
    }

    /**
     * Returns the factory for creating components for displaying the object.
     *
     * @return the component factory
     */
    protected abstract IMObjectComponentFactory getComponentFactory();

}
