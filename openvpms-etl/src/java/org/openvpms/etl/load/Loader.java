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

import static org.openvpms.etl.load.LoaderException.ErrorCode.RefResolvesMultipleObjects;
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
import org.openvpms.etl.ETLHelper;
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
     * Determines if values for lookup nodes need to be translated to
     * lookup codes.
     */
    private final boolean translateLookups;

    /**
     * Determines if validation should occur.
     */
    private final boolean validate;

    /**
     * Cache of reference strings to {@link IMObjectReference}.
     */
    private Map<String, IMObjectReference> references
            = new HashMap<String, IMObjectReference>();

    /**
     * The level of loading recursion. Objects cannot be saved unless recursion
     * is 0, as it indicates that an object is not yet complete.
     */
    private int recursion;

    /**
     * Mapped object references, keyed on {@link ETLValue#getObjectId()}.
     */
    private Map<String, IMObjectReference> mapped
            = new HashMap<String, IMObjectReference>();

    /**
     * The set of incomplete objects. Objects are considered incomplete until
     * {@link #recursion} is zero.
     */
    private final Map<IMObjectReference, IMObject> incomplete
            = new LinkedHashMap<IMObjectReference, IMObject>();

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
     * @param dao              the DAO
     * @param service          the archetype service
     * @param translateLookups if <tt>true</tt> translate values for lookup
     * @param validate         if <tt>true</tt> validate objects prior to saving
     *                         them
     */
    public Loader(ETLValueDAO dao, IArchetypeService service,
                  boolean translateLookups, boolean validate) {
        this.dao = dao;
        this.service = service;
        this.translateLookups = translateLookups;
        this.validate = validate;
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
        recursion = 0;
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
     * Loads the object associated with a reference.
     *
     * @param reference the reference to load
     * @return the object corresponding to the reference
     * @throws LoaderException if the reference is invalid
     */
    protected IMObject loadReference(String reference) {
        IMObject result;
        IMObjectReference loaded = references.get(reference);
        if (loaded == null) {
            Reference ref = ReferenceParser.parse(reference);
            if (ref == null) {
                throw new LoaderException(InvalidReference, reference);
            }
            if (ref.getObjectId() != null) {
                result = loadObjectIdReference(ref.getObjectId());
            } else if (ref.getLegacyId() != null) {
                result = loadReference(ref.getArchetype(), ref.getLegacyId());
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
                } else {
                    throw new LoaderException(IMObjectNotFound, reference);
                }
            }
        } else {
            result = getObject(loaded);
        }
        return result;
    }

    private IMObject loadReference(String archetype, String legacyId) {
        if (archetype.contains("*")) {
            // expand any wildcards, and see if the object has already been
            // loaded
            String[] shortNames = DescriptorHelper.getShortNames(archetype,
                                                                 service);
            IMObjectReference found = null;
            for (String shortName : shortNames) {
                String reference = Reference.create(shortName, legacyId);
                IMObjectReference loaded = references.get(reference);
                if (loaded != null) {
                    if (found == null) {
                        found = loaded;
                    } else {
                        throw new LoaderException(
                                RefResolvesMultipleObjects,
                                Reference.create(archetype, legacyId));
                    }
                }
            }
            if (found != null) {
                return getObject(found);
            }
        }
        List<ETLValue> values = dao.get(legacyId, archetype);
        String reference = Reference.create(archetype, legacyId);
        return loadReference(values, reference);
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
        setRecursion(recursion + 1);
        IMObject object;
        IMObjectReference reference = mapped.get(objectId);
        if (reference == null) {
            object = create(objectId, archetype, values);
            if (object == null) {
                throw new LoaderException(ArchetypeNotFound, objectId,
                                          archetype);
            }
            reference = object.getObjectReference();
            mapped.put(objectId, reference);
            incomplete.put(reference, object);
            IMObjectBean bean = new IMObjectBean(object, service);
            for (ETLValue value : values) {
                setValue(value, bean, objectId);
            }
        } else {
            object = getObject(reference);
        }
        setRecursion(recursion - 1);
        return object;
    }

    /**
     * Saves a set of mapped objects.
     *
     * @param objects  the objects to save
     * @param validate if <tt>true</tt> validate objects prior to saving them
     * @throws ArchetypeServiceException for any error
     */
    protected void save(Collection<IMObject> objects, boolean validate) {
        if (!validate) {
            // validation normally does derivation of values, so when not
            // validating, need to do it explicitly
            for (IMObject object : objects) {
                getService().deriveValues(object);
            }
        }
        service.save(objects, validate);
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
    protected IMObject create(String objectId, String archetype,
                              List<ETLValue> values) {
        IMObject result;
        result = service.create(archetype);
        if (result != null) {
            boolean remove = false;
            for (ETLValue value : values) {
                if (value.getRemoveDefaultObjects()) {
                    remove = true;
                    break;
                }
            }
            if (remove) {
                ArchetypeDescriptor descriptor
                        = DescriptorHelper.getArchetypeDescriptor(result,
                                                                  service);
                for (NodeDescriptor node : descriptor.getAllNodeDescriptors()) {
                    if (node.isCollection()) {
                        IMObject[] children = node.getChildren(result).toArray(
                                new IMObject[0]);
                        for (IMObject child : children) {
                            node.removeChildFromCollection(result, child);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Sets the current recursion level. If the level is <tt>0</tt>,
     * moves all incomplete objects to the save batch.
     *
     * @param level the recursion level
     */
    private void setRecursion(int level) {
        recursion = level;
        if (recursion <= 0) {
            batch.putAll(incomplete);
            incomplete.clear();
            if (batch.size() >= batchSize) {
                flushBatch();
            }
        }
    }

    /**
     * Saves the current batch of objects.
     *
     * @throws ArchetypeServiceException for any error
     */
    private void flushBatch() {
        save(batch.values(), validate);
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
        } else if (descriptor.isLookup() && translateLookups
                && !StringUtils.isEmpty(value.getValue())) {
            result = ETLHelper.getLookupCode(value.getValue());
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
     * Gets an object, given its reference.
     *
     * @param reference the object reference
     * @return the object
     * @throws LoaderException           if the object cannot be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private IMObject getObject(IMObjectReference reference) {
        // determine if the object is complete
        IMObject object = incomplete.get(reference);
        if (object == null) {
            // determine if the object is pending save
            object = batch.get(reference);
            if (object == null) {
                // try and load from the database
                object = ArchetypeQueryHelper.getByObjectReference(
                        service, reference);
                if (object == null) {
                    throw new LoaderException(IMObjectNotFound, reference);
                }
            }
        }
        return object;
    }

    /**
     * Loads a reference by object identifier.
     *
     * @param objectId the object identifier
     * @return the object corresponding to the reference
     * @throws LoaderException           for any loader error
     * @throws ArchetypeServiceException for any archetype service error
     */
    private IMObject loadObjectIdReference(String objectId) {
        IMObject result;
        IMObjectReference reference = mapped.get(objectId);
        if (reference != null) {
            result = getObject(reference);
            references.put(objectId, reference);
        } else {
            List<ETLValue> values = dao.get(objectId);
            result = loadReference(values, objectId);
        }
        return result;
    }

    /**
     * Loads a reference.
     *
     * @param values    the values associated with the reference
     * @param reference the reference
     * @return the associated object
     * @throws LoaderException           for any loader error
     * @throws ArchetypeServiceException for any archetype service error
     */
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
        references.put(reference, result.getObjectReference());
        return result;
    }

}
