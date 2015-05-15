

/*
 * Copyright (c) 2015.
 *
 * Copy Charlton IT
 *
 * All rights reserved.
 */

package org.openvpms.web.component.im.importer;

import org.openvpms.archetype.rules.export.ExportArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.DefaultIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Property;

/**
 * @author benjamincharlton on 17/03/2015.
 */
public class ImporterParticipationEditor extends ParticipationEditor<Party> {


    public ImporterParticipationEditor(Participation participation,
                                       Act parent, LayoutContext layout){
        super(participation, parent, layout);
        if (!TypeHelper.isA(participation, ExportArchetypes.IMPORTER_PARTICIPATION)) {
            throw new IllegalArgumentException(
                    "Invalid participation type:"
                            + participation.getArchetypeId().getShortName());
        }
        }
    @Override
    protected IMObjectReferenceEditor<Party> createEntityEditor(
            Property property) {
        LayoutContext context = getLayoutContext();
        LayoutContext subContext = new DefaultLayoutContext(context, context.getHelpContext().topic("importer"));
        IMObjectReferenceEditor refEditor = new DefaultIMObjectReferenceEditor(property,getParent(),subContext);
        refEditor.setAllowCreate(true);

        return refEditor;

        }
    }



