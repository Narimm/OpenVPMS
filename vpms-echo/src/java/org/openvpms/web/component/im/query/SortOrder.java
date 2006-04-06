package org.openvpms.web.component.im.query;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class SortOrder {

    /**
     * The sort node.
     */
    private final String _node;

    /**
     * if <code>true</code> sort the node in ascending order; otherwise sort it
     * in <code>descebding</code> order
     */
    private final boolean _ascending;

    /**
     * Construct a new <code>SortOrder</code>.
     *
     * @param node      the node to sort on
     * @param ascending if <code>true</code> sort the node in ascending order;
     *                  otherwise sort it in <code>descebding</code> order
     */
    public SortOrder(String node, boolean ascending) {
        _node = node;
        _ascending = ascending;
    }

    /**
     * Returns the sort node.
     *
     * @return the sort node
     */
    public String getNode() {
        return _node;
    }

    /**
     * Determines if the node is sorted in ascending order.
     *
     * @return <code>true</code> if the node should be sorted in ascending
     *         order; <code>false</code> if it should be sorted in
     *         <code>descebding</code> order
     */
    public boolean isAscending() {
        return _ascending;
    }
}
