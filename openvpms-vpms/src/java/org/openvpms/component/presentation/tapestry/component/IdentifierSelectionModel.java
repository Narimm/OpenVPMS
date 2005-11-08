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
import java.util.List;

import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.tapestry.form.IPropertySelectionModel;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class IdentifierSelectionModel implements IPropertySelectionModel {
    private List instances;

    private boolean allowNone;

    public static String NONE_LABEL = "None";

    public static String NONE_VALUE = "none";

    public IdentifierSelectionModel(List instances) {
        this.instances = instances;
        this.allowNone = false;
    }

    public IdentifierSelectionModel(List instances,boolean allowNone) {
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
        // TODO Auto-generated method stub
        if (instances == null)
            return 0;
        else
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
        // Return the name for the IMObject at the index in the collection 
        return ((IMObject)instances.get(index)).getDescription();
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
                // Return the String value of the uid for object at the index 
                return Long.toString(((IMObject)instances.get(index)).getUid());
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
        List realInstances = allowNone ? instances.subList(1, instances.size())
                : instances;
        try {          
            if (allowNone) {
                if (value.equals(NONE_VALUE))
                    return null;
            }
           
            if (realInstances != null) {
                for (Object object : realInstances) {
                    if ((object instanceof IMObject) && (((IMObject)object).getUid() == Long.parseLong(value))) {
                        return object;
                    }
                }
            }
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e);
        }
        return null;
    }

    /**
     * @return Returns the instances.
     */
    public List getInstances() {
        return instances;
    }

    /**
     * @param instances
     *            The instances to set.
     */
    public void setInstances(List instances) {
        this.instances = instances;
    }
}
