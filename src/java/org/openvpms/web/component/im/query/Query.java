package org.openvpms.web.component.im.query;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.IConstraint;


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
     * Performs the query.
     *
     * @param rows      the maxiomum no. of rows per page
     * @param node      the node to sort on. May be <code>null</code>
     * @param ascending if <code>true</code> sort the rows in ascending order;
     *                  otherwise sort them in <code>descebding</code> order
     * @return the query result set
     */
    ResultSet query(int rows, String node, boolean ascending);

    /**
     * Determines if the query should be run automatically.
     *
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwie <code>false</code>
     */
    boolean isAuto();

    /**
     * Add a listener for query events.
     *
     * @param listener the listener to add
     */
    void addQueryListener(QueryListener listener);

    /**
     * Remove a listener.
     *
     * @param listener the listener to remove
     */
    void removeQueryListener(QueryListener listener);

    /**
     * Set query constraints.
     *
     * @param constraints the constraints
     */
    void setConstraints(IConstraint constraints);

}
