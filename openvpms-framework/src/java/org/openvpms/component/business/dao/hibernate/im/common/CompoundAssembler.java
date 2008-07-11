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

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class CompoundAssembler implements Assembler {

    private Map<Class<? extends IMObject>, Assembler> doAssemblers
            = new HashMap<Class<? extends IMObject>, Assembler>();

    private Map<Class<? extends IMObjectDO>, Assembler> assemblers
            = new HashMap<Class<? extends IMObjectDO>, Assembler>();


    private Map<Class<? extends IMObject>, Class<? extends IMObjectDO>>
            typeDOMap = new HashMap<Class<? extends IMObject>,
            Class<? extends IMObjectDO>>();

    private Map<String, String> typeDONameMap = new HashMap<String, String>();
    private Map<String, String> doTypeNameMap = new HashMap<String, String>();

    public CompoundAssembler() {
    }

    public Class<? extends IMObjectDO> getDOClass(
            Class<? extends IMObject> type) {
        return typeDOMap.get(type);

    }

    public String getDOClassName(String className) {
        return typeDONameMap.get(className);
    }

    public String getClassName(String doClassName) {
        return doTypeNameMap.get(doClassName);
    }

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

    public DOState assemble(IMObject source, Context context) {
        Assembler assembler = getAssembler(source);
        return assembler.assemble(source, context);
    }

    public IMObject assemble(IMObjectDO source, Context context) {
        Assembler assembler = getAssembler(source);
        return assembler.assemble(source, context);
    }

    public DOState assemble(IMObjectDO target, IMObject source,
                            Context context) {
        Assembler assembler = getAssembler(source);
        return assembler.assemble(target, source, context);
    }

    public IMObject assemble(IMObject target, IMObjectDO source,
                             Context context) {
        Assembler assembler = getAssembler(source);
        return assembler.assemble(target, source, context);
    }

    private Assembler getAssembler(IMObject source) {
        Assembler assembler = doAssemblers.get(source.getClass());
        if (assembler == null) {
            throw new IllegalArgumentException(
                    "Unsupported type: " + source.getClass().getName());
        }
        return assembler;
    }

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

}

