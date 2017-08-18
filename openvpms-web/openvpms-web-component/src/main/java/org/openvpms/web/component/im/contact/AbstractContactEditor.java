package org.openvpms.web.component.im.contact;

import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.system.ServiceHelper;

/**
 * Base class for contact editors.
 *
 * @author Tim Anderson
 */
public abstract class AbstractContactEditor extends AbstractIMObjectEditor {

    /**
     * Preferred contact node name.
     */
    public static final String PREFERRED = "preferred";

    /**
     * Constructs an {@link AbstractContactEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public AbstractContactEditor(IMObject object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
    }

    /**
     * Determines if the contact is preferred.
     *
     * @return {@code true} if the contact is preferred
     */
    public boolean isPreferred() {
        Property preferred = getProperty(PREFERRED);
        return preferred != null && preferred.getBoolean();
    }

    /**
     * Adds a contact purpose to the contact, if it exists.
     *
     * @param code the <em>lookup.contactPurpose</em> code
     */
    public void addContactPurpose(String code) {
        CollectionProperty purposes = getCollectionProperty("purposes");
        if (purposes != null) {
            ILookupService service = ServiceHelper.getLookupService();
            Lookup lookup = service.getLookup(ContactArchetypes.PURPOSE, code);
            if (lookup != null) {
                purposes.add(lookup);
            }
        }
    }
}
