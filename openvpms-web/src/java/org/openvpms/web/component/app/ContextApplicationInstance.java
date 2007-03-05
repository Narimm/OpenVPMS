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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.app;

import nextapp.echo2.app.ApplicationInstance;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.web.system.SpringApplicationInstance;

import java.util.Iterator;
import java.util.List;


/**
 * An <code>ApplicationInstance</code> associated with a {@link GlobalContext}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ContextApplicationInstance
        extends SpringApplicationInstance {

    /**
     * Application context.
     */
    private GlobalContext context = new GlobalContext();


    /**
     * Constructs a new <code>ContextApplicationInstance</code>.
     */
    public ContextApplicationInstance() {
        initUser();
        initPractice();
        initLocation();
    }

    /**
     * Returns the instance associated with the current thread.
     *
     * @return the current instance, or <code>null</code>
     */
    public static ContextApplicationInstance getInstance() {
        return (ContextApplicationInstance) ApplicationInstance.getActive();
    }

    /**
     * Returns the current context.
     *
     * @return the current context
     */
    public GlobalContext getContext() {
        return context;
    }

    /**
     * Switches the current workspace to display an object.
     *
     * @param object the object to view
     */
    public abstract void switchTo(IMObject object);

    /**
     * Clears the current context.
     */
    protected void clearContext() {
        context = new GlobalContext();
    }

    /**
     * Initialises the user.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void initUser() {
        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            ArchetypeQuery query = new ArchetypeQuery("security.user",
                                                      true, true);
            query.add(new NodeConstraint("username", auth.getName()));
            query.setMaxResults(1);
            Iterator<User> iterator = new IMObjectQueryIterator<User>(query);
            if (iterator.hasNext()) {
                context.setUser(iterator.next());
            }
        }
    }

    /**
     * Initialises the practice.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void initPractice() {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        // First get the Practice.  Should only be one but get first if more.
        List<IMObject> rows = ArchetypeQueryHelper.get(
                service, "party", "party", "organisationPractice", null, true,
                0, 1).getResults();
        if (!rows.isEmpty()) {
            Party practice = (Party) rows.get(0);
            context.setPractice(practice);
        }
    }

    /**
     * Initialises the location specific information.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void initLocation() {
        IArchetypeService service
        = ArchetypeServiceHelper.getArchetypeService();
        
        // Get the current Practice
        Party practice = context.getPractice();
        
        // Get the current user.
        User user = context.getUser();
        
        // If pactice and/or user not set then exit.
        if (practice == null || user == null) {        	
        	return;
        }
        
        // Now get the default location for the user or the first location if no default. 
        IMObjectBean bean = new IMObjectBean(user, service);
        List<IMObject> relationships = bean.getValues("locations");
        Party location = (Party)getDefaultRelationship(relationships, service);

        // If no locations defined for user find default location for Practice or the first location if no default.
        if (location == null) {
            bean = new IMObjectBean(practice, service);
            relationships = bean.getValues("locations");
            location = (Party)getDefaultRelationship(relationships, service);
        }
        
        // If no location then return
        if (location == null) {
        	return;
        }
        context.setLocation(location);
        
        // Now get the default Deposit object.
        bean = new IMObjectBean(location, service);
        relationships = bean.getValues("depositAccounts");
        context.setTill((Party)getDefaultRelationship(relationships, service));
        
        // Now get the default Till object.
        bean = new IMObjectBean(location, service);
        relationships = bean.getValues("tills");
        context.setTill((Party)getDefaultRelationship(relationships, service));

        // Now get the default Schedule object.
        bean = new IMObjectBean(location, service);
        relationships = bean.getValues("schedules");
        context.setSchedule((Party)getDefaultRelationship(relationships, service));
               
        // Now get the default WorkList object.
        bean = new IMObjectBean(location, service);
        relationships = bean.getValues("workLists");
        context.setWorkList((Party)getDefaultRelationship(relationships, service));        
    }

    /**
     * Returns the target object from the default entityRelationship from the supplied
     * relationship list.  If no default it returns the target object for the first relationship found.
     *
     * @param relationships a list of relationship objects
     * @param service the archetype service
     * @return the default or the first target object or null if neither
     */

    private IMObject getDefaultRelationship(List<IMObject> relationships, IArchetypeService service) {
    	IMObject firstRelationship = null;
    	IMObject defaultRelationship = null;
        for (IMObject object : relationships) {
            EntityRelationship relationship = (EntityRelationship) object;
            IMObjectBean relationshipBean = new IMObjectBean(relationship,service);
            IMObjectReference locationRef = relationship.getTarget();
            if (locationRef != null) {
                defaultRelationship = (Party)ArchetypeQueryHelper.getByObjectReference(
                        service, locationRef);
                if (firstRelationship == null)
                	firstRelationship = defaultRelationship;
            }
            if (relationshipBean.hasNode("default") && relationshipBean.getBoolean("default",false)) {           	
                break;
            }
        }
        if (defaultRelationship == null) {
        	return firstRelationship;
        }
        else
        	return defaultRelationship;
    }
}
