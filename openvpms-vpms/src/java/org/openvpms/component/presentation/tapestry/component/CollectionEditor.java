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

package org.openvpms.component.presentation.tapestry.component;

// java core
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

// commons-lang
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.hivemind.ApplicationRuntimeException;

// jakarta-tapestry
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.form.IPropertySelectionModel;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeService;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.presentation.tapestry.callback.CollectionCallback;
import org.openvpms.component.presentation.tapestry.callback.EditCallback;
import org.openvpms.component.presentation.tapestry.page.EditPage;
import org.openvpms.component.presentation.tapestry.page.OpenVpmsPage;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class CollectionEditor extends OpenVpmsComponent {

    public abstract Collection getCollection();

    public abstract void setCollection(Collection Collection);

    public abstract Object getModel();

    public abstract void setModel(Object Model);

    public abstract NodeDescriptor getDescriptor();

    public abstract void setDescriptor(NodeDescriptor nodeDescriptor);

    public abstract Object getCurrentObject();

    public abstract void setCurrentObject(Object CurrentObject);

    public abstract String getArchetypeName();

    private List selected = new ArrayList();

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.AbstractComponent#prepareForRender(org.apache.tapestry.IRequestCycle)
     */
    protected void prepareForRender(IRequestCycle arg0) {
        // TODO Auto-generated method stub
        super.prepareForRender(arg0);
        if (arg0.isRewinding() == false)
            buildSelectedList();
    }

    /**
     * 
     *
     */
    @SuppressWarnings("unchecked")
    void buildSelectedList() {
        Collection collection = getCollection();
        if (collection != null) {
            selected = new ArrayList();
            for (Iterator iter = collection.iterator(); iter.hasNext();) {
                iter.next();
                selected.add(new Boolean(false));
            }
        }
    }

    /**
     * 
     * @param cycle
     */
    @SuppressWarnings("unchecked")
    public void onNew(IRequestCycle cycle) {

        Object[] parameters = cycle.getListenerParameters();
        NodeDescriptor descriptor = (NodeDescriptor) parameters[0];

        // First check we have a selected archetype Name
        if (getArchetypeName() == null || getArchetypeName() == "")
            return;

        // Push a Collection Callback onto the stack.
        CollectionCallback callback = new CollectionCallback(getPage()
                .getPageName(), getModel(), descriptor);
        ((OpenVpmsPage) getPage()).getVisitObject().getCallbackStack().push(
                callback);

        // Look for specific page or else use DefaultEdit
        EditPage editPage = (EditPage) Utils.findPage(cycle, Utils
                .unqualify(descriptor.getDisplayName() + "Edit"), "Edit");
        try {
            // we need to do some indirection to avoid a StaleLink
            EditCallback nextPage = new EditCallback(editPage.getPageName(),
                    editPage.getArchetypeService().create(getArchetypeName()));
            ((EditPage) getPage()).setNextPage(nextPage);

        } catch (Exception ex) {
            throw new ApplicationRuntimeException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public void onNameClick(IRequestCycle cycle) {
        try {
            // Push a Collection Callback onto the stack.
            CollectionCallback callback = new CollectionCallback(getPage()
                    .getPageName(), getModel(), getDescriptor());
            ((OpenVpmsPage) getPage()).getVisitObject().getCallbackStack()
                    .push(callback);

            // Look for specific edit page for archetype or else use DefaultEdit
            // page.
            EditPage editPage = (EditPage) Utils.findPage(cycle, "DefaultEdit",
                    "Edit");

            // we need to do some indirection to avoid a StaleLink
            EditCallback nextPage = new EditCallback(editPage.getPageName(),
                    getCurrentObject());
            ((EditPage) getPage()).setNextPage(nextPage);

        } catch (Exception ex) {
            throw new ApplicationRuntimeException(ex);
        }
    }

    /**
     * 
     * @param cycle
     */
    @SuppressWarnings("unchecked")
    public void onRemove(IRequestCycle cycle) {

        Object[] parameters = cycle.getListenerParameters();
        NodeDescriptor descriptor = (NodeDescriptor) parameters[0];
        try {
            Collection collect = (Collection) getValue(getModel(), descriptor
                    .getPath());
            int i = 0;
            ArrayList deleting = new ArrayList();
            for (Iterator iter = collect.iterator(); iter.hasNext();) {

                Object element = (Object) iter.next();

                if (((Boolean) getSelected().get(i)).booleanValue()) {
                    deleting.add(element);
                }
                i++;
            }
            collect.removeAll(deleting);
        } catch (Exception e) {
        }
    }

    /**
     * @return Returns the toBeDeleted.
     */
    public List getSelected() {
        return selected;
    }

    /**
     * @param toBeDeleted
     *            The toBeDeleted to set.
     */
    public void setSelected(List toBeDeleted) {
        this.selected = toBeDeleted;
    }

    /**
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public List getSelectedList() {
        ArrayList selectedList = new ArrayList();
        selectedList.addAll(getCollection());
        return selectedList;
    }

    /**
     * 
     * @param selected
     */
    @SuppressWarnings("unchecked")
    public void setSelectedList(List selected) {
        if (selected != null) {
            getCollection().clear();
            getCollection().addAll(selected);
        }
    }

    public IPropertySelectionModel getArchetypeNamesModel(
            NodeDescriptor descriptor) {
        if (descriptor == null)
            return new ArchetypeNameSelectionModel(new String[] { "" });
        else
            return new ArchetypeNameSelectionModel(descriptor
                    .getArchetypeRange());
    }

    /**
     * 
     * @return
     */
    public IPropertySelectionModel getSelectionModel() {
        // don't allow use to select from all here
        if (getDescriptor().isParentChild()) {
            return new IdentifierSelectionModel(getSelectedList());
        }
        // but do here
        else {
            // return new
            // IdentifierSelectionModel(getDescriptor().getCandidateChildren(((IMObject)getModel())));
            return new IdentifierSelectionModel(ArchetypeServiceHelper
                    .getCandidateChildren(
                            (ArchetypeService) ((OpenVpmsPage) getPage())
                                    .getArchetypeService(), getDescriptor(),
                            ((IMObject) getModel())));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * 
     * @return
     */
    public boolean isList() {
        return getCollection() instanceof List;
    }
}
