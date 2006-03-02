package org.openvpms.web.component.im.view;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.DefaultLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategyFactory;
import org.openvpms.web.component.im.view.act.ActLayoutStrategy;


/**
 * Default implementation of the {@link IMObjectLayoutStrategyFactory}
 * interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DefaultLayoutStrategyFactory
        implements IMObjectLayoutStrategyFactory {

    /**
     * Creates a new layout strategy for an object.
     *
     * @param object  the object to create the layout strategy for
     * @param showAll if <code>true</code>, show all non-hidden fields;
     *                otherwise show required fields.
     * @return a new layout strategy
     */
    public IMObjectLayoutStrategy create(IMObject object, boolean showAll) {
        IMObjectLayoutStrategy result = null;
        if (object instanceof Act) {
            result = new ActLayoutStrategy(showAll);
        } else {
            result = new DefaultLayoutStrategy(showAll);
        }
        return result;
    }
}
