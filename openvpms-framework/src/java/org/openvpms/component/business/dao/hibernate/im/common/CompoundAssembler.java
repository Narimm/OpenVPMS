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

package org.openvpms.component.business.dao.hibernate.im.common;

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Implementation of the {@link Assembler} interface that supports registration
 * of {@link IMObjectAssembler}s for different data object types.
 *
 * @author Tim Anderson
 */
public abstract class CompoundAssembler extends AbstractAssembler implements Assembler {

    /**
     * The assemblers, keyed on object type.
     */
    private Map<Class<? extends IMObject>, Assembler> doAssemblers = new HashMap<>();

    /**
     * The assemblers, keyed on data object type.
     */
    private Map<Class<? extends IMObjectDO>, Assembler> assemblers = new HashMap<>();

    /**
     * Map of object and object implementation types to data object implementation types.
     */
    private Map<Class<? extends org.openvpms.component.model.object.IMObject>, Class<? extends IMObjectDOImpl>>
            typeImplMap = new HashMap<>();

    /**
     * Map of object type names to data object implementation type names.
     */
    private Map<String, String> typeDONameMap = new HashMap<>();

    /**
     * Returns the data object class name for the specified {@link IMObject}
     * class name.
     *
     * @param className the object class name
     * @return the corresponding data object class name, or {@code null} if
     * none is found
     */
    public String getDOClassName(String className) {
        return typeDONameMap.get(className);
    }

    /**
     * Returns the data object class for the specified {@link IMObject} class.
     *
     * @param type the object class name
     * @return the corresponding data object class, or {@code null} if none is found
     */
    public <T extends org.openvpms.component.model.object.IMObject> Class<? extends IMObjectDOImpl> getDOClass(
            Class<T> type) {
        return typeImplMap.get(type);
    }

    /**
     * Registers an assembler.
     *
     * @param assembler the assembler
     */
    public void addAssembler(IMObjectAssembler<? extends IMObject, ? extends IMObjectDO> assembler) {
        doAssemblers.put(assembler.getTypeImpl(), assembler);
        assemblers.put(assembler.getDOType(), assembler);
        assemblers.put(assembler.getDOImplType(), assembler);
        Class<? extends org.openvpms.component.model.object.IMObject> type = assembler.getType();
        if (type != null) {
            typeImplMap.put(type, assembler.getDOImplType());
        }
        typeImplMap.put(assembler.getTypeImpl(), assembler.getDOImplType());
        typeDONameMap.put(assembler.getTypeImpl().getName(), assembler.getDOType().getName());
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
    public DOState assemble(IMObjectDO target, IMObject source, Context context) {
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
    public IMObject assemble(IMObject target, IMObjectDO source, Context context) {
        source = deproxy(source);
        Assembler assembler = getAssembler(source);
        return assembler.assemble(target, source, context);
    }

    /**
     * Returns an assembler for the specified object.
     *
     * @param source the object to assemble from
     * @return the assembler
     * @throws IllegalArgumentException if {@code source} is invalid
     */
    private Assembler getAssembler(IMObject source) {
        Assembler assembler = doAssemblers.get(source.getClass());
        if (assembler == null) {
            throw new IllegalArgumentException("Unsupported type: " + source.getClass().getName());
        }
        return assembler;
    }

    /**
     * Returns an assembler for the specified object.
     *
     * @param source the object to assemble from
     * @return the assembler
     * @throws IllegalArgumentException if {@code source} is invalid
     */
    private Assembler getAssembler(IMObjectDO source) {
        Assembler assembler = assemblers.get(source.getClass());
        if (assembler == null) {
            Class<? extends IMObjectDO> bestMatch = null;
            for (Map.Entry<Class<? extends IMObjectDO>, Assembler> entry : assemblers.entrySet()) {
                Class<? extends IMObjectDO> type = entry.getKey();
                if (type.isAssignableFrom(source.getClass())) {
                    if (bestMatch == null || bestMatch.isAssignableFrom(type)) {
                        bestMatch = type;
                        assembler = entry.getValue();
                    }
                }
            }
            if (assembler == null) {
                throw new IllegalArgumentException("Unsupported type: " + source.getClass().getName());
            }
        }
        return assembler;
    }

}