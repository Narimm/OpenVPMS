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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
     * Replaces one lookup with another.
     * <p/>
     * Each lookup must be of the same archetype.
     *
     * @param source the lookup to replace
     * @param target the lookup to replace <tt>source</tt> with
     */
    public void replace(Lookup source, Lookup target) {
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
        query.setMaxResults(ArchetypeQuery.ALL_RESULTS);
        List results = service.get(query).getResults();
        return (List<Lookup>) results;
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
