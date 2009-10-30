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
     * Maps an archetype class to its corresponding SQL update statement.
     */
    private static final Map<Class, String> sqlMappings = new HashMap<Class, String>();

    /**
     * Maps an archetype class to its corresponding Hibernate class.
     */
    private static final Map<Class, Class> hqlMappings = new HashMap<Class, Class>();

    /**
     * The entity_classifications table update statement.
     */
    private static final String entityClassificationsUpdate;

    /**
     * The entity_classifications table delete statement.
     */
    private static final String entityClassificationsDelete;

    /**
     * The contact_classifications table update statement.
     */
    private static final String contactClassificationsUpdate;

    /**
     * The contact_classifications table delete statement.
     */
    private static final String contactClassificationsDelete;

    /**
     * The product_price_classifications table update statement.
     */
    private static final String priceClassificationsUpdate;

    /**
     * The product_price_classifications table delete statement.
     */
    private static final String priceClassificationsDelete;


    static {
        sqlMappings.put(Act.class, createSQL("acts", "act_details", "act_id"));
        sqlMappings.put(ActRelationship.class, createSQL("act_relationships", "act_relationship_details",
                                                         "act_relationship_id"));
        sqlMappings.put(Contact.class, createSQL("contacts", "contact_details", "contact_id"));
        sqlMappings.put(Document.class, createSQL("documents", "document_details", "document_id"));
        sqlMappings.put(Entity.class, createSQL("entities", "entity_details", "entity_id"));
        sqlMappings.put(EntityIdentity.class, createSQL("entity_identities", "entity_identity_details",
                                                        "entity_identity_id"));
        sqlMappings.put(EntityRelationship.class, createSQL("entity_relationships", "entity_relationship_details",
                                                            "entity_relationship_id"));
        sqlMappings.put(Lookup.class, createSQL("lookups", "lookup_details", "lookup_id"));
        sqlMappings.put(LookupRelationship.class, createSQL("lookup_relationships", "lookup_relationship_details",
                                                            "lookup_relationship_id"));
        sqlMappings.put(Participation.class, createSQL("participations", "participation_details", "participation_id"));
        sqlMappings.put(ProductPrice.class, createSQL("product_prices", "product_price_details", "product_price_id"));

        hqlMappings.put(Act.class, ActDOImpl.class);
        hqlMappings.put(ActRelationship.class, ActRelationshipDOImpl.class);
        hqlMappings.put(Contact.class, ContactDOImpl.class);
        hqlMappings.put(Document.class, DocumentDOImpl.class);
        hqlMappings.put(Entity.class, EntityDOImpl.class);
        hqlMappings.put(EntityIdentity.class, EntityIdentityDOImpl.class);
        hqlMappings.put(EntityRelationship.class, EntityRelationshipDOImpl.class);
        hqlMappings.put(Lookup.class, LookupDOImpl.class);
        hqlMappings.put(LookupRelationship.class, LookupRelationshipDOImpl.class);
        hqlMappings.put(Participation.class, ParticipationDOImpl.class);
        hqlMappings.put(ProductPrice.class, ProductPriceDOImpl.class);

        entityClassificationsUpdate = createClassificationsUpdateSQL("entity_classifications", "entity_id");
        entityClassificationsDelete = createClassificationsDeleteSQL("entity_classifications");

        contactClassificationsUpdate = createClassificationsUpdateSQL("contact_classifications", "contact_id");
        contactClassificationsDelete = createClassificationsDeleteSQL("contact_classifications");

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
     * Replaces instances of the source lookup with the target.
     *
     * @param source  the lookup to replace
     * @param target  the lookup to replace <tt>source</tt> with
     * @param session the session
     */
    public void replace(Lookup source, Lookup target, Session session) {
        if (source.getObjectReference().equals(target.getObjectReference())) {
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
        Class clazz = archetype.getClazz();
        String sql = sqlMappings.get(clazz);
        while (sql == null && !clazz.equals(Object.class)) {
            clazz = clazz.getSuperclass();
            sql = sqlMappings.get(clazz);
        }
        if (sql == null) {
            throw new IllegalStateException("Cannot update code node=" + node.getName() + ", archetype="
                                            + archetype.getType() + ". Unsupported class: " + clazz);
        }
        SQLQuery query = session.createSQLQuery(sql);
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
        Class clazz = archetype.getClazz();
        Class doClass = hqlMappings.get(clazz);
        while (doClass == null && !clazz.equals(Object.class)) {
            clazz = clazz.getSuperclass();
            doClass = hqlMappings.get(clazz);
        }
        if (doClass == null) {
            throw new IllegalStateException("Cannot update code node=" + node.getName() + ", archetype="
                                            + archetype.getType() + ". Unsupported class: " + clazz);
        }
        String name = node.getPath().substring(1);
        StringBuilder hql = new StringBuilder("update ").append(doClass.getName())
                .append(" set ").append(name).append(" = :newCode where archetypeId.shortName = :archetype and ")
                .append(name).append(" = :oldCode");
        Query query = session.createQuery(hql.toString());
        query.setString("archetype", archetype.getType().getShortName());
        query.setString("oldCode", source.getCode());
        query.setString("newCode", target.getCode());
        query.executeUpdate();
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
     * Helper to create an SQL update statement that updates the value column of a 'details' table
     * for a given archetype and value.
     *
     * @param table   the table name, used to restrict on archetype
     * @param details the details table name
     * @param id      the join column name
     * @return an SQL update statement
     */
    private static String createSQL(String table, String details, String id) {
        return "update " + table + " t join " + details + " d on t." + id + " = d." + id
               + " set d.value = :newCode"
               + " where t.arch_short_name=:archetype and d.name = :name and d.value = :oldCode";
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
