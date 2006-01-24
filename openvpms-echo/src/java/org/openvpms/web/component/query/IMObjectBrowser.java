package org.openvpms.web.component.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.DefaultLayoutStrategy;
import org.openvpms.web.component.im.IMObjectComponentFactory;
import org.openvpms.web.component.im.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.IMObjectViewer;


/**
 * {@link IMObject} browser.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectBrowser extends IMObjectViewer {


    /**
     * Construct a new <code>IMObjectBrowser</code>.
     *
     * @param object the object to browse.
     */
    public IMObjectBrowser(IMObject object) {
        this(object, new DefaultLayoutStrategy());
    }

    /**
     * Construct a new <code>IMObjectBrowser</code>.
     *
     * @param object the object to browse.
     * @param layout the layout strategy
     */
    public IMObjectBrowser(IMObject object, IMObjectLayoutStrategy layout) {
        super(object, layout);
    }

    /**
     * Returns the factory for creating components for displaying the object.
     *
     * @return the component factory
     */
    protected IMObjectComponentFactory getComponentFactory() {
        return new NodeBrowserFactory();
    }
}
