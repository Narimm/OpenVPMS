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

package org.openvpms.web.component.im.relationship;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IdConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Helper to create {@link RelationshipState} instances for each of the supplied
 * {@link EntityRelationship} instances associated with an {@link Entity}.
 * This performs a query to reduce the number of database accesses.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class RelationshipStateQuery {

    /**
     * The parent entity.
     */
    private final Entity entity;

    /**
     * The relationships.
     */
    private final List<IMObject> relationships;

    /**
     * The relationship short names.
     */
    private final String[] relationshipShortNames;

    /**
     * Determines if <tt>entity</tt> is the source or target of the
     * relationships.
     */
    private final boolean source;

    /**
     * The short names of the entities to query, obtained from the
     * relationships.
     */
    private final String[] shortNames;

    /**
     * The primary relationship node name. If entity is the source,
     * it will be "source", otherwise "target".
     */
    private final String primaryNode;

    /**
     * The secondary relationship node name. If entity is the source,
     * it will be "target", otherwise "source".
     */
    private final String secondaryNode;

    /**
     * The qualified secondary id node.
     */
    private final String secondaryIdNode;

    /**
     * The qualified secondary name node.
     */
    private final String secondaryNameNode;

    /**
     * The qualified secondary description node.
     */
    private final String secondaryDescNode;

    /**
     * The qualified secondary active node.
     */
    private final String secondaryActiveNode;

    /**
     * The state factory.
     */
    private final RelationshipStateFactory factory;

    /**
     * The default state factory.
     */
    private static final RelationshipStateFactory DEFAULT_FACTORY
            = new RelationshipStateFactory();


    /**
     * Creates a new <tt>RelationshipStateQuery</tt>.
     *
     * @param entity                 the parent entity
     * @param relationships          the relationships
     * @param relationshipShortNames the relationship short names
     */
    public RelationshipStateQuery(Entity entity, List<IMObject> relationships,
                                  String[] relationshipShortNames) {
        this(entity, relationships, relationshipShortNames, DEFAULT_FACTORY);
    }

    /**
     * Creates a new <tt>RelationshipStateQuery</tt>.
     *
     * @param entity                 the parent entity
     * @param relationships          the relationships
     * @param relationshipShortNames the relationship short names
     * @param factory                the relationship state factory
     */
    public RelationshipStateQuery(Entity entity, List<IMObject> relationships,
                                  String[] relationshipShortNames,
                                  RelationshipStateFactory factory) {
        this.entity = entity;
        this.relationships = relationships;
        this.relationshipShortNames = relationshipShortNames;
        this.factory = factory;
        String[] sourceShortNames = DescriptorHelper.getNodeShortNames(
                relationshipShortNames, "source");
        source = TypeHelper.isA(entity, sourceShortNames);
        if (source) {
            primaryNode = "source";
            secondaryNode = "target";
            shortNames = DescriptorHelper.getNodeShortNames(
                    relationshipShortNames, secondaryNode);
        } else {
            primaryNode = "target";
            secondaryNode = "source";
            shortNames = sourceShortNames;
        }
        String alias = getSecondaryAlias();
        secondaryIdNode = alias + ".id";
        secondaryNameNode = alias + ".name";
        secondaryDescNode = alias + ".description";
        secondaryActiveNode = alias + ".active";
    }

    /**
     * Returns the parent entity.
     *
     * @return the parent entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Determines if th parent is the source or target of the relationships.
     *
     * @return <tt>true</tt> if the parent is the source
     */
    public boolean parentIsSource() {
        return source;
    }

    /**
     * Queries all those {@link RelationshipState} instances corresponding
     * to the relationships supplied at construction.
     *
     * @return the matching {@link RelationshipState} instances, keyed on
     *         their associated relationships
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Map<EntityRelationship, RelationshipState> query() {
        // create a map of relationships keyed on their id
        Map<Long, EntityRelationship> relsById
                = new HashMap<Long, EntityRelationship>();
        for (IMObject relationship : relationships) {
            relsById.put(relationship.getId(),
                         (EntityRelationship) relationship);
        }
        Map<EntityRelationship, RelationshipState> result
                = new HashMap<EntityRelationship, RelationshipState>();
        if (!relsById.isEmpty()) {
            // query the the database for matching relationship states, and
            // create RelationshipState instances for each returned ObjectSet
            ArchetypeQuery query = createQuery();
            Iterator<ObjectSet> iter = new ObjectSetQueryIterator(query);
            while (iter.hasNext()) {
                ObjectSet set = iter.next();
                long relId = set.getLong("rel.id");
                long id = set.getLong(secondaryIdNode);
                String name = set.getString(secondaryNameNode);
                String description = set.getString(secondaryDescNode);
                boolean active = set.getBoolean(secondaryActiveNode);

                EntityRelationship r = relsById.remove(relId);
                if (r != null) {
                    long sourceId;
                    String sourceName;
                    String sourceDesc;
                    long targetId;
                    String targetName;
                    String targetDesc;

                    if (source) {
                        sourceId = entity.getId();
                        sourceName = entity.getName();
                        sourceDesc = entity.getDescription();
                        targetId = id;
                        targetName = name;
                        targetDesc = description;
                    } else {
                        sourceId = id;
                        sourceName = name;
                        sourceDesc = description;
                        targetId = entity.getId();
                        targetName = entity.getName();
                        targetDesc = entity.getDescription();
                    }
                    RelationshipState state = factory.create(
                            r, sourceId, sourceName, sourceDesc, targetId,
                            targetName, targetDesc, active);
                    result.put(r, state);
                } else {
                    // no corresponding EntityRelationship, so discard it
                }
            }

            // If there are any remaining relationships, then these are not
            // present in the database. For these create the RelationshipState
            // from the relationship. This is somewhat slower
            // as the name and active nodes require a single query for each
            // relationship.
            for (EntityRelationship r : relsById.values()) {
                result.put(r, factory.create(entity, r, source));
            }
        }
        return result;
    }

    /**
     * Returns the state factory.
     *
     * @return the state factory
     */
    public RelationshipStateFactory getFactory() {
        return factory;
    }

    /**
     * Creates a new archetype query returning the relationship id,
     * and the name and active nodes for the secondary entity.
     *
     * @return a new archetype query
     */
    protected ArchetypeQuery createQuery() {
        ShortNameConstraint relationships = new ShortNameConstraint(
                "rel", relationshipShortNames, false, false);
        ObjectRefConstraint primary = new ObjectRefConstraint(
                primaryNode, entity.getObjectReference());
        ShortNameConstraint secondary = new ShortNameConstraint(
                secondaryNode, shortNames, false, false);

        ArchetypeQuery query = new ArchetypeQuery(relationships);
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        query.add(primary);
        query.add(secondary);
        query.add(new IdConstraint("rel.source", "source"));
        query.add(new IdConstraint("rel.target", "target"));
        query.add(new NodeSelectConstraint("rel.id"));
        query.add(new NodeSelectConstraint(secondaryIdNode));
        query.add(new NodeSelectConstraint(secondaryNameNode));
        query.add(new NodeSelectConstraint(secondaryDescNode));
        query.add(new NodeSelectConstraint(secondaryActiveNode));
        return query;
    }

    /**
     * Returns the short names of the entities to query, obtained from the
     * relationships.
     *
     * @return the entity short names
     */
    protected String[] getShortNames() {
        return shortNames;
    }

    /**
     * Returns the query alias for the secondary (i.e non parent) entity.
     *
     * @return the alias
     */
    protected String getSecondaryAlias() {
        return secondaryNode;
    }

}
