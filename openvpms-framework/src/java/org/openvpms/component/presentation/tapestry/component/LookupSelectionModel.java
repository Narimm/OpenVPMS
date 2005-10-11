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
 *  $Id: LookupSelectionModel.java 118 2005-09-21 09:36:09Z tony $
 */

package org.openvpms.component.presentation.tapestry.component;

// java core
import java.util.ArrayList;
import java.util.List;

// jakarta hivemind
import org.apache.hivemind.ApplicationRuntimeException;

// jakarta tapestry
import org.apache.tapestry.form.IPropertySelectionModel;

// openvpms-framework
import org.openvpms.component.business.domain.im.lookup.Lookup;

/**
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-09-21 19:36:09 +1000 (Wed, 21 Sep 2005) $
 */
public class LookupSelectionModel implements IPropertySelectionModel {
    private List<Lookup> instances;

    private boolean allowNone;

    public static String NONE_LABEL = "None";

    public static String NONE_VALUE = "none";

    /**
     * @param instances
     * @param idProperty
     */
    public LookupSelectionModel(List<Lookup> instances) {
        this.instances = instances;
        this.allowNone = false;
    }

    /**
     * @param instances
     * @param idProperty
     * @param allowNone
     */
    @SuppressWarnings("unchecked")
    public LookupSelectionModel(List<Lookup> instances, boolean allowNone) {
        this(instances);
        this.allowNone = allowNone;
        if (this.allowNone) {
            this.instances = new ArrayList();
            this.instances.addAll(instances);
            this.instances.add(0, null);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.form.IPropertySelectionModel#getOptionCount()
     */
    public int getOptionCount() {
        return instances.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.form.IPropertySelectionModel#getOption(int)
     */
    public Object getOption(int index) {
        return instances.get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.form.IPropertySelectionModel#getLabel(int)
     */
    public String getLabel(int index) {
        if (allowNone && index == 0) {
            return NONE_LABEL;
        }
        return instances.get(index).getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.form.IPropertySelectionModel#getValue(int)
     */
    public String getValue(int index) {
        try {
            if (allowNone && index == 0) {
                return NONE_VALUE;
            } else {
                return instances.get(index).getValue();
            }
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.form.IPropertySelectionModel#translateValue(java.lang.String)
     */
    public Object translateValue(String value) {
        @SuppressWarnings("unused") List realInstances = allowNone ? instances.subList(1, instances.size())
                : instances;
        try {
            if (allowNone) {
                if (value.equals(NONE_VALUE))
                    return null;
            }
            return value;
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e);
        }
    }

    /**
     * @return
     */
    public List getInstances() {
        return instances;
    }

    /**
     * @param instances
     */
    public void setInstances(List<Lookup> instances) {
        this.instances = instances;
    }
}
