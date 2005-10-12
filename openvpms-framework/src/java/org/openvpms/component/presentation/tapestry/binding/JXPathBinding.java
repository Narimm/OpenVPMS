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


package org.openvpms.component.presentation.tapestry.binding;

// common-jxpath
import org.apache.commons.jxpath.JXPathContext;

// jakarta-hivemind
import org.apache.hivemind.Location;

// log4j
import org.apache.log4j.Logger;

// jakarta-tapestrty
import org.apache.tapestry.IComponent;
import org.apache.tapestry.binding.AbstractBinding;
import org.apache.tapestry.coerce.ValueConverter;

/**
 * This class supports JXPath value bindings in Tapestry
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class JXPathBinding extends AbstractBinding {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger
            .getLogger(JXPathBinding.class);

    
    /**
     * Hold a reference to the root object
     */
    private IComponent root;
    
    /**
     * The jxpath path relative to the root object
     */
    private String path;
        
    /**
     * Instantiate an instance of the this binding. The first part of the 
     * jxpath refers to the root object, which is a property within the 
     * component.
     *  
     * @param description
     * 
     * @param valueConverter
     * 
     * @param location
     * 
     * @param root
     *            the root object
     * @param path
     *            the path into the object
     */
    public JXPathBinding(String description, ValueConverter valueConverter, 
            Location location, IComponent root, String path) {
        super(description, valueConverter, location);
        this.root = root;
        this.path = path;
    }

    /*
     * TODO Inefficient implementation...do we need to cache
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.IBinding#getObject()
     */
    public Object getObject() {
        return JXPathContext.newContext(root).getValue(path);
    }

    /* 
     * TODO Inefficient implementation...do we need to cache
     * (non-Javadoc)
     * @see org.apache.tapestry.binding.AbstractBinding#setObject(java.lang.Object)
     */
    @Override
    public void setObject(Object value) {
        JXPathContext.newContext(root).setValue(path, value);
    }
}
