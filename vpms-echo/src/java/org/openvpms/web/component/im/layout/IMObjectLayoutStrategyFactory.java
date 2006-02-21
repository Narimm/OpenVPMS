package org.openvpms.web.component.im.layout;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Factory for {@link IMObjectLayoutStrategy} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface IMObjectLayoutStrategyFactory {

    /**
     * Creates a new layout strategy for an object.
     *
     * @param object  the object to create the layout strategy for
     * @param showAll if <code>true</code>, show all non-hidden fields;
     *                otherwise show required fields.
     * @return a new layout strategy
     */
    IMObjectLayoutStrategy create(IMObject object, boolean showAll);
}
