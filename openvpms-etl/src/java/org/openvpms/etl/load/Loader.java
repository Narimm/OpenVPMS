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

package org.openvpms.etl.load;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.etl.ETLValue;
import org.openvpms.etl.ETLValueDAO;
import org.openvpms.etl.Reference;
import org.openvpms.etl.ReferenceParser;
import static org.openvpms.etl.load.LoaderException.ErrorCode.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Loads {@link IMObject} instances, mapping them from {@link ETLValue}
 * instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Loader {

    /**
     * The source.
     */
    protected final ETLValueDAO dao;

    /**
     * The archetype service.
     */
    protected final IArchetypeService service;

    /**
     * Reference to object id mapping.
     */
    protected Map<String, String> references = new HashMap<String, String>();

    /**
     * The mapped objects, keyed on {@link ETLValue#getObjectId()}.
     * Objects that are only partially mapped non-null {@link LoadState#getObject()}
     * and {@link LoadState#getBean()} values. There's are set to null once the
     * object has been completely processed.
     */
    protected Map<String, LoadState> mapped = new HashMap<String, LoadState>();


    /**
     * Constructs a new <tt>Loader</tt>.
     *
     * @param service the archetype service
     */
    public Loader(ETLValueDAO dao, IArchetypeService service) {
        this.dao = dao;
        this.service = service;
    }

    /**
     * Loads all objects.
     *
     * @return the no. of objects loaded
     * @throws LoaderException           for any load error
     * @throws ArchetypeServiceException for any archetype service error
     */
    public int load() {
        List<ETLValue> values = new ArrayList<ETLValue>();
        Iterator<ETLValue> iterator = new ETLValueIterator(dao);
        String objectId = null;
        String archetype = null;
        while (iterator.hasNext()) {
            ETLValue value = iterator.next();
            if (objectId != null) {
                if (!objectId.equals(value.getObjectId())) {
                    load(objectId, archetype, values);
                    values.clear();
                } else if (!archetype.equals(value.getArchetype())) {
                    throw new LoaderException(ArchetypeMismatch, objectId,
                                              archetype, value.getArchetype());
                }
            }
            values.add(value);
            objectId = value.getObjectId();
            archetype = value.getArchetype();
        }
        if (!values.isEmpty()) {
            load(objectId, archetype, values);
        }
        return mapped.size();
    }

    /**
     * Loads the object associated with reference.
     *
     * @param reference the reference to load
     * @return the object corresponding to the reference
     * @throws LoaderException if the reference is invalid
     */
    protected IMObject loadReference(String reference) {
        IMObject result = null;
        String objectId = references.get(reference);
        if (objectId == null) {
            Reference ref = ReferenceParser.parse(reference);
            if (ref == null) {
                throw new LoaderException(InvalidReference, reference);
            }
            if (ref.getObjectId() != null) {
                List<ETLValue> values = dao.get(ref.getObjectId());
                result = loadReference(values, reference);
            } else if (ref.getLegacyId() != null) {
                List<ETLValue> values = dao.get(ref.getLegacyId(),
                                                ref.getArchetype());
                result = loadReference(values, reference);
            } else {
                ArchetypeQuery query = new ArchetypeQuery(ref.getArchetype(),
                                                          true, true);
                query.add(new NodeConstraint(ref.getName(), ref.getValue()));
                query.setMaxResults(2);
                Iterator<IMObject> iterator
                        = new IMObjectQueryIterator<IMObject>(service, query);
                if (iterator.hasNext()) {
                    result = iterator.next();
                    if (iterator.hasNext()) {
                        throw new LoaderException(
                                RefResolvesMultipleObjects, ref.toString());
                    }
                }
            }
        } else {
            LoadState state = mapped.get(objectId);
            result = getObject(state);
        }
        return result;
    }

    /**
     * Loads an object.
     *
     * @param objectId  the object's identifier
     * @param archetype the object's archetype
     * @param values    the values to construct the object from
     * @return the object
     * @throws LoaderException for any error
     */
    protected IMObject load(String objectId, String archetype,
                            List<ETLValue> values) {
        IMObject target;
        LoadState state = mapped.get(objectId);
        if (state == null) {
            target = service.create(archetype);
            if (target == null) {
                throw new LoaderException(ArchetypeNotFound, archetype);
            }
            state = new LoadState(target, service);
            mapped.put(objectId, state);
            IMObjectBean bean = state.getBean();
            for (ETLValue value : values) {
                setValue(value, bean, objectId);
            }
            service.save(target);
            state.setNull();
        } else {
            target = getObject(state);
        }
        return target;
    }

    /**
     * Sets a value on a object.
     *
     * @param value    the value to set
     * @param bean     the bean wrapping the object
     * @param objectId the source object id
     * @throws LoaderException if the node is invalid
     */
    protected void setValue(ETLValue value, IMObjectBean bean,
                            String objectId) {
        String name = value.getName();
        NodeDescriptor descriptor = bean.getDescriptor(name);
        if (descriptor == null) {
            String archetype = bean.getObject().getArchetypeId().getShortName();
            throw new LoaderException(InvalidNode, objectId, archetype, name);
        }
        if (value.isReference()) {
            if (descriptor.isCollection()) {
                IMObject child = loadReference(value.getValue());
                bean.addValue(name, child);
            } else {
                bean.setValue(name, getReference(value.getValue()));
            }
        } else {
            bean.setValue(name, convert(value, descriptor, objectId));
        }
    }

    /**
     * Converts an {@link ETLValue} in order to populate it on an
     * {@link IMObject}. In particular, this handles stringified date/times in
     * JDBC escape format, converting them to <tt>java.util.Date</tt> instances.
     *
     * @param value      the value to convert
     * @param descriptor the node descriptor
     * @param objectId   the source object id
     * @return the converted object
     * @throws LoaderException if  the value cannot be converted
     */
    protected Object convert(ETLValue value, NodeDescriptor descriptor,
                             String objectId) {
        Object result;
        if (descriptor.isDate()) {
            if (StringUtils.isEmpty(value.getValue())) {
                result = null;
            } else {
                try {
                    result = Timestamp.valueOf(value.getValue());
                } catch (IllegalArgumentException exception) {
                    throw new LoaderException(InvalidTimestamp, objectId,
                                              value.getValue(),
                                              value.getName());
                }
            }
        } else {
            result = value.getValue();
        }
        return result;
    }

    /**
     * Returns the {@link IMObjectReference} associated with a reference.
     *
     * @param reference the reference
     * @return the corresponding {@link IMObjectReference}
     * @throws LoaderException if the reference is invalid
     */
    protected IMObjectReference getReference(String reference) {
        IMObject object = loadReference(reference);
        return object.getObjectReference();
    }

    /**
     * Gets an object, given its load state.
     *
     * @param state the load state
     * @return the object
     * @throws LoaderException           for any loader error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObject getObject(LoadState state) {
        IMObject target;
        target = state.getObject();
        if (target == null) {
            target = ArchetypeQueryHelper.getByObjectReference(
                    service, state.getRef());
        }
        if (target == null) {
            throw new LoaderException(ObjectNotFound, state.getRef());
        }
        return target;
    }

    private IMObject loadReference(List<ETLValue> values, String reference) {
        if (values.isEmpty()) {
            throw new LoaderException(ObjectNotFound, reference);
        }
        String objectId = null;
        String archetype = null;
        for (ETLValue value : values) {
            if (objectId == null) {
                objectId = value.getObjectId();
                archetype = value.getArchetype();
            } else if (!objectId.equals(value.getObjectId())) {
                throw new LoaderException(RefResolvesMultipleObjects,
                                          reference);
            } else if (!archetype.equals(value.getArchetype())) {
                throw new LoaderException(ArchetypeMismatch, objectId,
                                          archetype,
                                          value.getArchetype());
            }
        }
        IMObject result = load(objectId, archetype, values);
        references.put(reference, objectId);
        return result;
    }


}
