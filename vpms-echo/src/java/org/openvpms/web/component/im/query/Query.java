package org.openvpms.web.component.im.query;

import java.util.List;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Query facility for {@link IMObject} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface Query {

    /**
     * Returns the query component.
     *
     * @return the query component
     */
    Component getComponent();

    /**
     * Performs the query, returning the matching objects.
     *
     * @return the matching objects
     */
    List<IMObject> query();

}
