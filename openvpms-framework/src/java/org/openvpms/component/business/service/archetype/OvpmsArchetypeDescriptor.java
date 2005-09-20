/*
 * Copyright 2004 Chris Nelson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.openvpms.component.business.service.archetype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ognl.Ognl;
import ognl.OgnlException;

import org.openvpms.component.business.domain.archetype.Archetype;
import org.openvpms.component.presentation.tapestry.component.Utils;


/**
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class OvpmsArchetypeDescriptor extends OvpmsDescriptor implements IArchetypeDescriptor
{
    private List propertyDescriptors = new ArrayList();
    private boolean child;
    private Archetype archetype;
    
    public OvpmsArchetypeDescriptor(IArchetypeDescriptor descriptor)
    {
        super(descriptor);
        for (Iterator iter = descriptor.getPropertyDescriptors().iterator(); iter.hasNext();)
        {
            IPropertyDescriptor propertyDescriptor = (IPropertyDescriptor) iter.next();
            getPropertyDescriptors().add(propertyDescriptor.clone());
        }
    }
    
    /**
     * @return Returns the archetype.
     */
    public Archetype getArchetype() {
        return archetype;
    }

    /**
     * @param archetype The archetype to set.
     */
    public void setArchetype(Archetype archetype) {
        this.archetype = archetype;
    }

    public OvpmsArchetypeDescriptor(Class type)
    {
        super(type);
    }

    public OvpmsArchetypeDescriptor(Class type, String displayName)
    {
        super(type);
        this.setDisplayName(displayName);
    }
    
    /**
     * @return Returns the propertyDescriptors.
     */
    public List getPropertyDescriptors()
    {
        return propertyDescriptors;
    }

    /**
     * @param propertyDescriptors
     *            The propertyDescriptors to set.
     */
    public void setPropertyDescriptors(List propertyDescriptors)
    {
        this.propertyDescriptors = propertyDescriptors;
    }

    public IPropertyDescriptor getIdentifierDescriptor()
    {
        String ognl = "propertyDescriptors.{? identifier}[0]";

        return findDescriptor(ognl);
    }

    /**
     * @param ognl
     * @return
     */
    private IPropertyDescriptor findDescriptor(String ognl)
    {
        try
        {
            return (IPropertyDescriptor) Ognl.getValue(ognl, this);
        }catch (OgnlException oe)
        {
            oe.printStackTrace();

            return null;
        }catch (IndexOutOfBoundsException ie)
        {
            return null;
        }
    }

    /**
     * @param string
     * @return
     */
    public IPropertyDescriptor getPropertyDescriptor(String name)
    {
        return findDescriptor("propertyDescriptors.{? name == '" + name +
            "'}[0]");
    }

    /**
     * @return
     */
    public String getPluralDisplayName()
    {
        return Utils.pluralize(Utils.unCamelCase(getDisplayName()));
    }
    
    /**
     * @return Returns the child.
     */
    public boolean isChild()
    {
        return child;
    }
    
    /**
     * @param child The child to set.
     */
    public void setChild(boolean child)
    {
        this.child = child;
    }

    @Override
    public Object clone()
    {
        return new OvpmsArchetypeDescriptor(this);
    }

    public List getPropertyDescriptors(String[] properties)
    {
        ArrayList descriptors = new ArrayList();
        for (int i = 0; i < properties.length; i++)
        {
            descriptors.add(getPropertyDescriptor(properties[i]));
        }
        return descriptors;
    }

    
}
