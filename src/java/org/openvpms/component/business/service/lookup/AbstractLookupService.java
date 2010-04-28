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

package org.openvpms.component.business.service.lookup;

import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertion;
import org.openvpms.component.business.service.archetype.helper.lookup.LookupAssertionFactory;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


/**
 * Abstract implementation of the {@link ILookupService}, that
 * delegates to the {@link IArchetypeService}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractLookupService implements ILookupService {

    /**
     * The archetype service
     */
    private final IArchetypeService service;

    /**
     * The data access object.
     */
    private final IMObjectDAO dao;

    /**
     * The default lookup node.
     */
    private static final String DEFAULT_LOOKUP = "defaultLookup"; // NON-NLS


    /**
     * Constructs an <tt>AbstractLookupService</tt>.
     *
     * @param service the archetype service
     * @param dao     the data access object
     */
    public AbstractLookupService(IArchetypeService service, IMObjectDAO dao) {
        this.service = service;
        this.dao = dao;
    }

    /**
     * Returns the lookup with the specified lookup archetype short name and
     * code.
     *
     * @param shortName the lookup archetype short name
     * @param code      the lookup code
     * @return the corresponding lookup or <tt>null</tt> if none is found
     */
    public Lookup getLookup(String shortName, String code) {
        return query(shortName, code);
    }

    /**
     * Returns all lookups with the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name
     * @return a collection of lookups with the specified short name
     */
    public Collection<Lookup> getLookups(String shortName) {
        return query(shortName);
    }

    /**
     * Returns the default lookup for the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name
     * @return the default lookup, or <Tt>null</tt> if none is found
     */
    public Lookup getDefaultLookup(String shortName) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, false)
                .add(new NodeConstraint(DEFAULT_LOOKUP, RelationalOp.EQ, true))
                .setMaxResults(1);
        List<IMObject> results = getService().get(query).getResults();

        return (!results.isEmpty()) ? (Lookup) results.get(0) : null;
    }

    /**
     * Returns the lookups that are the source of any lookup relationship where
     * the supplied lookup is the target.
     *
     * @param lookup the target lookup
     * @return a collection of source lookups
     */
    public Collection<Lookup> getSourceLookups(Lookup lookup) {
        return getSourceLookups(lookup.getTargetLookupRelationships());
    }

    /**
     * Returns the lookups that are the source of specific lookup relationships
     * where the supplied lookup is the target.
     *
     * @param lookup                the target lookup
     * @param relationshipShortName the relationship short name. May contain
     *                              wildcards
     * @return a collection of source lookups
     */
    public Collection<Lookup> getSourceLookups(Lookup lookup,
                                               String relationshipShortName) {
        Collection<LookupRelationship> relationships
                = getRelationships(relationshipShortName,
                                   lookup.getTargetLookupRelationships());
        return getSourceLookups(relationships);
    }

    /**
     * Returns the lookups that are the target of any lookup relationship where
     * the supplied lookup is the source.
     *
     * @param lookup the source lookup
     * @return a collection of target lookups
     */
    public Collection<Lookup> getTargetLookups(Lookup lookup) {
        return getTargetLookups(lookup.getSourceLookupRelationships());
    }

    /**
     * Returns the lookups that are the target of specific lookup relationships
     * where the supplied lookup is the source.
     *
     * @param lookup                the source lookup
     * @param relationshipShortName the relationship short name. May contain
     *                              wildcards
     * @return a collection of target lookups
     */
    public Collection<Lookup> getTargetLookups(Lookup lookup,
                                               String relationshipShortName) {
        Collection<LookupRelationship> relationships
                = getRelationships(relationshipShortName,
                                   lookup.getSourceLookupRelationships());
        return getTargetLookups(relationships);
    }

    /**
     * Returns a list of lookups for an archetype's node.
     *
     * @param shortName the archetype short name
     * @param node      the node name
     * @return a list of lookups
     */
    public Collection<Lookup> getLookups(String shortName, String node) {
        ArchetypeDescriptor archetype = service.getArchetypeDescriptor(shortName);
        if (archetype == null) {
            throw new IllegalArgumentException("Invalid archetype shortname: " + shortName);
        }
        NodeDescriptor descriptor = archetype.getNodeDescriptor(node);
        if (descriptor == null) {
            throw new IllegalArgumentException("Invalid node name: " + node);
        }
        LookupAssertion assertion = LookupAssertionFactory.create(descriptor, service, this);
        return assertion.getLookups();
    }

    /**
     * Return a list of lookups for a given object and node value.
     * <p/>
     * This will limit lookups returned if the node refers to the source or target of a lookup relationship.
     *
     * @param object the object
     * @param node   the node name
     * @return a list of lookups
     */
    public Collection<Lookup> getLookups(IMObject object, String node) {
        IMObjectBean bean = new IMObjectBean(object, service);
        NodeDescriptor descriptor = bean.getDescriptor(node);
        if (descriptor == null) {
            throw new IllegalArgumentException("Invalid node name: " + node);
        }
        LookupAssertion assertion = LookupAssertionFactory.create(descriptor, service, this);
        return assertion.getLookups(object);
    }

    /**
     * Returns a lookup based on the value of a node.
     *
     * @param object the object
     * @param node   the node name
     * @return the lookup, or <tt>null</tt> if none is found
     */
    public Lookup getLookup(IMObject object, String node) {
        IMObjectBean bean = new IMObjectBean(object, service);
        NodeDescriptor descriptor = bean.getDescriptor(node);
        if (descriptor == null) {
            throw new IllegalArgumentException("Invalid node name: " + node);
        }
        Lookup result = null;
        Object value = descriptor.getValue(object);
        if (value != null) {
            LookupAssertion assertion = LookupAssertionFactory.create(descriptor, service, this);
            result = assertion.getLookup(object, (String) value);
        }
        return result;
    }

    /**
     * Returns a lookup's name based on the value of a node.
     *
     * @param object the object
     * @param node   the node name
     * @return the lookup's name, or <tt>null</tt> if none is found
     */
    public String getName(IMObject object, String node) {
        Lookup lookup = getLookup(object, node);
        return (lookup != null) ? lookup.getName() : null;
    }

    /**
     * Replaces one lookup with another.
     * <p/>
     * Each lookup must be of the same archetype.
     *
     * @param source the lookup to replace
     * @param target the lookup to replace <tt>source</tt> with
     */
    public void replace(Lookup source, Lookup target) {
        if (!source.getArchetypeId().equals(target.getArchetypeId())) {
            throw new LookupServiceException(LookupServiceException.ErrorCode.CannotReplaceArchetypeMismatch);
        }
        dao.replace(source, target);
    }

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Returns the DAO.
     *
     * @return the DAO
     */
    protected IMObjectDAO getDAO() {
        return dao;
    }

    /**
     * Returns the lookup with the specified lookup archetype short name and
     * code.
     *
     * @param shortName the lookup archetype short name
     * @param code      the lookup code
     * @return the corresponding lookup or <tt>null</tt> if none is found
     */
    protected Lookup query(String shortName, String code) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true);
        query.add(new NodeConstraint("code", code)); // NON-NLS
        query.setMaxResults(1);
        List<IMObject> results = service.get(query).getResults();
        return (!results.isEmpty()) ? (Lookup) results.get(0) : null;
    }

    /**
     * Returns all lookups with the specified lookup archetype short name.
     *
     * @param shortName the lookup archetype short name
     * @return a collection of lookups with the specified short name
     */
    @SuppressWarnings("unchecked")
    protected Collection<Lookup> query(String shortName) {
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true);
        query.setMaxResults(1000);
        query.add(new NodeSortConstraint("id"));
        Iterator<Lookup> iter = new IMObjectQueryIterator(service, query);
        List<Lookup> result = new ArrayList<Lookup>();
        while (iter.hasNext()) {
            result.add(iter.next());
        }
        return result;
    }

    /**
     * Retrieves a lookup by reference.
     *
     * @param reference the lookup reference. May be <tt>null</tt>
     * @return the corresponding lookup, or <tt>null</tt> if none is found
     */
    protected Lookup getLookup(IMObjectReference reference) {
        return (reference != null) ? (Lookup) service.get(reference) : null;
    }

    /**
     * Returns all source lookups of the specified relationships.
     *
     * @param relationships the relationships
     * @return the source lookups
     */
    private Collection<Lookup> getSourceLookups(
            Collection<LookupRelationship> relationships) {
        Collection<Lookup> result;
        if (!relationships.isEmpty()) {
            result = new ArrayList<Lookup>();
            for (LookupRelationship relationship : relationships) {
                Lookup source = getLookup(relationship.getSource());
                if (source != null) {
                    result.add(source);
                }
            }
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Returns all target lookups of the specified relationships.
     *
     * @param relationships the relationships
     * @return the target lookups
     */
    private Collection<Lookup> getTargetLookups(
            Collection<LookupRelationship> relationships) {
        Collection<Lookup> result;
        if (!relationships.isEmpty()) {
            result = new ArrayList<Lookup>();
            for (LookupRelationship relationship : relationships) {
                Lookup target = getLookup(relationship.getTarget());
                if (target != null) {
                    result.add(target);
                }
            }
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    /**
     * Helper to return all relationships matching the specified short name.
     *
     * @param shortName     the relationship short name
     * @param relationships the relatiosnhips to search
     * @return all relationships with the specified short name
     */
    private Collection<LookupRelationship> getRelationships(
            String shortName, Collection<LookupRelationship> relationships) {
        Collection<LookupRelationship> result = null;
        for (LookupRelationship relationship : relationships) {
            if (TypeHelper.isA(relationship, shortName)) {
                if (result == null) {
                    result = new ArrayList<LookupRelationship>();
                }
                result.add(relationship);
            }
        }
        if (result == null) {
            result = Collections.emptyList();
        }
        return result;
    }

}
