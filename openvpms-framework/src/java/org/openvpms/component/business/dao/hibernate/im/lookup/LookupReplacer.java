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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.dao.hibernate.im.lookup;

import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.openvpms.component.business.dao.hibernate.im.act.ActDOImpl;
import org.openvpms.component.business.dao.hibernate.im.act.ActRelationshipDOImpl;
import org.openvpms.component.business.dao.hibernate.im.act.ParticipationDOImpl;
import org.openvpms.component.business.dao.hibernate.im.document.DocumentDOImpl;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityDOImpl;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityIdentityDOImpl;
import org.openvpms.component.business.dao.hibernate.im.entity.EntityRelationshipDOImpl;
import org.openvpms.component.business.dao.hibernate.im.party.ContactDOImpl;
import org.openvpms.component.business.dao.hibernate.im.product.ProductPriceDOImpl;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.ActRelationship;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.service.archetype.descriptor.cache.IArchetypeDescriptorCache;

import java.util.HashMap;
import java.util.Map;


/**
 * Replaces an instance of a lookup with another lookup.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupReplacer {

    /**
     * The archetype descriptors.
     */
    private final IArchetypeDescriptorCache archetypes;

    /**
     * The entity_classifications table select statement.
     */
    private static final String entityClassificationsSelect;

    /**
     * The entity_classifications table update statement.
     */
    private static final String entityClassificationsUpdate;

    /**
     * The entity_classifications table delete statement.
     */
    private static final String entityClassificationsDelete;

    /**
     * The contact_classifications table select statement.
     */
    private static final String contactClassificationsSelect;

    /**
     * The contact_classifications table update statement.
     */
    private static final String contactClassificationsUpdate;

    /**
     * The contact_classifications table delete statement.
     */
    private static final String contactClassificationsDelete;

    /**
     * The product_price_classifications table select statement.
     */
    private static final String priceClassificationsSelect;

    /**
     * The product_price_classifications table update statement.
     */
    private static final String priceClassificationsUpdate;

    /**
     * The product_price_classifications table delete statement.
     */
    private static final String priceClassificationsDelete;

    private static class Mapping {

        private final String updateSQL;

        private final String isUsedSQL;

        private final Class impl;

        public Mapping(String table, String details, String joinId, Class impl) {
            this.updateSQL = createUpdateSQL(table, details, joinId);
            this.isUsedSQL = createIsUsedSQL(table, details, joinId);
            this.impl = impl;
        }

        public String getUpdateSQL() {
            return updateSQL;
        }

        public String getIsUsedSQL() {
            return isUsedSQL;
        }

        public Class getImpl() {
            return impl;
        }
    }

    private static final Map<Class, Mapping> mappings = new HashMap<Class, Mapping>();

    private static void addMapping(Class clazz, String table, String details, String joinId, Class impl) {
        mappings.put(clazz, new Mapping(table, details, joinId, impl));
    }

    static {
        addMapping(Act.class, "acts", "act_details", "act_id", ActDOImpl.class);
        addMapping(ActRelationship.class, "act_relationships", "act_relationship_details", "act_relationship_id",
                   ActRelationshipDOImpl.class);
        addMapping(Contact.class, "contacts", "contact_details", "contact_id", ContactDOImpl.class);
        addMapping(Document.class, "documents", "document_details", "document_id", DocumentDOImpl.class);
        addMapping(Entity.class, "entities", "entity_details", "entity_id", EntityDOImpl.class);
        addMapping(EntityIdentity.class, "entity_identities", "entity_identity_details", "entity_identity_id",
                   EntityIdentityDOImpl.class);
        addMapping(EntityRelationship.class, "entity_relationships", "entity_relationship_details",
                   "entity_relationship_id", EntityRelationshipDOImpl.class);
        addMapping(Lookup.class, "lookups", "lookup_details", "lookup_id", LookupDOImpl.class);
        addMapping(LookupRelationship.class, "lookup_relationships", "lookup_relationship_details",
                   "lookup_relationship_id", LookupRelationshipDOImpl.class);
        addMapping(Participation.class, "participations", "participation_details", "participation_id",
                   ParticipationDOImpl.class);
        addMapping(ProductPrice.class, "product_prices", "product_price_details", "product_price_id",
                   ProductPriceDOImpl.class);

        entityClassificationsSelect = createClassificationsSelectSQL("entity_classifications");
        entityClassificationsUpdate = createClassificationsUpdateSQL("entity_classifications", "entity_id");
        entityClassificationsDelete = createClassificationsDeleteSQL("entity_classifications");

        contactClassificationsSelect = createClassificationsSelectSQL("contact_classifications");
        contactClassificationsUpdate = createClassificationsUpdateSQL("contact_classifications", "contact_id");
        contactClassificationsDelete = createClassificationsDeleteSQL("contact_classifications");

        priceClassificationsSelect = createClassificationsSelectSQL("product_price_classifications");
        priceClassificationsUpdate = createClassificationsUpdateSQL("product_price_classifications",
                                                                    "product_price_id");
        priceClassificationsDelete = createClassificationsDeleteSQL("product_price_classifications");
    }

    /**
     * Construct a <tt>LookupReplace</tt>.
     *
     * @param archetypes the archetype descriptors
     */
    public LookupReplacer(IArchetypeDescriptorCache archetypes) {
        this.archetypes = archetypes;
    }

    /**
     * Determines if a lookup is being used.
     *
     * @param lookup  the lookup
     * @param session the session
     * @return <tt>true</tt> if the lookup is being used
     */
    public boolean isUsed(Lookup lookup, Session session) {
        if (isClassificationInUse(lookup, entityClassificationsSelect, session)
            || isClassificationInUse(lookup, contactClassificationsSelect, session)
            || isClassificationInUse(lookup, priceClassificationsSelect, session)) {
            return true;
        }

        // find all uses of the lookup where it is referred to by a node descriptor using the lookup's code
        LookupUsageFinder finder = new LookupUsageFinder(archetypes);
        Map<NodeDescriptor, ArchetypeDescriptor> refs
                = finder.getCodeReferences(lookup.getArchetypeId().getShortName());

        for (Map.Entry<NodeDescriptor, ArchetypeDescriptor> entry : refs.entrySet()) {
            NodeDescriptor node = entry.getKey();
            ArchetypeDescriptor archetype = entry.getValue();
            if (isDetailsNode(node)) {
                if (isUsedSQL(lookup, node, archetype, session)) {
                    return true;
                }
            } else {
                if (isUsedHQL(lookup, node, archetype, session)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Replaces instances of the source lookup with the target.
     *
     * @param source  the lookup to replace
     * @param target  the lookup to replace <tt>source</tt> with
     * @param session the session
     */
    public void replace(Lookup source, Lookup target, Session session) {
        if (source.getId() == target.getId()) {
            throw new IllegalArgumentException("Source and target lookups are identical");
        }
        if (!source.getArchetypeId().equals(target.getArchetypeId())) {
            throw new IllegalArgumentException("Source and target lookups must have the same archetype");
        }

        // find all uses of the lookup where it is referred to by a node descriptor using the lookup's code
        LookupUsageFinder finder = new LookupUsageFinder(archetypes);
        Map<NodeDescriptor, ArchetypeDescriptor> refs
                = finder.getCodeReferences(source.getArchetypeId().getShortName());

        // now replace them
        for (Map.Entry<NodeDescriptor, ArchetypeDescriptor> entry : refs.entrySet()) {
            NodeDescriptor node = entry.getKey();
            ArchetypeDescriptor archetype = entry.getValue();
            if (isDetailsNode(node)) {
                replaceCodeSQL(node, archetype, source, target, session);
            } else {
                replaceCodeHQL(node, archetype, source, target, session);
            }
        }

        // replace any uses of the lookup as classifications. Don't do it based on the archetypes that use the lookup,
        // as these could change over time.
        replaceClassifications(source, target, entityClassificationsUpdate, entityClassificationsDelete, session);
        replaceClassifications(source, target, contactClassificationsUpdate, contactClassificationsDelete, session);
        replaceClassifications(source, target, priceClassificationsUpdate, priceClassificationsDelete, session);
    }

    private boolean isUsedSQL(Lookup lookup, NodeDescriptor node, ArchetypeDescriptor archetype, Session session) {
        Mapping mapping = getMapping(archetype, node);
        SQLQuery query = session.createSQLQuery(mapping.getIsUsedSQL());
        query.setMaxResults(1);
        query.setString("archetype", archetype.getType().getShortName());
        query.setString("name", node.getName());
        query.setString("code", lookup.getCode());
        return !query.list().isEmpty();
    }

    private boolean isUsedHQL(Lookup lookup, NodeDescriptor node, ArchetypeDescriptor archetype, Session session) {
        Mapping mapping = getMapping(archetype, node);
        String name = node.getPath().substring(1);
        StringBuilder hql = new StringBuilder("select id from ").append(mapping.getImpl().getName())
                .append(" where archetypeId.shortName = :archetype and ")
                .append(name).append(" = :code");
        Query query = session.createQuery(hql.toString());
        query.setString("archetype", archetype.getType().getShortName());
        query.setString("code", lookup.getCode());
        query.setMaxResults(1);
        return !query.list().isEmpty();
    }

    /**
     * Determines if a node is a 'details' node. These are mapped to a <em>*_details</em> table by hibernate,
     * and must be updated using SQL rather than HQL.
     *
     * @param node the node to check
     * @return <tt>true</tt> if the node is a details node
     */
    private boolean isDetailsNode(NodeDescriptor node) {
        return node.getPath().startsWith("/details/");
    }

    /**
     * Replaces instances of a lookup where it is referred to by a 'details' node via its code.
     * <p/>
     * This must be done using SQL, as HQL doesn't support updates to maps.
     *
     * @param node      the node descriptor that refers to the lookup's archetype
     * @param archetype the node's archetype descriptor
     * @param source    the lookup to replace
     * @param target    the lookup to replace <tt>source</tt> with
     * @param session   the Hibernate session
     */
    private void replaceCodeSQL(NodeDescriptor node, ArchetypeDescriptor archetype, Lookup source, Lookup target,
                                Session session) {
        Mapping mapping = getMapping(archetype, node);
        SQLQuery query = session.createSQLQuery(mapping.getUpdateSQL());
        query.setString("archetype", archetype.getType().getShortName());
        query.setString("name", node.getName());
        query.setString("oldCode", source.getCode());
        query.setString("newCode", target.getCode());
        query.executeUpdate();
    }

    /**
     * Replaces instances of a lookup where it is referred to by a node via its code.
     *
     * @param node      the node descriptor that refers to the lookup's archetype
     * @param archetype the node's archetype descriptor
     * @param source    the lookup to replace
     * @param target    the lookup to replace <tt>source</tt> with
     * @param session   the Hibernate session
     */
    private void replaceCodeHQL(NodeDescriptor node, ArchetypeDescriptor archetype, Lookup source, Lookup target,
                                Session session) {
        Mapping mapping = getMapping(archetype, node);
        String name = node.getPath().substring(1);
        StringBuilder hql = new StringBuilder("update ").append(mapping.getImpl().getName())
                .append(" set ").append(name).append(" = :newCode where archetypeId.shortName = :archetype and ")
                .append(name).append(" = :oldCode");
        Query query = session.createQuery(hql.toString());
        query.setString("archetype", archetype.getType().getShortName());
        query.setString("oldCode", source.getCode());
        query.setString("newCode", target.getCode());
        query.executeUpdate();
    }

    private boolean isClassificationInUse(Lookup lookup, String sql, Session session) {
        Query query = session.createSQLQuery(sql);
        query.setLong("id", lookup.getId());
        query.setMaxResults(1);
        return !query.list().isEmpty();
    }

    /**
     * Replaces one lookup classification with another.
     * <p/>
     * This requires two SQL statements as HQL doesn't support updates of collection nodes.
     * <p/>
     * The first SQL statement, <tt>updateSQL</tt>, replaces all instances of the source with the target, for a
     * given object, so long as the object hasn't already been classified with the target. This is required to avoid
     * duplicate keys.
     * <p/>
     * The second SQL statement, <tt>deleteSQL</tt>, removes all of those source instances that couldn't be replaced
     * due to duplicates.
     *
     * @param source    the lookup classification to replace
     * @param target    the lookup classification to replace <tt>source</tt> with
     * @param updateSQL the SQL to replace existing instances
     * @param deleteSQL the SQL to delete the source classification with
     * @param session   the session
     */
    private void replaceClassifications(Lookup source, Lookup target, String updateSQL, String deleteSQL,
                                        Session session) {
        Query query = session.createSQLQuery(updateSQL);
        query.setLong("oldId", source.getId());
        query.setLong("newId", target.getId());
        query.executeUpdate();

        query = session.createSQLQuery(deleteSQL);
        query.setLong("oldId", source.getId());
        query.executeUpdate();
    }

    /**
     * Returns a mapping for a given archetype.
     *
     * @param archetype the archetype descriptor
     * @param node      the node, for error reporting
     * @return the corresponding mapping
     */
    private Mapping getMapping(ArchetypeDescriptor archetype, NodeDescriptor node) {
        Class clazz = archetype.getClazz();
        Mapping mapping = mappings.get(clazz);
        while (mapping == null && !clazz.equals(Object.class)) {
            clazz = clazz.getSuperclass();
            mapping = mappings.get(clazz);
        }
        if (mapping == null) {
            throw new IllegalStateException("Cannot update code node=" + node.getName() + ", archetype="
                                            + archetype.getType() + ". Unsupported class: " + clazz);
        }
        return mapping;
    }

    /**
     * Helper to create an SQL update statement that updates the value column of a 'details' table
     * for a given archetype and value.
     *
     * @param table   the table name, used to restrict on archetype
     * @param details the details table name
     * @param id      the join column name
     * @return an SQL update statement
     */
    private static String createUpdateSQL(String table, String details, String id) {
        return "update " + table + " t join " + details + " d on t." + id + " = d." + id
               + " set d.value = :newCode"
               + " where t.arch_short_name=:archetype and d.name = :name and d.value = :oldCode";
    }

    /**
     * Helper to create an SQL statement the determines if a lookup is used by a 'details' node.
     *
     * @param table   the table name, used to restrict on archetype
     * @param details the details table name
     * @param id      the join column name
     * @return an SQL query statement
     */
    private static String createIsUsedSQL(String table, String details, String id) {
        return "select t." + id + " from " + table + " t join " + details + " d on t." + id + " = d." + id
               + " where t.arch_short_name=:archetype and d.name = :name and d.value = :code";
    }

    /**
     * Helper to create an SQL query statement that determines if a lookup is used by a classifications node.
     *
     * @param table the classification table
     * @return a new SQL statement
     */
    private static String createClassificationsSelectSQL(String table) {
        return "select * from " + table + " t where t.lookup_id = :id";
    }

    /**
     * Helper to create an SQL update statement that replaces a classification lookup with another, providing an
     * instance of that lookup doesn't already exist. This is required to avoid duplicate keys.
     *
     * @param table the classification table
     * @param id    the id column to join on
     * @return a new SQL update statement
     */
    private static String createClassificationsUpdateSQL(String table, String id) {
        return "update " + table + " c1 left join " + table + " c2 on c1." + id + " = c2." + id
               + " and c2.lookup_id = :newId "
               + "set c1.lookup_id = :newId "
               + "where c1.lookup_id = :oldId and c2.lookup_id is null";
    }

    /**
     * Helper to create an SQL delete statement that removes a classification lookup.
     *
     * @param table the classification table
     * @return a new SQL delete statement
     */
    private static String createClassificationsDeleteSQL(String table) {
        return "delete from " + table + " where lookup_id = :oldId";
    }

}
