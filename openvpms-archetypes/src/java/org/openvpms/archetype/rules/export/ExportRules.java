/*
 * Copyright (c) 2015.
 *
 * Copy Charlton IT
 *
 * All rights reserved.
 */
package org.openvpms.archetype.rules.export;

import org.apache.commons.collections.Predicate;
import org.openvpms.archetype.rules.party.PartyRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.functor.IsA;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.sort.IMObjectSorter;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * @author benjamincharlton on 23/03/2015.
 */
public class ExportRules extends PartyRules {

    private final IArchetypeService service;

    private final ILookupService lookups;

    private final IMObjectSorter sorter;

    public ExportRules(ILookupService lookups) {
        this(ArchetypeServiceHelper.getArchetypeService(), lookups);
    }

    public ExportRules(IArchetypeService service, ILookupService lookups) {
        super(service, lookups);
        this.service = service;
        this.lookups = lookups;
        this.sorter = new IMObjectSorter(service, lookups);
    }

    public Act getExportAct(Act exportDoc) {
        ActBean docbean = new ActBean(exportDoc);
        return docbean.getTargetAct("act.Export");
    }
    public Party getImporter(Act exportAct) {
        ActBean bean = new ActBean(exportAct, service);
        return (Party) bean.getParticipant(ExportArchetypes.IMPORTER_PARTICIPATION);
    }


    public String getImportCountryName(Act exportAct) {
        ActBean bean = new ActBean(exportAct, service);
        NodeDescriptor descriptor = bean.getDescriptor("exportCountry");
        return LookupHelper.getName(service, lookups, descriptor, bean.getObject());
    }

    @Override
    public String getFullName(Act exportAct) {
        ActBean bean = new ActBean(exportAct, service);
        if (bean.hasNode("exporterAgent") && (bean.getString("exporterAgent") != null)) {
            return bean.getString("exporterAgent");
        }
        return super.getFullName(exportAct);
    }

    public List<Party> getPatients(Act exportAct) {
        Collection<Participation> participations = exportAct.getParticipations();
        Predicate predicate = new IsA("participation.patient");
        List<Party> result = new ArrayList<Party>();
        for (Participation p : participations) {
            if (predicate.evaluate(p)) {
                result.add((Party) service.get(p.getEntity()));
            }
        }
        return (result.size() == 0) ? null : sorter.sort(result, "id");
    }
}
