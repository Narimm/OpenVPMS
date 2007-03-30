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
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    private final ETLValueDAO dao;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Reference to object id mapping.
     */
    private Map<String, String> references = new HashMap<String, String>();

    /**
     * The mapped objects, keyed on {@link ETLValue#getObjectId()}.
     * Objects that are only partially mapped non-null {@link LoadState#getObject()}
     * and {@link LoadState#getBean()} values. These are set to null once the
     * object has been completely processed.
     */
    private Map<String, LoadState> mapped = new HashMap<String, LoadState>();

    /**
     * The batch of unsaved objects.
     */
    private final Map<IMObjectReference, IMObject> batch
            = new LinkedHashMap<IMObjectReference, IMObject>();

    /**
     * The batch size.
     */
    private final int batchSize = 100;


    /**
     * Constructs a new <tt>Loader</tt>.
     *
     * @param dao the DAO
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
        flushBatch();
        int count = mapped.size();
        mapped.clear();
        references.clear();
        return count;
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
     * @throws LoaderException           for any error
     * @throws ArchetypeServiceException for any archetype service error
     */
    protected IMObject load(String objectId, String archetype,
                            List<ETLValue> values) {
        IMObject target;
        LoadState state = mapped.get(objectId);
        if (state == null) {
            state = create(objectId, archetype, values);
            if (state == null) {
                throw new LoaderException(ArchetypeNotFound, objectId,
                                          archetype);
            }
            IMObjectBean bean = state.getBean();
            for (ETLValue value : values) {
                setValue(value, bean, objectId);
            }
            target = bean.getObject();
            queue(objectId, state.getRef(), target);
            state.setNull();
        } else {
            target = getObject(state);
        }
        return target;
    }

    /**
     * Queues an object to be saved, flushing the batch if the batch size is
     * reached.
     *
     * @param objectId  the source object identifier
     * @param reference the object reference
     * @param target    the object to queue
     * @throws ArchetypeServiceException for any error
     */
    protected void queue(String objectId, IMObjectReference reference,
                         IMObject target) {
        batch.put(reference, target);
        if (batch.size() >= batchSize) {
            flushBatch();
        }
    }

    /**
     * Saves a set of mapped objects.
     *
     * @param objects the objects to save
     * @throws ArchetypeServiceException for any error
     */
    protected void save(Collection<IMObject> objects) {
        service.save(objects);
    }

    /**
     * Determines if an object has been loaded.
     *
     * @param objectId the object identifier
     * @return <tt>true</tt> if the object has been loaded
     */
    protected boolean isLoaded(String objectId) {
        return mapped.containsKey(objectId);
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
     * Creates a new {@link IMObject}.
     *
     * @param objectId  the object's identifier
     * @param archetype the object's archetype
     * @param values    the values associated with the object, used to determine
     *                  if default child objects should be removed
     * @return a new object, or <tt>null</tt> if there is no corresponding
     *         archetype descriptor for <tt>archetype</tt>
     * @throws ArchetypeServiceException if the object can't be created
     */
    protected LoadState create(String objectId, String archetype,
                               List<ETLValue> values) {
        LoadState state = null;
        IMObject target;
        target = service.create(archetype);
        if (target != null) {
            boolean remove = false;
            for (ETLValue value : values) {
                if (value.getRemoveDefaultObjects()) {
                    remove = true;
                    break;
                }
            }
            if (remove) {
                ArchetypeDescriptor descriptor
                        = DescriptorHelper.getArchetypeDescriptor(target,
                                                                  service);
                for (NodeDescriptor node : descriptor.getAllNodeDescriptors()) {
                    if (node.isCollection()) {
                        IMObject[] children = node.getChildren(target).toArray(
                                new IMObject[0]);
                        for (IMObject child : children) {
                            node.removeChildFromCollection(target, child);
                        }
                    }
                }
            }
            state = new LoadState(target, service);
            mapped.put(objectId, state);
        }
        return state;
    }

    /**
     * Saves the current batch of objects.
     *
     * @throws ArchetypeServiceException for any error
     */
    private void flushBatch() {
        save(batch.values());
        batch.clear();
    }

    /**
     * Sets a value on a object.
     *
     * @param value    the value to set
     * @param bean     the bean wrapping the object
     * @param objectId the source object id
     * @throws LoaderException if the node is invalid
     */
    private void setValue(ETLValue value, IMObjectBean bean,
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
    private Object convert(ETLValue value, NodeDescriptor descriptor,
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
    private IMObjectReference getReference(String reference) {
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
    private IMObject getObject(LoadState state) {
        IMObject target;
        target = state.getObject();
        if (target == null) {
            target = batch.get(state.getRef());  // see if it needs to be saved
            if (target == null) {
                target = ArchetypeQueryHelper.getByObjectReference(
                        service, state.getRef());
                if (target == null) {
                    throw new LoaderException(IMObjectNotFound, state.getRef());
                }
            }
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
