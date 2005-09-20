package org.openvpms.component.presentation.tapestry.validation;

import java.util.Iterator;

import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.form.IFormComponent;
import org.apache.tapestry.valid.FieldTracking;
import org.apache.tapestry.valid.IFieldTracking;
import org.apache.tapestry.valid.IValidator;
import org.apache.tapestry.valid.RenderString;
import org.apache.tapestry.valid.ValidationDelegate;
import org.apache.tapestry.valid.ValidatorException;

public class OvpmsValidationDelegate extends ValidationDelegate
{
    
    public void record(ValidatorException ex)
    {
        FieldTracking tracking = findCurrentTracking();
        tracking.setErrorRenderer(new RenderString(ex.getMessage()));
        
    }

    public IFieldTracking getFieldTracking(String string)
    {
        if (getAssociatedTrackings() == null) {return null;}
        for (Iterator iter = getAssociatedTrackings().iterator(); iter.hasNext();)
        {
            IFieldTracking tracking = (IFieldTracking) iter.next();
            if (string.equals(tracking.getFieldName()))
            {
                return tracking;
            }
                    
        }   
        return null;
    }
    
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
