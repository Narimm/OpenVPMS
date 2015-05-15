/*
 * Copyright (c) 2015.
 *
 * Copy Charlton IT
 *
 * All rights reserved.
 */

package org.openvpms.archetype.function.export;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;
import org.openvpms.archetype.rules.export.ExportRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.List;


/**
 * @author benjamincharlton on 23/03/2015.
 */
public class ExportFunctions {

    private final ExportRules rules;

    public ExportFunctions(IArchetypeService service, ILookupService lookups) {
        rules = new ExportRules(service, lookups);
    }

    public String getImportCountryFullName(ExpressionContext context) {
        Act exportAct = getAct(context);
        return (exportAct == null) ? "" : rules.getImportCountryName(exportAct);
    }

    public String getImportCountryFullName(Act export) {
        return rules.getImportCountryName(export);
    }

    public Party getImporter(ExpressionContext context) {
        Act exportAct = getAct(context);
        return (exportAct == null) ? null : getImporter(exportAct);
    }

    public Party getImporter(Act document) {
        ActBean docbean = new ActBean(document);
        Act export = docbean.getSourceAct("actRelationship.exportItem");
        return rules.getImporter(export);
    }

    public List<Party> getPatients(ExpressionContext context) {
        Act docAct = getAct(context);
        return (docAct == null) ? null : getPatients(docAct);
    }

    public List<Party> getPatients(Act document) {
        ActBean docbean = new ActBean(document);
        Act export = docbean.getSourceAct("actRelationship.exportItem");
        return rules.getPatients(export);
    }

    private Act getAct(ExpressionContext context) {
        Pointer pointer = context.getContextNodePointer();
        Object value = pointer.getValue();
        if ((pointer == null) && !(value instanceof Act)) {
            return null;
        }
        return (Act) pointer.getValue();
    }

}
