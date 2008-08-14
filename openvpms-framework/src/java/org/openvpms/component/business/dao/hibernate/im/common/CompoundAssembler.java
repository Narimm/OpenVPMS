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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.common;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of the {@link Assembler} interface that supports registration
 * of {@link IMObjectAssembler}s for different data object types.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class CompoundAssembler implements Assembler {

    /**
     * The assemblers, keyed on object type.
     */
    private Map<Class<? extends IMObject>, Assembler> doAssemblers
            = new HashMap<Class<? extends IMObject>, Assembler>();

    /**
     * The assemblers, keyed on data object type.
     */
    private Map<Class<? extends IMObjectDO>, Assembler> assemblers
            = new HashMap<Class<? extends IMObjectDO>, Assembler>();

    /**
     * A map of object types to data object types.
     */
    private Map<Class<? extends IMObject>, Class<? extends IMObjectDO>>
            typeDOMap = new HashMap<Class<? extends IMObject>,
            Class<? extends IMObjectDO>>();

    /**
     * Map of object type names to data object type names.
     */
    private Map<String, String> typeDONameMap = new HashMap<String, String>();

    /**
     * Map of data object type names to object type names.
     */
    private Map<String, String> doTypeNameMap = new HashMap<String, String>();


    /**
     * Returns the data object class for the specified object type.
     *
     * @param type the object type
     * @return the corresponding data object class, or <tt>null</tt> if none is
     *         found
     */
    public Class<? extends IMObjectDO> getDOClass(
            Class<? extends IMObject> type) {
        return typeDOMap.get(type);
    }

    /**
     * Returns the data object class name for the specified {@link IMObject}
     * class name.
     *
     * @param className the object class name
     * @return the corresponding data object class name, or <tt>null</tt> if
     *         none is found
     */
    public String getDOClassName(String className) {
        return typeDONameMap.get(className);
    }

    /**
     * Returns the object class name for the specified {@link IMObjectDO} class
     * name.
     *
     * @param doClassName the data object class name
     * @return the corresponding object class name, or <tt>null</tt> if none is
     *         found
     */
    public String getClassName(String doClassName) {
        return doTypeNameMap.get(doClassName);
    }

    /**
     * Registers an assembler.
     *
     * @param assembler the assembler
     */
    public void addAssembler(IMObjectAssembler<? extends IMObject,
            ? extends IMObjectDO> assembler) {
        doAssemblers.put(assembler.getType(), assembler);
        assemblers.put(assembler.getDOType(), assembler);
        String typeName = assembler.getType().getName();
        String doTypeName = assembler.getDOType().getName();

        typeDOMap.put(assembler.getType(), assembler.getDOType());
        typeDONameMap.put(typeName, doTypeName);
        doTypeNameMap.put(doTypeName, typeName);
    }

    /**
     * Assembles an {@link IMObjectDO} from an {@link IMObject}.
     *
     * @param source  the object to assemble from
     * @param context the assembly context
     * @return the assembled object
     */
    public DOState assemble(IMObject source, Context context) {
        Assembler assembler = getAssembler(source);
        return assembler.assemble(source, context);
    }

    /**
     * Assembles an {@link IMObjectDO} from an {@link IMObject}.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     * @return the assembled object
     */
    public DOState assemble(IMObjectDO target, IMObject source,
                            Context context) {
        target = deproxy(target);
        Assembler assembler = getAssembler(source);
        return assembler.assemble(target, source, context);
    }

    /**
     * Assembles an {@link IMObject} from an {@link IMObjectDO}.
     *
     * @param source  the object to assemble from
     * @param context the assembly context
     * @return the assembled object
     */
    public IMObject assemble(IMObjectDO source, Context context) {
        source = deproxy(source);
        Assembler assembler = getAssembler(source);
        return assembler.assemble(source, context);
    }

    /**
     * Assembles an {@link IMObject} from an {@link IMObjectDO}.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     * @return the assembled object
     */
    public IMObject assemble(IMObject target, IMObjectDO source,
                             Context context) {
        source = deproxy(source);
        Assembler assembler = getAssembler(source);
        return assembler.assemble(target, source, context);
    }

    /**
     * Returns an assembler for the specified object.
     *
     * @param source the object to assemble from
     * @return the assembler
     * @throws IllegalArgumentException if <tt>source</tt> is invalid
     */
    private Assembler getAssembler(IMObject source) {
        Assembler assembler = doAssemblers.get(source.getClass());
        if (assembler == null) {
            throw new IllegalArgumentException(
                    "Unsupported type: " + source.getClass().getName());
        }
        return assembler;
    }

    /**
     * Returns an assembler for the specified object.
     *
     * @param source the object to assemble from
     * @return the assembler
     * @throws IllegalArgumentException if <tt>source</tt> is invalid
     */
    private Assembler getAssembler(IMObjectDO source) {
        Assembler assembler = assemblers.get(source.getClass());
        if (assembler == null) {
            Class<? extends IMObjectDO> bestMatch = null;
            for (Map.Entry<Class<? extends IMObjectDO>, Assembler> entry
                    : assemblers.entrySet()) {
                Class<? extends IMObjectDO> type = entry.getKey();
                if (type.isAssignableFrom(source.getClass())) {
                    if (bestMatch == null || bestMatch.isAssignableFrom(type)) {
                        bestMatch = type;
                        assembler = entry.getValue();
                    }
                }
            }
            if (assembler == null) {
                throw new IllegalArgumentException(
                        "Unsupported type: " + source.getClass().getName());
            }
        }
        return assembler;
    }

    /**
     * Helper to deproxy an object if required.
     *
     * @param object the potentially proxied object
     * @return the deproxied object, or <tt>object</tt> if it wasn't proxied
     */
    private IMObjectDO deproxy(IMObjectDO object) {
        if (object instanceof HibernateProxy) {
            HibernateProxy proxy = ((HibernateProxy) object);
            LazyInitializer init = proxy.getHibernateLazyInitializer();
            object = (IMObjectDO) init.getImplementation();
        }
        return object;
    }

}