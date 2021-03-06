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

package org.openvpms.component.service.archetype;

import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.archetype.ArchetypeDescriptor;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.query.TypedQuery;
import org.openvpms.component.query.criteria.CriteriaBuilder;
import org.openvpms.component.query.criteria.CriteriaQuery;

import java.util.Collection;
import java.util.List;

/**
 * The archetype service provides support to create, retrieve, validate, save and remove {@link IMObject}s.
 *
 * @author Tim Anderson
 */
public interface ArchetypeService {

    /**
     * Returns the {@link ArchetypeDescriptor} for the given archetype.
     *
     * @param archetype the archetype
     * @return the descriptor corresponding to the archetype, or {@code null} if none is found
     * @throws OpenVPMSException for any error
     */
    ArchetypeDescriptor getArchetypeDescriptor(String archetype);

    /**
     * Returns a bean for an object.
     *
     * @param object the object
     * @return the bean
     * @throws OpenVPMSException for any error
     */
    IMObjectBean getBean(IMObject object);

    /**
     * Creates an object given its archetype.
     *
     * @param archetype the archetype name
     * @return a new object
     * @throws OpenVPMSException for any error
     */
    IMObject create(String archetype);

    /**
     * Creates an object given its archetype.
     *
     * @param archetype the archetype name
     * @param type the expected type of the object
     * @return a new object
     * @throws OpenVPMSException for any error
     * @throws ClassCastException if the resulting object is not of the expected type
     */
    <T extends IMObject> T create(String archetype, Class<T> type);

    /**
     * Saves an object, executing any <em>save</em> rules associated with its archetype.
     *
     * @param object the object to save
     * @throws OpenVPMSException for any error
     */
    void save(IMObject object);

    /**
     * Save a collection of {@link IMObject} instances.
     *
     * @param objects the objects to insert or update
     * @throws OpenVPMSException for any error
     */
    void save(Collection<? extends IMObject> objects);

    /**
     * Remove the specified object.
     *
     * @param object the object to remove
     * @throws OpenVPMSException for any error
     */
    void remove(IMObject object);

    /**
     * Validates an object.
     *
     * @param object the object to validate
     * @return a list of validation errors, if any
     * @throws OpenVPMSException for any error
     */
    List<ValidationError> validate(IMObject object);

    /**
     * Returns an object given its reference.
     *
     * @param reference the reference
     * @return the object, or {@code null} if none is found
     * @throws OpenVPMSException for any error
     */
    IMObject get(Reference reference);

    /**
     * Returns an object given its archetype and identifier.
     *
     * @param archetype the object's archetype
     * @param id        the object's identifier
     * @return the object, or {@code null} if none is found
     * @throws OpenVPMSException for any error
     */
    IMObject get(String archetype, long id);

    /**
     * Returns a builder to create queries.
     *
     * @return the criteria builder
     */
    CriteriaBuilder getCriteriaBuilder();

    /**
     * Creates a {@link TypedQuery} for executing a criteria query.
     *
     * @param query the criteria query
     * @return the new query instance
     */
    <T> TypedQuery<T> createQuery(CriteriaQuery<T> query);

}
