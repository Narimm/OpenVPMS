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

package org.openvpms.component.presentation.tapestry.page;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.PageRedirectException;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageDetachListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.util.DefaultPrimaryKeyConverter;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.presentation.tapestry.Visit;
import org.openvpms.component.presentation.tapestry.callback.SearchCallback;
import org.openvpms.component.presentation.tapestry.component.Utils;



/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public abstract class SearchPage extends OpenVpmsPage implements PageBeginRenderListener, PageDetachListener
{
    @Persist("session")
    public abstract String getArchetypeRange();
    public abstract void setArchetypeRange(String archetypeRange);  

    @Persist("session")
    public abstract String getDefaultArchetypeName();
    public abstract void setDefaultArchetypeName(String archetypeName);  

    private DefaultPrimaryKeyConverter _converter;

    public DefaultPrimaryKeyConverter getConverter()
    {
        if (_converter == null)
            _converter = new DefaultPrimaryKeyConverter()
            {
                // Here's why we DON'T use @Bean ...

                @Override
                protected Object provideMissingValue(Object key)
                {
                    throw new PageRedirectException(SearchPage.this);
                }

            };

        return _converter;
    }

    public void pageBeginRender(PageEvent event)
    {
        readRecords();
    }

    public void pageDetached(PageEvent event)
    {
        _converter = null;
    }


    public void pushCallback()
    {
        Visit visit = (Visit)getVisitObject();
        visit.getCallbackStack().push(new SearchCallback(getPageName()));
    }
    
    /**
     * Reads all the records from the database, building the list of record ids, and the map from
     * id to Record. 
     */

    private void readRecords()
    {
        //* get the archetype range tokens
        String range = getArchetypeRange();
        StringTokenizer tokens = new StringTokenizer(range,  ".");
        if (tokens.countTokens() != 3)
            return;
        String rmName = tokens.nextToken();
//        String entityName = tokens.nextToken();
//        String conceptName = tokens.nextToken();

        DefaultPrimaryKeyConverter converter = getConverter();
        List results = null;
        try {
            if (rmName.equalsIgnoreCase("party"))
                results = getEntityService().get(rmName,null, null, null);
            else if (rmName.equalsIgnoreCase("lookup"))
                results = getLookupService().get("lookup.country");
            for (Object object : results)
            {
                converter.add(((IMObject)object).getUid(), object);
            }
        }
        catch (Exception e) {
            converter.clear();             
        }
    }
    
    public void onNameClick(Object model)
    {
        // TODO Retieve ID from model and the utilise this to retrieve object using service layer.  This 
        // will solve problem with stale object sin Search List.
        EditPage page = (EditPage) Utils.findPage(getRequestCycle(), ((IMObject)model).getArchetypeId().getShortName(),"Edit");
        page.setModel(model);
        page.setArchetypeRange(getArchetypeRange());
        page.setCurrentArchetypeName(((IMObject)model).getArchetypeId().getShortName());
        this.pushCallback();
        getRequestCycle().activate(page);
    }

    public void onNewClick(IRequestCycle cycle)
    {
        EditPage page = (EditPage) Utils.findPage(getRequestCycle(), getDefaultArchetypeName(),"Edit");
        page.setModel(getArchetypeService().createDefaultObject(getDefaultArchetypeName()));
        page.setArchetypeRange(getArchetypeRange());
        page.setCurrentArchetypeName(getDefaultArchetypeName());
        this.pushCallback();
        getRequestCycle().activate(page);
    }
}
