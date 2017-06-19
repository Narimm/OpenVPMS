package org.openvpms.plugin.service.internal.archetype;

import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.DelegatingArchetypeService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.business.service.security.RunAs;
import org.openvpms.plugin.service.archetype.PluginArchetypeService;

import java.util.Collection;

/**
 * .
 *
 * @author Tim Anderson
 */
public class PluginArchetypeServiceImpl implements PluginArchetypeService {

    private final IArchetypeService service;
    private final PracticeService practiceService;

    private final IArchetypeService writable;

    public PluginArchetypeServiceImpl(IArchetypeRuleService service, PracticeService practiceService) {
        this.service = service;
        this.practiceService = practiceService;
        writable = new DelegatingArchetypeService(service) {
            @Override
            public void save(final IMObject object) {
                run(new Runnable() {
                    @Override
                    public void run() {
                        PluginArchetypeServiceImpl.this.save(object);
                    }
                });
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
    public IMObjectBean getBean(IMObject object) {
        return new IMObjectBean(object, writable);
    }

    /**
     * Returns a bean for an act.
     *
     * @param object the object
     * @return the bean
     */
    @Override
    public ActBean getBean(Act object) {
        return new ActBean(object, writable);
    }

    /**
     * Returns a bean for an entity.
     *
     * @param object the object
     * @return the bean
     */
    @Override
    public EntityBean getBean(Entity object) {
        return new EntityBean(object, writable);
    }

    /**
     * Create a domain object given its archetype.
     *
     * @param archetype the archetype name
     * @return a new object, or {@code null} if there is no corresponding archetype descriptor for {@code shortName}
     */
    @Override
    public IMObject create(String archetype) {
        return service.create(archetype);
    }

    /**
     * Saves an object, executing any <em>save</em> rules associated with its archetype.
     *
     * @param object the object to save
     */
    @Override
    public void save(final IMObject object) {
        run(new Runnable() {
            @Override
            public void run() {
                service.save(object);
            }
        });
    }

    /**
     * Save a collection of {@link IMObject} instances.
     *
     * @param objects the objects to insert or update
     */
    @Override
    public void save(final Collection<? extends IMObject> objects) {
        run(new Runnable() {
            @Override
            public void run() {
                service.save(objects);
            }
        });
    }

    /**
     * Remove the specified object.
     *
     * @param object the object to remove
     */
    @Override
    public void remove(final IMObject object) {
        run(new Runnable() {
            @Override
            public void run() {
                service.remove(object);
            }
        });
    }

    /**
     * Validates an object.
     *
     * @param object the object to validate
     */
    @Override
    public void validate(IMObject object) {
        service.validateObject(object);
    }

    /**
     * Returns an object given its reference.
     *
     * @param reference the reference
     * @return the object, or {@code null} if none is found
     */
    @Override
    public IMObject get(IMObjectReference reference) {
        return service.get(reference);
    }

    private void run(Runnable runnable) {
        User user = practiceService.getServiceUser();
        if (user == null) {
            throw new IllegalStateException(
                    "Cannot invoke PluginArchetypeService operation as no Service User has been configured");
        }
        RunAs.run(user, runnable);
    }

}
