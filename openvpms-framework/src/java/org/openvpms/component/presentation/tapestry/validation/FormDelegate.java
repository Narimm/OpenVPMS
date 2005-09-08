/*
 * FormDelegate.java
 * Created on 06.04.2005 by andyman
 * project wirteverein-admin
 * 
 */

package org.openvpms.component.presentation.tapestry.validation;

import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.form.IFormComponent;
import org.apache.tapestry.valid.IValidator;
import org.apache.tapestry.valid.ValidationDelegate;

/**
 * 
 * The delegate class used to provide the bells and whistles for the
 * applications form. Extends ValidationDelegate only to provide custom label
 * decorations and stuff like that.
 * 
 * 
 * @author andyman
 *  
 */
public class FormDelegate extends ValidationDelegate {
    
    public void writeLabelPrefix(IFormComponent component,
            IMarkupWriter writer, IRequestCycle cycle) {
        if (isInError(component)) {
            writer.begin("span");
            writer.attribute("class", "invalidFormFieldLabel");
        }
    }

    public void writeLabelSuffix(IFormComponent component,
            IMarkupWriter writer, IRequestCycle cycle) {
        if (isInError(component))
            writer.end();
    }

    public void writeAttributes(IMarkupWriter writer, IRequestCycle cycle,
            IFormComponent component, IValidator validator) {
        if (isInError())
            writer.attribute("class", "invalidFormField");
    }

    public void writeSuffix(IMarkupWriter writer, IRequestCycle cycle,
            IFormComponent component, IValidator validator) {
       
    }
}