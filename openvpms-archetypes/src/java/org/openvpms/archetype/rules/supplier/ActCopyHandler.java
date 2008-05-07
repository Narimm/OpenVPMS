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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.supplier;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.AbstractIMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;


/**
 * An {@link IMObjectCopyHandler} for supplier acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
abstract class ActCopyHandler extends AbstractIMObjectCopyHandler {

    /**
     * The short names of types to map.
     */
    private final String[][] typeMap;

    /**
     * If <tt>true</tt>,
     */
    private final boolean reverse;


    /**
     * Creates a new <tt>ActCopyHandler</tt>.
     *
     * @param typeMap the short names of types to map
     */
    public ActCopyHandler(String[][] typeMap) {
        this(typeMap, false);
    }

    /**
     * Creates a new <tt>ActCopyHandler</tt>.
     *
     * @param typeMap the short names of types to map
     * @param reverse if <tt>true</tt>, reverse the short names
     */
    public ActCopyHandler(String[][] typeMap, boolean reverse) {
        this.typeMap = typeMap;
        this.reverse = reverse;
    }

    /**
     * Determines how {@link IMObjectCopier} should treat an object.
     * <p/>
     * For each <tt>Act</tt>, <tt>ActRelationship</tt>, or
     * <tt>Participation</tt>, determines if there is a short name in
     * {@link #typeMap} to map the object to. If so, and:
     * <ul><li>non-null, creates and returns a new instance of the target
     * object</li>
     * <li>null, returns <tt>null</tt></li>
     * </ul>
     * All other objects are returned unchanged.
     *
     * @param object  the source object
     * @param service the archetype service
     * @return <tt>object</tt> if the object shouldn't be copied,
     *         <tt>null</tt> if it should be replaced with <tt>null</tt>,
     *         or a new instance if the object should be copied
     */
    @Override
    public IMObject getObject(IMObject object, IArchetypeService service) {
        IMObject result;
        if (object instanceof Act || object instanceof ActRelationship
                || object instanceof Participation) {
            String shortName = object.getArchetypeId().getShortName();
            for (String[] map : typeMap) {
                String from;
                String to;
                if (!reverse) {
                    from = map[0];
                    to = map[1];
                } else {
                    from = map[1];
                    to = map[0];
                }
                if (from.equals(shortName)) {
                    shortName = to;
                    break;
                }
            }
            if (shortName != null) {
                result = service.create(shortName);
                if (result == null) {
                    throw new ArchetypeServiceException(
                            ArchetypeServiceException.ErrorCode.FailedToCreateArchetype,
                            shortName);
                }
            } else {
                result = null;
            }
        } else {
            result = object;
        }
        return result;
    }

    /**
     * Helper to determine if a node is copyable.
     * <p/>
     * In addition to that provided by the superclass, this implementation
     * excludes the <em>startTime</em>, <em>status</em> and <em>printed</em>
     * nodes.
     *
     * @param node   the node descriptor
     * @param source if <tt>true</tt> the node is the source; otherwise its
     *               the target
     * @return <tt>true</tt> if the node is copyable; otherwise <tt>false</tt>
     */
    @Override
    protected boolean isCopyable(NodeDescriptor node, boolean source) {
        boolean result = super.isCopyable(node, source);
        if (result) {
            String name = node.getName();
            result = !"startTime".equals(name) && !"status".equals(name)
                    && !"printed".equals(name);
        }
        return result;
    }
}
