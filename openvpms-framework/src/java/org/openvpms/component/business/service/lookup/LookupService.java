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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.service.lookup;

// openvpms-framework
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.dao.im.lookup.ILookupDAO;
import org.openvpms.component.business.dao.im.lookup.LookupDAOException;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.service.archetype.descriptor.AssertionProperty;
import org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupService implements ILookupService {
    /**
     * Cache a reference to an archetype archetype service..
     */
    private IArchetypeService archetypeService;

    /**
     * The DAO instance it will use
     */
    private ILookupDAO dao;

    /**
     * Instantiate an instance of this service passing it a reference to the
     * archetype servie and a DAO.
     * <p>
     * The archetype service is mandatory, since it provides a means of mapping
     * a short name (i.e. party.person) to a archetype identifier.
     * <p>
     * The data access object is required to interact with persistent or non-
     * persistent store. The store is used to cache the data.
     * 
     * @param archetypeService
     *            the archetype service reference
     * @param dao
     *            the reference to the data access object it will use for the
     *            service
     * @throws LookupServiceException
     *             a runtime exception tha is raised if the service cannot be
     *             instatiated
     */
    public LookupService(IArchetypeService archetypeService, ILookupDAO dao) {
        this.archetypeService = archetypeService;
        this.dao = dao;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#createLookup(java.lang.String)
     */
    public Lookup create(String shortName) {
        // ensure that we can retrieve an arhetype record for the
        // specified short name
        ArchetypeDescriptor descriptor = archetypeService
                .getArchetypeDescriptor(shortName);
        if (descriptor == null) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedToLocateArchetype,
                    new Object[] { shortName });
        }

        // create and return the party object
        return (Lookup) archetypeService.createDefaultObject(descriptor
                .getArchetypeId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#insertLookup(org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public void insert(Lookup lookup) {
        archetypeService.validateObject(lookup);
        try {
            dao.insert(lookup);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedToCreateLookup,
                    new Object[] { lookup.toString() }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#removeLookup(org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public void remove(Lookup lookup) {
        try {
            dao.delete(lookup);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedToDeleteLookup,
                    new Object[] { lookup.getArchetypeId().toString(),
                            lookup.toString() }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#updateLookup(org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public void update(Lookup lookup) {
        archetypeService.validateObject(lookup);
        try {
            dao.update(lookup);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedToUpdateLookup,
                    new Object[] { lookup.toString() }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#save(org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public void save(Lookup lookup) {
        archetypeService.validateObject(lookup);
        try {
            dao.save(lookup);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedToSaveLookup,
                    new Object[] { lookup.toString() }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#add(org.openvpms.component.business.domain.im.lookup.LookupRelationship)
     */
    public void add(LookupRelationship relationship) {
        try {
            dao.insert(relationship);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedtoCreateLookupRelationship,
                    new Object[] { relationship.getType() }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#remove(org.openvpms.component.business.domain.im.lookup.LookupRelationship)
     */
    public void remove(LookupRelationship relationship) {
        try {
            dao.delete(relationship);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedtoDeleteLookupRelationship,
                    new Object[] { relationship.getType() }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#getLookups(java.lang.String)
     */
    public List<Lookup> get(String shortName) {
        try {
            return dao.getLookupsByConcept(shortName);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedToRetrieveLookupsByConcept,
                    new Object[] { shortName }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#getTargetLookups(java.lang.String,
     *      org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public List<Lookup> getTargetLookups(String type, Lookup source) {
        try {
            return dao.getTargetLookups(type, source);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedToRetrieveTargetLookups,
                    new Object[] { type, source }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#getTargetLookups(java.lang.String,
     *      java.lang.String)
     */
    public List<Lookup> getTargetLookups(String type, String source) {
        try {
            return dao.getTargetLookups(type, source);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedToRetrieveTargetLookups,
                    new Object[] { type, source }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#getSourceLookups(java.lang.String,
     *      org.openvpms.component.business.domain.im.lookup.Lookup)
     */
    public List<Lookup> getSourceLookups(String type, Lookup target) {
        try {
            return dao.getSourceLookups(type, target);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedToRetrieveSourceLookups,
                    new Object[] { type, target }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#getSourceLookups(java.lang.String,
     *      java.lang.String)
     */
    public List<Lookup> getSourceLookups(String type, String target) {
        try {
            return dao.getSourceLookups(type, target);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedToRetrieveSourceLookups,
                    new Object[] { type, target }, exception);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#get(org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor)
     */
    public List<Lookup> get(NodeDescriptor descriptor) {
        List<Lookup> lookups = new ArrayList<Lookup>();

        if (descriptor.isLookup()) {
            Map<String, AssertionDescriptor> assertions = 
                descriptor.getAssertionDescriptorsAsMap();
            
            if (assertions.containsKey("lookup")) {
                AssertionDescriptor assertion = assertions.get("lookup");
                
                // This is a remote lookup
                String type = (String) assertion.getPropertiesAsMap()
                    .get("type").getValue();
                String concept = (String) assertion.getPropertiesAsMap()
                    .get("concept").getValue();

                if ((StringUtils.isEmpty(type)) || 
                    (StringUtils.isEmpty(concept))) {
                    throw new LookupServiceException(
                            LookupServiceException.ErrorCode.InvalidAssertion,
                            new Object[] { assertion.getType() });
                }
                
                if (type.equals("lookup")) {
                    lookups = dao.getLookupsByConcept(assertion
                            .getPropertiesAsMap().get("concept").getValue());
                } else {
                    // invalid lookup type throw an exception
                    throw new LookupServiceException(
                            LookupServiceException.ErrorCode.InvalidLookupType,
                            new Object[] { type });
                }
            } else if (assertions.containsKey("lookup.local")){
                // it is a local lookup
                // TODO This is very inefficient..we should cache them in
                // this service
                AssertionDescriptor assertion = assertions.get("lookup.local");
                for (AssertionProperty prop : assertion.getProperties()) {
                    lookups.add(new Lookup(ArchetypeId.LocalLookupId, prop
                            .getKey(), prop.getValue()));
                }
            } else {
                throw new LookupServiceException(
                        LookupServiceException.ErrorCode.InvalidLookupAssertion,
                        new Object[] { descriptor.getName() });
            }
        }

        return lookups;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.openvpms.component.business.service.lookup.ILookupService#get(org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor,
     *      org.openvpms.component.business.domain.im.common.IMObject)
     */
    public List<Lookup> get(NodeDescriptor descriptor, IMObject object) {
        List<Lookup> lookups = new ArrayList<Lookup>();

        if (descriptor.isLookup()) {
            Map<String, AssertionDescriptor> assertions = 
                descriptor.getAssertionDescriptorsAsMap();
            
            if (assertions.containsKey("lookup")) {
                // This is a remote lookup
                AssertionDescriptor assertion = assertions.get("lookup");
                String type = (String) assertion.getPropertiesAsMap().get(
                        "type").getValue();
                String concept = (String) assertion.getPropertiesAsMap().get(
                        "concept").getValue();

                // if the type and concept properties are not specified
                // then throw an exception
                if ((StringUtils.isEmpty(type))
                        || (StringUtils.isEmpty(concept))) {
                    throw new LookupServiceException(
                            LookupServiceException.ErrorCode.InvalidAssertion,
                            new Object[] { assertion.getType() });
                }

                if (type.equals("lookup")) {
                    lookups = dao.getLookupsByConcept(concept);
                } else if (type.equals("targetLookup")) {
                    String source = (String) assertion.getPropertiesAsMap()
                            .get("source").getValue();
                    if (StringUtils.isEmpty(source)) {
                        throw new LookupServiceException(
                                LookupServiceException.ErrorCode.InvalidAssertion,
                                new Object[] { assertion.getType() });
                    }

                    lookups = dao.getTargetLookups(concept, (String)JXPathContext
                            .newContext(object).getValue(source));
                } else if (type.equals("sourceLookup")) {
                    String target = (String) assertion.getPropertiesAsMap()
                            .get("target").getValue();
                    if (StringUtils.isEmpty(target)) {
                        throw new LookupServiceException(
                                LookupServiceException.ErrorCode.InvalidAssertion,
                                new Object[] { assertion.getType() });

                    }
                    lookups = dao.getSourceLookups(concept, (String)JXPathContext
                            .newContext(object).getValue(target));
                } else {
                    // invalid lookup type throw an exception
                    throw new LookupServiceException(
                            LookupServiceException.ErrorCode.InvalidLookupType,
                            new Object[] { type });
                }
            } else if (assertions.containsKey("lookup.local")){
                // it is a local lookup
                // TODO This is very inefficient..we should cache them in
                // this service
                AssertionDescriptor assertion = assertions.get("lookup.local");
                for (AssertionProperty prop : assertion.getProperties()) {
                    lookups.add(new Lookup(ArchetypeId.LocalLookupId, prop
                            .getKey(), prop.getValue()));
                }
            } else {
                throw new LookupServiceException(
                        LookupServiceException.ErrorCode.InvalidLookupAssertion,
                        new Object[] { descriptor.getName() });
            }
        }

        return lookups;
    }

}
