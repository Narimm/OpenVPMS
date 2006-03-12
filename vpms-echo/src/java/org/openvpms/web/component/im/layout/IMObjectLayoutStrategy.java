package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Strategy for laying out an {@link IMObject} in a <code>Component</code>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface IMObjectLayoutStrategy {

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object  the object to apply
     * @param context the layout context
     * @return the component containing the rendered <code>object</code>
     */
    Component apply(IMObject object, LayoutContext context);
}
