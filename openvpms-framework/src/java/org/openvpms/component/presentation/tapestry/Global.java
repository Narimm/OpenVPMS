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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.presentation.tapestry;

import org.openvpms.component.business.service.act.IActService;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.entity.IEntityService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.springframework.context.ApplicationContext;

/**
 * The tapestry applications Global object, common to all clients and all
 * components (pages).  Holds the spring application context and references to any necessary 
 * OpenVPMS service interfaces.    
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Global {
    
    /** the Springframework application context */
    private ApplicationContext appContext;
    
    /** the EntityService */
    private IEntityService entityService;
    
    /** the Act Service */
    private IActService actService;
    
    /** the Archetype Service */
    private IArchetypeService archetypeService;
    
    /** the Lookup Service */
    private ILookupService lookupService;
            
    /**
     * @return Returns the Springframeweork application context appContext.
     */
    public ApplicationContext getAppContext() {
        return this.appContext;
    }

    /**
     * set the Springframework application context
     * @param ac
     */
    public void setAppContext(ApplicationContext ac) {
        this.appContext = ac;
    }

    /**
     * @return Returns the archetypeService.
     */
    public IArchetypeService getArchetypeService() {
        if (archetypeService == null)
            archetypeService = (IArchetypeService)getAppContext().getBean("archetypeService");
        return archetypeService;
    }

    /**
     * @return Returns the entityService.
     */
    public IEntityService getEntityService() {
        if (entityService == null)
            entityService = (IEntityService)getAppContext().getBean("entityService");
        return entityService;
    }

    /**
     * @return Returns the actService.
     */
    public IActService getActService() {
        if (actService == null)
            actService = (IActService)getAppContext().getBean("actService");
        return actService;
    }

    /**
     * @return Returns the lookupService.
     */
    public ILookupService getLookupService() {
        if (lookupService == null)
            lookupService = (ILookupService)getAppContext().getBean("lookupService");
        return lookupService;
    }
}