package org.openvpms.web.component.im.view;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.ExpandableLayoutStrategy;
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
     * Determines if expandable layouts can be changed.
     */
    private final boolean _toggleLayout;


    /**
     * Construct a new <code>DefaultLayoutStrategy</code>
     *
     * @param toggleLayout if <code>true</code> enable toggling of layouts
     */
    public DefaultLayoutStrategyFactory(boolean toggleLayout) {
        _toggleLayout = toggleLayout;
    }

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
            result = new ActLayoutStrategy(showAll, _toggleLayout);
        } else {
            result = new ExpandableLayoutStrategy(showAll, _toggleLayout);

        }
        return result;
    }
}
