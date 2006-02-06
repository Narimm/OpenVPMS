package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Query implementation that queries {@link IMObject} instances on short name,
 * instance name, and active/inactive status.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DefaultQuery extends AbstractQuery {

    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified short names.
     *
     * @param shortNames the short names
     */
    public DefaultQuery(String[] shortNames) {
        super(shortNames);
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public DefaultQuery(String refModelName, String entityName,
                        String conceptName) {
        super(refModelName, entityName, conceptName);
    }

}
