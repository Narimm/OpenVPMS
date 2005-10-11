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
import java.util.Iterator;
import java.util.List;
import org.apache.tapestry.IRequestCycle;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public abstract class ArchetypeTable extends OpenVpmsComponent {
	public abstract List getInstances();

	public abstract void setInstances(List instances);

	public abstract Object getCurrentObject();

	public abstract void setCurrentObject(Object CurrentObject);

	private List selected = new ArrayList();

	protected void prepareForRender(IRequestCycle arg0) {
		// TODO Auto-generated method stub
		super.prepareForRender(arg0);
		buildSelectedList();
	}

	@SuppressWarnings("unchecked")
	void buildSelectedList() {
		if (getInstances() != null) {
			selected = new ArrayList();
			for (Iterator iter = getInstances().iterator(); iter.hasNext();) {
				iter.next();
				selected.add(new Boolean(false));
			}
		}
	}

	public void showAddPage(IRequestCycle cycle) {
	}

	@SuppressWarnings("unchecked")
	public void remove(IRequestCycle cycle) {
		int i = 0;
		ArrayList deleting = new ArrayList();
		for (Iterator iter = getInstances().iterator(); iter.hasNext();) {

			Object element = (Object) iter.next();

			if (((Boolean) getSelected().get(i)).booleanValue()) {
				deleting.add(element);
			}
			i++;
		}
		getInstances().removeAll(deleting);
	}

	@SuppressWarnings("unchecked")
	public List getSelectedList() {
		ArrayList selectedList = new ArrayList();
		selectedList.addAll(getInstances());
		return selectedList;
	}

	@SuppressWarnings("unchecked")
	public void setSelectedList(List selected) {
		if (selected != null) {
			getInstances().clear();
			getInstances().addAll(selected);
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
}
