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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.util;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBeanException;

import java.util.List;


/**
 * Entity relationship helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class EntityRelationshipHelper {

    /**
     * Returns the target from the default entity relationship from the
     * specified relationship node.
     *
     * @param entity  the parent entity
     * @param node    the relationship node
     * @param service the archetype service
     * @return the default target, or the the first target if there is no
     *         default, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the node does't exist or an element
     *                                   is of the wrong type
     */
    public static IMObjectReference getDefaultTargetRef(
            Entity entity, String node, IArchetypeService service) {
        return getDefaultTargetRef(entity, node, true, service);
    }

    /**
     * Returns the target from the default entity relationship from the
     * specified relationship node.
     *
     * @param entity  the parent entity
     * @param node    the relationship node
     * @param useNonDefault if <tt>true</tt> use a non-default relationship if
     *                      no default can be found
     * @param service the archetype service
     * @return the default target, or the the first target if there is no
     *         default, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the node does't exist or an element
     *                                   is of the wrong type
     */
    public static IMObjectReference getDefaultTargetRef(
            Entity entity, String node, boolean useNonDefault,
            IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(entity, service);
        return getDefaultTargetRef(
                bean.getValues(node, EntityRelationship.class), useNonDefault,
                service);
    }

    /**
     * Returns the active target from the default entity relationship from the
     * specified relationship node.
     *
     * @param entity  the parent entity
     * @param node    the relationship node
     * @param service the archetype service
     * @return the default target, or the the first target if there is no
     *         default, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the node does't exist or an element
     *                                   is of the wrong type
     */
    public static Entity getDefaultTarget(Entity entity, String node,
                                          IArchetypeService service) {
        return getDefaultTarget(entity, node, true, service);
    }

    /**
     * Returns the active target from the default entity relationship from the
     * specified relationship node.
     *
     * @param entity        the parent entity
     * @param node          the relationship node
     * @param useNonDefault if <tt>true</tt> use a non-default relationship if
     *                      no default can be found
     * @param service       the archetype service
     * @return the default target, or the the first target if there is no
     *         default and <tt>useNonDefault</tt> is <tt>true</tt>,
     *         or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     * @throws IMObjectBeanException     if the node does't exist or an element
     *                                   is of the wrong type
     */
    public static Entity getDefaultTarget(Entity entity, String node,
                                          boolean useNonDefault,
                                          IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(entity, service);
        return getDefaultTarget(bean.getValues(node, EntityRelationship.class),
                                useNonDefault, service);
    }

    /**
     * Returns the active target from the default entity relationship from the
     * supplied relationship list.
     *
     * @param relationships a list of relationship objects
     * @param service       the archetype service
     * @return the default target, or the the first target if there is no
     *         default, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Entity getDefaultTarget(
            List<EntityRelationship> relationships, IArchetypeService service) {
        return getDefaultTarget(relationships, true, service);
    }

    /**
     * Returns the active target from the default entity relationship from the
     * supplied relationship list.
     *
     * @param relationships a list of relationship objects
     * @param useNonDefault if <tt>true</tt> use a non-default relationship if
     *                      no default can be found
     * @param service       the archetype service
     * @return the default target, or the the first target if there is no
     *         default and <tt>useNonDefault</tt> is <tt>true</tt>,
     *         or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static Entity getDefaultTarget(
            List<EntityRelationship> relationships, boolean useNonDefault,
            IArchetypeService service) {
        Entity result = null;
        for (EntityRelationship relationship : relationships) {
            if (relationship.isActive()) {
                if (result == null && useNonDefault) {
                    result = getEntity(relationship.getTarget(), service);
                } else {
                    IMObjectBean bean = new IMObjectBean(relationship);
                    if (bean.hasNode("default") && bean.getBoolean("default")) {
                        result = getEntity(relationship.getTarget(), service);
                        if (result != null) {
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns the active target reference from the default entity relationship
     * from the supplied relationship list.
     *
     * @param relationships a list of relationship objects
     * @return the default target, or the the first target if there is no
     *         default and <tt>useNonDefault</tt> is <tt>true</tt>,
     *         or <tt>null</tt> if none is found
     * @param service       the archetype service
     * @return the default target, or the the first target if there is no
     *         default, or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static IMObjectReference getDefaultTargetRef(
            List<EntityRelationship> relationships, boolean useNonDefault,
            IArchetypeService service) {
        IMObjectReference result = null;
        for (EntityRelationship relationship : relationships) {
            if (relationship.isActive()) {
                if (result == null && useNonDefault) {
                    result = relationship.getTarget();
                } else {
                    IMObjectBean bean = new IMObjectBean(relationship, service);
                    if (bean.hasNode("default") && bean.getBoolean("default")) {
                        result = relationship.getTarget();
                        if (result != null) {
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Makes a relationship the default, resetting the default status of any
     * other relationship associated with the specified node.
     *
     * @param entity       the entity
     * @param node         the relationship node
     * @param relationship the relationship to make the default
     * @param service      the archetype service
     * @throws ArchetypeServiceException for any archetype service error
     */
    public static void setDefault(Entity entity, String node,
                                  EntityRelationship relationship,
                                  IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(entity, service);
        List<EntityRelationship> relationships
                = bean.getValues(node, EntityRelationship.class);
        for (EntityRelationship r : relationships) {
            if (r != relationship) {
                IMObjectBean relBean = new IMObjectBean(r, service);
                relBean.setValue("default", false);
            }
        }
        IMObjectBean relBean = new IMObjectBean(relationship, service);
        relBean.setValue("default", true);
    }


    /**
     * Returns the entity associate with a reference.
     *
     * @param ref     the reference. May be <tt>null</tt>
     * @param service the archetype service.
     * @return the corresponding entity, or <tt>null</tt>
     * @throws ArchetypeServiceException for any archetype service error
     */
    private static Entity getEntity(IMObjectReference ref,
                                    IArchetypeService service) {
        Entity result = null;
        if (ref != null) {
            Entity entity = (Entity) service.get(ref);
            if (entity != null && entity.isActive()) {
                result = entity;
            }
        }
        return result;
    }

}
