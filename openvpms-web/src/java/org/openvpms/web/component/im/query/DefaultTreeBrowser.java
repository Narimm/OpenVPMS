/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.tree.TreeBuilder;


/**
 * Default implementation of the {@link TreeBrowser} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultTreeBrowser<T extends IMObject>
        extends AbstractTreeBrowser<T> {

    /**
     * Construct a new <code>TreeBrowser</code> that queries IMObjects using the
     * specified query.
     *
     * @param query   the query
     * @param sort    the sort criteria. May be <code>null</code>
     * @param builder the tree builder
     */
    public DefaultTreeBrowser(Query<T> query, SortConstraint[] sort,
                              TreeBuilder<T> builder) {
        super(query, sort, builder);
    }

}
