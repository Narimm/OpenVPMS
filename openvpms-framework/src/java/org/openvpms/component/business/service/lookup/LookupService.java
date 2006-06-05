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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.dao.im.lookup.ILookupDAO;
import org.openvpms.component.business.dao.im.lookup.LookupDAOException;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionTypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.property.AssertionProperty;
import org.openvpms.component.business.domain.im.datatypes.property.NamedProperty;
import org.openvpms.component.business.domain.im.datatypes.property.PropertyList;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;

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
        return (Lookup) archetypeService.create(descriptor
                .getType());
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
                    new Object[] { relationship }, exception);
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
                    new Object[] { relationship }, exception);
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

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.lookup.ILookupService#get(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public List get(String rmName, String entityName, String conceptName, String instanceName) {
        try {
            return dao.get(rmName, entityName, conceptName, instanceName);
        } catch (LookupDAOException exception) {
            throw new LookupServiceException(
                    LookupServiceException.ErrorCode.FailedToFindLookups,
                    new Object[]{rmName, entityName, conceptName, instanceName}, 
                    exception);
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
     * @see org.openvpms.component.business.service.lookup.ILookupService#get(org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor)
     */
    public List<Lookup> get(NodeDescriptor descriptor) {
        List<Lookup> lookups = new ArrayList<Lookup>();

        if (descriptor.isLookup()) {
            if (descriptor.containsAssertionType("lookup")) {
                AssertionDescriptor assertion = descriptor.getAssertionDescriptor("lookup");
                
                // This is a remote lookup
                String type = (String) assertion.getPropertyMap().getProperties()
                    .get("type").getValue();
                String concept = (String) assertion.getPropertyMap().getProperties()
                    .get("concept").getValue();

                if ((StringUtils.isEmpty(type)) || 
                    (StringUtils.isEmpty(concept))) {
                    throw new LookupServiceException(
                            LookupServiceException.ErrorCode.InvalidAssertion,
                            new Object[] { assertion.getName() });
                }
                
                if (type.equals("lookup")) {
                    lookups = dao.getLookupsByConcept((String)assertion
                            .getPropertyMap().getProperties()
                            .get("concept").getValue());
                } else {
                    // invalid lookup type throw an exception
                    throw new LookupServiceException(
                            LookupServiceException.ErrorCode.InvalidLookupType,
                            new Object[] { type });
                }
            } else if (descriptor.containsAssertionType("lookup.local")){
                // it is a local lookup
                // TODO This is very inefficient..we should cache them in
                // this service
                AssertionDescriptor assertion = descriptor.getAssertionDescriptor("lookup.local");
                PropertyList list = (PropertyList)assertion.getPropertyMap()
                    .getProperties().get("entries");
                for (NamedProperty prop : list.getProperties()) {
                    AssertionProperty aprop = (AssertionProperty)prop;
                    lookups.add(new Lookup(ArchetypeId.LocalLookupId, 
                            aprop.getName(), aprop.getValue()));
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
     * @see org.openvpms.component.business.service.lookup.ILookupService#get(org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor,
     *      org.openvpms.component.business.domain.im.common.IMObject)
     */
    public List<Lookup> get(NodeDescriptor descriptor, IMObject object) {
        List<Lookup> lookups = new ArrayList<Lookup>();

        // TODO This needs to be fixed up so that it is more pluggabl
        // Need to define a better interface for the different assertion
        // types.
        if (descriptor.isLookup()) {
            
            if (descriptor.containsAssertionType("lookup")) {
                // This is a remote lookup
                AssertionDescriptor assertion = descriptor.getAssertionDescriptor("lookup");
                String type = (String) assertion.getPropertyMap().getProperties()
                    .get("type").getValue();
                String concept = (String) assertion.getPropertyMap().getProperties()
                    .get("concept").getValue();

                // if the type and concept properties are not specified
                // then throw an exception
                if ((StringUtils.isEmpty(type))
                        || (StringUtils.isEmpty(concept))) {
                    throw new LookupServiceException(
                            LookupServiceException.ErrorCode.InvalidAssertion,
                            new Object[] { assertion.getName() });
                }

                if (type.equals("lookup")) {
                    lookups = dao.getLookupsByConcept(concept);
                } else if (type.equals("targetLookup")) {
                    String source = (String) assertion.getPropertyMap().getProperties()
                            .get("source").getValue();
                    if (StringUtils.isEmpty(source)) {
                        throw new LookupServiceException(
                                LookupServiceException.ErrorCode.InvalidAssertion,
                                new Object[] { assertion.getName() });
                    }

                    lookups = dao.getTargetLookups(concept, (String)JXPathContext
                            .newContext(object).getValue(source));
                } else if (type.equals("sourceLookup")) {
                    String target = (String) assertion.getPropertyMap().getProperties()
                            .get("target").getValue();
                    if (StringUtils.isEmpty(target)) {
                        throw new LookupServiceException(
                                LookupServiceException.ErrorCode.InvalidAssertion,
                                new Object[] { assertion.getName() });

                    }
                    lookups = dao.getSourceLookups(concept, (String)JXPathContext
                            .newContext(object).getValue(target));
                } else {
                    // invalid lookup type throw an exception
                    throw new LookupServiceException(
                            LookupServiceException.ErrorCode.InvalidLookupType,
                            new Object[] { type });
                }
            } else if (descriptor.containsAssertionType("lookup.local")){
                // it is a local lookup
                // TODO This is very inefficient..we should cache them in
                // this service
                AssertionDescriptor assertion = descriptor.getAssertionDescriptor("lookup.local");
                PropertyList list = (PropertyList)assertion.getPropertyMap()
                    .getProperties().get("entries");
                for (NamedProperty prop : list.getProperties()) {
                    AssertionProperty aprop = (AssertionProperty)prop;
                    lookups.add(new Lookup(ArchetypeId.LocalLookupId, 
                            aprop.getName(), aprop.getValue()));
                }
            } else if (descriptor.containsAssertionType("lookup.assertionType")){
                // retrieve all the assertionTypes from the archetype service
                // we need to 
                List<AssertionTypeDescriptor> adescs = 
                    archetypeService.getAssertionTypeDescriptors();
                for (AssertionTypeDescriptor adesc : adescs) {
                    lookups.add(new Lookup(ArchetypeId.LocalLookupId, 
                            adesc.getName(), adesc.getName()));
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
