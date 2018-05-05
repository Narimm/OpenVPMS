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

package org.openvpms.plugin.internal.service.archetype;

import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.DelegatingArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.business.service.security.RunAs;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.service.archetype.ArchetypeService;
import org.openvpms.component.service.archetype.ValidationError;

import java.util.Collection;
import java.util.List;

/**
 * Implementation of the {@link ArchetypeService} for plugins.
 *
 * @author Tim Anderson
 */
public class PluginArchetypeService implements ArchetypeService {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * Archetype service that handles setting up a user context for save operations.
     */
    private final IArchetypeService writable;

    /**
     * Constructs a {@link PluginArchetypeService}.
     *
     * @param service         the archetype service
     * @param lookups         the lookup service
     * @param practiceService the practice service
     */
    public PluginArchetypeService(IArchetypeRuleService service, ILookupService lookups,
                                  PracticeService practiceService) {
        this.service = service;
        this.lookups = lookups;
        this.practiceService = practiceService;
        writable = new DelegatingArchetypeService(service) {
            @Override
            public void save(org.openvpms.component.business.domain.im.common.IMObject object) {
                PluginArchetypeService.this.save(object);
            }

            @Override
            public void save(Collection<? extends org.openvpms.component.business.domain.im.common.IMObject> objects) {
                PluginArchetypeService.this.save(objects);
            }

            @Override
            public org.openvpms.component.business.domain.im.common.IMObject create(String shortName) {
                return (org.openvpms.component.business.domain.im.common.IMObject)
                        PluginArchetypeService.this.create(shortName);
            }
        };
    }

    /**
     * Returns the {@link ArchetypeDescriptor} for the given archetype.
     *
     * @param archetype the archetype
     * @return the descriptor corresponding to the archetype, or {@code null} if none is found
     */
    @Override
    public ArchetypeDescriptor getArchetypeDescriptor(String archetype) {
        return service.getArchetypeDescriptor(archetype);
    }

    /**
     * Returns a bean for an object.
     *
     * @param object the object
     * @return the bean
     */
    @Override
    public IMObjectBean getBean(org.openvpms.component.model.object.IMObject object) {
        return new IMObjectBean(object, writable, lookups);
    }

    /**
     * Create a domain object given its archetype.
     *
     * @param archetype the archetype name
     * @return a new object, or {@code null} if there is no corresponding archetype descriptor for {@code shortName}
     */
    @Override
    public IMObject create(final String archetype) {
        final IMObject[] result = new IMObject[1];
        run(() -> result[0] = service.create(archetype));
        return result[0];
    }

    /**
     * Saves an object, executing any <em>save</em> rules associated with its archetype.
     *
     * @param object the object to save
     */
    @Override
    public void save(final IMObject object) {
        run(() -> service.save((org.openvpms.component.business.domain.im.common.IMObject) object));
    }

    /**
     * Save a collection of {@link IMObject} instances.
     *
     * @param objects the objects to insert or update
     */
    @Override
    @SuppressWarnings("unchecked")
    public void save(final Collection<? extends IMObject> objects) {
        run(() -> service.save((Collection<org.openvpms.component.business.domain.im.common.IMObject>)
                                       (Collection) objects));
    }

    /**
     * Remove the specified object.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(final IMObject object) {
        run(() -> service.remove((org.openvpms.component.business.domain.im.common.IMObject) object));
    }

    /**
     * Validates an object.
     *
     * @param object the object to validate
     * @return a list of validation errors, if any
     */
    @Override
    public List<ValidationError> validate(IMObject object) {
        return service.validate((org.openvpms.component.business.domain.im.common.IMObject) object);
    }

    /**
     * Returns an object given its reference.
     *
     * @param reference the reference
     * @return the object, or {@code null} if none is found
     */
    @Override
    public IMObject get(Reference reference) {
        return service.get(reference);
    }

    private void run(Runnable runnable) {
        User user = practiceService.getServiceUser();
        if (user == null) {
            throw new IllegalStateException(
                    "Cannot invoke ArchetypeService operation as no Service User has been configured");
        }
        RunAs.run(user, runnable);
    }

}
