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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.tapestry.IRequestCycle;
import org.openvpms.component.business.service.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.presentation.tapestry.callback.EditCallback;
import org.openvpms.component.presentation.tapestry.page.EditPage;

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

    private List selected = new ArrayList();

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.AbstractComponent#prepareForRender(org.apache.tapestry.IRequestCycle)
     */
    protected void prepareForRender(IRequestCycle arg0) {
        // TODO Auto-generated method stub
        super.prepareForRender(arg0);
        buildSelectedList();
    }

    @SuppressWarnings("unchecked")
    void buildSelectedList() {
        if (getCollection() != null) {
            selected = new ArrayList();
            for (Iterator iter = getCollection().iterator(); iter.hasNext();) {
                iter.next();
                selected.add(new Boolean(false));
            }
        }
    }

    public void showAddPage(IRequestCycle cycle) {

        // Look for specific page or else use <Default>AddToCollection
        EditPage editPage = (EditPage) Utils.findPage(cycle, Utils
                .unqualify(getDescriptor().getDisplayName() + "Edit"), "Edit");
        try {
            // we need to do some indirection to avoid a StaleLink
            EditCallback nextPage = new EditCallback(editPage.getPageName(),
                    getDescriptor().getType().getClass().newInstance());
            ((EditPage) getPage()).setNextPage(nextPage);

        } catch (Exception ex) {
            throw new ApplicationRuntimeException(ex);
        }
        // cycle.activate(editPage);
    }

    EditCallback buildCallback() {
        EditCallback callback = new EditCallback(getPage().getPageName(),
                getModel());
        return callback;
    }

    @SuppressWarnings("unchecked")
	public void remove(IRequestCycle cycle) {
        int i = 0;
        // TODO CN - This code stinks (I wrote it). Isn't there a better way??
        ArrayList deleting = new ArrayList();
        for (Iterator iter = getCollection().iterator(); iter.hasNext();) {

            Object element = (Object) iter.next();

            if (((Boolean) getSelected().get(i)).booleanValue()) {
                deleting.add(element);
            }
            i++;
        }
        getCollection().removeAll(deleting);
    }

    @SuppressWarnings("unchecked")
	public List getSelectedList() {
        ArrayList selectedList = new ArrayList();
        selectedList.addAll(getCollection());
        return selectedList;
    }

    @SuppressWarnings("unchecked")
	public void setSelectedList(List selected) {
        if (selected != null) {
            getCollection().clear();
            getCollection().addAll(selected);
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

    public boolean isList() {
        return getCollection() instanceof List;
    }
}
