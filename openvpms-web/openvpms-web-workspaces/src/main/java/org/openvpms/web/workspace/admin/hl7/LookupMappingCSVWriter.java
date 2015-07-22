/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.hl7;

import au.com.bytecode.opencsv.CSVWriter;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.business.service.lookup.ILookupService;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.openvpms.archetype.csv.AbstractCSVReader.MIME_TYPE;

/**
 * Writes a mapping between two lookup archetypes to CSV.
 *
 * @author Tim Anderson
 */
public class LookupMappingCSVWriter {

    /**
     * The CSV header line.
     */
    public static final String[] HEADER = {
            "Map From Type", "Map From Code", "Map From Name", "Map To Type", "Map To Code", "Map To Name"};

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The document handlers.
     */
    private final DocumentHandlers handlers;

    /**
     * The field separator.
     */
    private final char separator;


    /**
     * Constructs a {@link LookupMappingCSVWriter}.
     *
     * @param service   the archetype service
     * @param lookups   the lookups
     * @param handlers  the document handlers
     * @param separator the field separator
     */
    public LookupMappingCSVWriter(IArchetypeService service, ILookupService lookups, DocumentHandlers handlers,
                                  char separator) {
        this.service = service;
        this.lookups = lookups;
        this.handlers = handlers;
        this.separator = separator;
    }

    /**
     * Writes lookup mappings to a document.
     *
     * @param name the document name
     * @param from the from archetype shortname
     * @param to   the to archetype short name
     * @return the document
     */
    public Document write(String name, String from, String to) {
        StringWriter writer = new StringWriter();
        CSVWriter csv = new CSVWriter(writer, separator);
        csv.writeNext(HEADER);
        Collection<Lookup> fromLookups = sort(getLookups(from));
        Collection<Lookup> toLookups = sort(getLookups(to));
        for (Lookup fromLookup : fromLookups) {
            boolean found = false;
            IMObjectBean bean = new IMObjectBean(fromLookup);
            for (IMObjectReference mapping : bean.getNodeTargetObjectRefs("mapping")) {
                if (TypeHelper.isA(mapping, to)) {
                    Lookup toLookup = (Lookup) service.get(mapping);
                    if (toLookup != null) {
                        write(csv, from, fromLookup, to, toLookup);
                        toLookups.remove(toLookup);
                        found = true;
                    }
                }
            }
            if (!found) {
                write(csv, from, fromLookup, to, null);
            }
        }
        for (Lookup toLookup : toLookups) {
            write(csv, from, null, to, toLookup);

        }
        DocumentHandler handler = handlers.get(name, MIME_TYPE);
        byte[] buffer = writer.getBuffer().toString().getBytes(Charset.forName("UTF-8"));
        return handler.create(name, new ByteArrayInputStream(buffer), MIME_TYPE, buffer.length);
    }

    /**
     * Returns lookups matching a short name.
     *
     * @param shortName the archetype short name
     * @return the lookups
     */
    protected Collection<Lookup> getLookups(String shortName) {
        return lookups.getLookups(shortName);
    }

    /**
     * Sorts lookups on code.
     *
     * @param lookups the lookups to sort
     * @return the sorted lookups
     */
    protected List<Lookup> sort(Collection<Lookup> lookups) {
        List<Lookup> result = new ArrayList<>(lookups);
        Collections.sort(result, new Comparator<Lookup>() {
            @Override
            public int compare(Lookup o1, Lookup o2) {
                return o1.getCode().compareTo(o2.getCode());
            }
        });
        return result;
    }

    /**
     * Writes a mapping.
     *
     * @param writer     the writer
     * @param from       the 'from lookup' archetype
     * @param fromLookup the 'from lookup'. May be {@code null}
     * @param to         the 'to lookup' archetype
     * @param toLookup   the 'to lookup'. May be {@code null}
     */
    private void write(CSVWriter writer, String from, Lookup fromLookup, String to, Lookup toLookup) {
        String[] line = {from, getCode(fromLookup), getName(fromLookup), to, getCode(toLookup), getName(toLookup)};
        writer.writeNext(line);
        if (writer.checkError()) {
            throw new IllegalStateException("Failed to write lookup mapping");
        }
    }

    /**
     * Returns the name of a lookup.
     *
     * @param lookup the lookup. May be {@code null}
     * @return the lookup name. May be {@code null}
     */
    private String getName(Lookup lookup) {
        return lookup != null ? lookup.getName() : null;
    }

    /**
     * Returns the code of a lookup.
     *
     * @param lookup the lookup. May be {@code null}
     * @return the lookup code. May be {@code null}
     */
    private String getCode(Lookup lookup) {
        return lookup != null ? lookup.getCode() : null;
    }

}
