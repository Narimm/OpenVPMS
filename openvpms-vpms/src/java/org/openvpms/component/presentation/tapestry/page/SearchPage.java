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

// java-core
import java.util.List;
import java.util.StringTokenizer;

// commons-lang
import org.apache.commons.lang.StringUtils;

// jakarta-tapestry
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.PageRedirectException;
import org.apache.tapestry.annotations.Persist;
import org.apache.tapestry.event.PageBeginRenderListener;
import org.apache.tapestry.event.PageDetachListener;
import org.apache.tapestry.event.PageEvent;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.apache.tapestry.util.DefaultPrimaryKeyConverter;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.presentation.tapestry.Visit;
import org.openvpms.component.presentation.tapestry.callback.SearchCallback;
import org.openvpms.component.presentation.tapestry.component.ArchetypeNameSelectionModel;
import org.openvpms.component.presentation.tapestry.component.Utils;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class SearchPage extends OpenVpmsPage implements
        PageBeginRenderListener, PageDetachListener {
    @Persist("session")
    public abstract String getArchetypeRange();

    /**
     * 
     * @param archetypeRange
     */
    public abstract void setArchetypeRange(String archetypeRange);
    
    /**
     * 
     * @return
     */
    public abstract String getArchetypeName();

    /**
     * 
     * @return
     */
    public abstract String getSearchName();

    /**
     * 
     */
    private DefaultPrimaryKeyConverter _converter;

    /**
     * 
     * @return
     */
    public DefaultPrimaryKeyConverter getConverter() {
        if (_converter == null)
            _converter = new DefaultPrimaryKeyConverter() {
                // Here's why we DON'T use @Bean ...

                @Override
                protected Object provideMissingValue(Object key) {
                    throw new PageRedirectException(SearchPage.this);
                }

            };

        return _converter;
    }

    /**
     * 
     */
    public void pageBeginRender(PageEvent event) {
        readRecords();
    }

    /**
     * 
     */
    public void pageDetached(PageEvent event) {
        _converter = null;
    }

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public void pushCallback() {
        Visit visit = (Visit) getVisitObject();
        visit.getCallbackStack().push(new SearchCallback(getPageName()));
    }

    /**
     * Reads all the records from the database, building the list of record ids,
     * and the map from id to Record.
     */
    private void readRecords() {
        if (getRequestCycle().isRewinding())
            return;

        // * get the archetype range tokens
        String range = getArchetypeRange();
        StringTokenizer rangetokens = new StringTokenizer(range, ".");
        String rmName = rangetokens.nextToken();
        String entityName = rangetokens.nextToken();
        String conceptName = rangetokens.nextToken();
        String name = getSearchName();
        if (StringUtils.isEmpty(name) == false)
            name = name + "*";
        String type = getArchetypeName();

        List results = null;
        DefaultPrimaryKeyConverter converter = null;
        if (type == null)
            type = "All";
        try {
            converter = getConverter();
            if (type.equalsIgnoreCase("all"))
                results = getArchetypeService().get(rmName, entityName,
                        conceptName, name, true);
            else {
                StringTokenizer typetokens = new StringTokenizer(type, ".");
                String entity = typetokens.nextToken();
                String concept = typetokens.nextToken();
                results = getArchetypeService().get(rmName, entity, concept,
                        name, true);
            }
            for (Object object : results) {
                converter.add(((IMObject) object).getLinkId(), object);
            }
        } catch (Exception e) {
            converter.clear();
        }
    }

    /**
     * 
     * @param model
     */
    public void onNameClick(Object model) {
        // TODO Retieve ID from model and the utilise this to retrieve object
        // using service layer. This
        // will solve problem with stale objects in Search List.
        EditPage page = (EditPage) Utils.findPage(getRequestCycle(),
                ((IMObject) model).getArchetypeId().getShortName(), "Edit");
        page.setModel(model);
        this.pushCallback();
        getRequestCycle().activate(page);
    }

    /**
     * 
     * @param cycle
     */
    public void onNew(IRequestCycle cycle) {
        // First check we have a selected archetype Name
        if (getArchetypeName() == null || getArchetypeName() == ""
                || getArchetypeName().equalsIgnoreCase("all"))
            return;

        EditPage page = (EditPage) Utils.findPage(getRequestCycle(),
                getArchetypeName(), "Edit");
        page.setModel(getArchetypeService().create(getArchetypeName()));
        this.pushCallback();
        cycle.activate(page);
    }

    /**
     * 
     * @param cycle
     */
    public void onSearch(IRequestCycle cycle) {
        readRecords();
    }

    /**
     * 
     * @return
     */
    public IPropertySelectionModel getArchetypeSearchModel() {
        // * get the archetype range tokens
        String range = getArchetypeRange();
        StringTokenizer tokens = new StringTokenizer(range, ".");
        String rmName = tokens.nextToken();
        String entityName = tokens.nextToken();
        String conceptName = tokens.nextToken();

        // Create a list of archetype short names from the supplied archetype
        // range
        // ArchetypeDescriptor[] archetypes =
        // getArchetypeService().getArchetypeDescriptorsByRmName(rmName);
        // <String> archetypenames = new ArrayList<String>();
        // for (ArchetypeDescriptor desc : archetypes) {
        // archetypenames.add(desc.getName());
        // }
        List<String> archetypeNames = getArchetypeService()
                .getArchetypeShortNames(rmName, entityName, conceptName, true);
        archetypeNames.add(0, "All");
        return new ArchetypeNameSelectionModel((String[]) archetypeNames
                .toArray(new String[archetypeNames.size()]));
    }
    
    /**
     * @return
     */
    public String getTitle() {
        String range = getArchetypeRange();
        StringTokenizer tokens = new StringTokenizer(range,  ".");
        String rmName = tokens.nextToken();
        String entityName = tokens.nextToken();
        String conceptName = tokens.nextToken();
        String title = rmName;
        if (entityName != null && !entityName.equalsIgnoreCase("*"))
            title = entityName;
        if (conceptName != null && !conceptName.equalsIgnoreCase("*"))
            title = conceptName;
        
        return "Search " + StringUtils.capitalize(Utils.pluralize(title));
    }

}
