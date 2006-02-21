package org.openvpms.web.component.im.view;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionListener;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;


/**
 * Represents a view of an {@link IMObject}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface IMObjectView {

    /**
     * Returns the object being viewed.
     *
     * @return the object being viewed
     */
    IMObject getObject();

    /**
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    Component getComponent();

    /**
     * Changes the layout.
     *
     * @param layout the new layout strategy
     */
    void setLayout(IMObjectLayoutStrategy layout);

    /**
     * Sets a listener to be notified when the layout changes.
     *
     * @param listener the listener
     */
    void setLayoutListener(ActionListener listener);
}
