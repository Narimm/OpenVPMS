/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.delete;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.model.object.Relationship;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.springframework.transaction.PlatformTransactionManager;


/**
 * An {@link IMObjectDeletionHandler} for {@link Entity} instances.
 * <p>
 * By default, this prevents deletion of any {@link Entity} that is the target of a <em>participation.*</em> or
 * <em>entityLink.*</em>, or is the source of an entity relationship.
 *
 * @author Tim Anderson
 */
public abstract class AbstractEntityDeletionHandler<T extends Entity> extends AbstractIMObjectDeletionHandler<T> {

    /**
     * The default participation archetypes to check.
     */
    public static final String[] DEFAULT_PARTICIPATIONS = {"participation.*"};


    /**
     * The entity relationships to exclude from checks.
     */
    private final String[] exclude;


    /**
     * Constructs a {@link AbstractEntityDeletionHandler}.
     *
     * @param object             the object to delete
     * @param factory            the editor factory
     * @param transactionManager the transaction manager
     * @param service            the archetype service
     */
    public AbstractEntityDeletionHandler(T object, IMObjectEditorFactory factory,
                                         PlatformTransactionManager transactionManager, IArchetypeRuleService service) {
        this(object, null, factory, transactionManager, service);
    }

    /**
     * Constructs a {@link AbstractEntityDeletionHandler}.
     *
     * @param exclude            the entity relationships to exclude from checks. May be null
     * @param factory            the editor factory
     * @param transactionManager the transaction manager
     * @param service            the archetype service
     */
    public AbstractEntityDeletionHandler(T object, String[] exclude,
                                         IMObjectEditorFactory factory,
                                         PlatformTransactionManager transactionManager,
                                         IArchetypeRuleService service) {
        super(object, factory, transactionManager, service);
        this.exclude = exclude;
    }

    /**
     * Determines if an object can be deleted.
     *
     * @return {@code true} if the object can be deleted
     */
    @Override
    public boolean canDelete() {
        T object = getObject();
        return !hasParticipations(object, getParticipations()) && !hasMatches(object, exclude);
    }

    /**
     * Returns the participation archetypes to check.
     *
     * @return the participation archetypes to check
     */
    protected String[] getParticipations() {
        return DEFAULT_PARTICIPATIONS;
    }

    /**
     * Determines if an entity has participations of the specified archetypes.
     *
     * @param entity     the entity
     * @param archetypes the participation archetypes.
     * @return {@code true} if the entity has participations, otherwise {@code false}
     * @throws ArchetypeServiceException for any error
     */
    protected boolean hasParticipations(Entity entity, String[] archetypes) {
        ArchetypeQuery query = new ArchetypeQuery(archetypes, false, false);
        query.add(Constraints.eq("entity", entity));
        return hasMatches(query);
    }

    /**
     * Determines if an entity is the target of any entity links.
     *
     * @param entity the entity
     * @return {@code true} if the entity is a target of at least one entity link
     */
    protected boolean hasEntityLinks(Entity entity) {
        ArchetypeQuery query = new ArchetypeQuery("entityLink.*", false, false);
        query.add(Constraints.eq("target", entity));
        return hasMatches(query);
    }

    /**
     * Determines if an entity has any relationships where it is the source, and the relationship isn't excluded.
     *
     * @param entity  the entity
     * @param exclude the relationships to exclude
     * @return {@code true} if the entity has relationships where it is the source, otherwise {@code false}
     */
    protected boolean hasMatches(Entity entity, String[] exclude) {
        boolean result = false;
        if (exclude == null || exclude.length == 0) {
            result = !entity.getSourceEntityRelationships().isEmpty();
        } else {
            for (Relationship relationship : entity.getSourceEntityRelationships()) {
                if (!relationship.isA(exclude)) {
                    result = true;
                    break;
                }
            }
        }
        if (!result) {
            result = hasEntityLinks(entity);
        }
        return result;
    }

}
