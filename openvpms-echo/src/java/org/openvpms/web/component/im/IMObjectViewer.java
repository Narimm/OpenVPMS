package org.openvpms.web.component.im;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;

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
     * The layout strafegy.
     */
    private IMObjectLayoutStrategy _layout;

    /**
     * The component produced by the renderer.
     */
    private Component _component;

    /**
     * Invoked when the layout changes.
     */
    private ActionListener _layoutListener;


    /**
     * Construct a new <code>IMObjectViewer</code>.
     *
     * @param object the object to display
     */
    public IMObjectViewer(IMObject object) {
        this(object, null);
    }

    /**
     * Construct a new <code>IMObjectViewer</code>.
     *
     * @param object the object to display
     * @param layout the layout strategy. May be <code>null</code>
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
     * Sets a listener to be notified when the layout changes.
     *
     * @param listener the listener
     */
    public void setLayoutListener(ActionListener listener) {
        _layoutListener = listener;
    }

    /**
     * Returns the factory for creating components for displaying the object.
     *
     * @return the component factory
     */
    protected abstract IMObjectComponentFactory getComponentFactory();

    /**
     * Changes the layout.
     *
     * @param layout the new layout strategy
     */
    protected void setLayout(IMObjectLayoutStrategy layout) {
        _component = null;
        _layout = layout;
        getComponent();
        if (_layoutListener != null) {
            _layoutListener.actionPerformed(new ActionEvent(this, null));
        }
    }

    /**
     * Returns the current layout.
     *
     * @return the layout. May be <code>null</code>
     */
    protected IMObjectLayoutStrategy getLayout() {
        return _layout;
    }

}
