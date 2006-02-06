package org.openvpms.web.component.im.layout;

import org.openvpms.web.component.im.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.filter.BasicNodeFilter;


/**
 * Default implementation of the {@link IMObjectLayoutStrategy} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DefaultLayoutStrategy extends AbstractLayoutStrategy {

    /**
     * Construct a new <code>DefaultLayoutStrategy</code>, showing all
     * non-hidden fields.
     */
    public DefaultLayoutStrategy() {
        this(true);
    }

    /**
     * Construct a new <code>DefaultLayoutStrategy</code>.
     *
     * @param showAll if <code>true</code>, show all non-hidden fields;
     *                otherwise show required fields.
     */
    public DefaultLayoutStrategy(boolean showAll) {
        super(new BasicNodeFilter(showAll, false));
    }
}
