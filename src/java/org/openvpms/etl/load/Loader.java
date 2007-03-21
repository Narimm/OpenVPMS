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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.etl.ETLNode;
import org.openvpms.etl.ETLObject;
import org.openvpms.etl.ETLObjectDAO;
import org.openvpms.etl.ETLReference;
import org.openvpms.etl.ETLText;
import org.openvpms.etl.ETLValue;
import org.openvpms.etl.Reference;
import org.openvpms.etl.ReferenceParser;
import static org.openvpms.etl.load.LoaderException.ErrorCode.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Loader {

    /**
     * The default application context.
     */
    private static final String APPLICATION_CONTEXT = "applicationContext.xml";

    /**
     * The source.
     */
    private final ETLObjectDAO dao;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The loaded objects.
     */
    private Map<Long, State> loaded
            = new HashMap<Long, State>();


    /**
     * Constructs a new <tt>Loader</tt>.
     *
     * @param service the archetype service
     */
    public Loader(ETLObjectDAO dao, IArchetypeService service) {
        this.dao = dao;
        this.service = service;
    }

    public void load() {
        List<ETLObject> objects;
        int first = 0;
        int count = 100;
        while (true) {
            objects = dao.get(first, count);
            if (objects.isEmpty()) {
                break;
            }
            first += count;
            for (ETLObject object : objects) {
                load(object);
            }
        }
    }

    private IMObject load(ETLReference source) {
        IMObject result = null;
        ETLObject object = source.getObject();
        if (object == null) {
            Reference ref = ReferenceParser.parse(source.getValue());
            if (ref == null) {
                throw new LoaderException(InvalidReference, source.getValue());
            }
            if (ref.getLegacyId() != null) {
                ETLObject target = dao.get(ref.getArchetype(),
                                           ref.getLegacyId());
                if (target == null) {
                    throw new LoaderException(ObjectNotFound, ref);
                }
                result = load(target);
            } else {
                ArchetypeQuery query = new ArchetypeQuery(ref.getArchetype(),
                                                          true, true);
                query.add(new NodeConstraint(ref.getName(), ref.getValue()));
                query.setMaxResults(2);
                Iterator<IMObject> iterator
                        = new IMObjectQueryIterator<IMObject>(service, query);
                if (iterator.hasNext()) {
                    result = iterator.next();
                    if (iterator.hasNext()) {
                        throw new LoaderException(
                                LoaderException.ErrorCode.RefResolvesMultipleObjects,
                                ref.toString());
                    }
                }
            }
        } else {
            result = load(source.getObject());
        }
        return result;
    }

    private IMObject load(ETLObject source) {
        IMObject target;
        State state = loaded.get(source.getObjectId());
        if (state == null) {
            target = service.create(source.getArchetype());
            if (target == null) {
                throw new LoaderException(ArchetypeNotFound,
                                          source.getArchetype());
            }
            state = new State(target);
            loaded.put(source.getObjectId(), state);
            IMObjectBean bean = new IMObjectBean(target, service);
            for (ETLNode node : source.getNodes()) {
                for (ETLValue value : node.getValues()) {
                    if (value instanceof ETLText) {
                        ETLText text = (ETLText) value;
                        bean.setValue(node.getName(), text.getValue());
                        // todo - need to handle multiple values for simple
                        // node
                    } else {
                        ETLReference ref = (ETLReference) value;
                        IMObject child = load(ref);
                        bean.addValue(node.getName(), child);
                    }
                }
            }
            service.save(target);
            state.setNull();
        } else {
            target = state.getObject();
            if (target == null) {
                target = ArchetypeQueryHelper.getByObjectReference(
                        service, state.getRef());
            }
            if (target == null) {
                throw new LoaderException(ObjectNotFound, state.getRef());
            }
        }
        return target;
    }

    /**
     * Main line.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            JSAP parser = createParser();
            JSAPResult config = parser.parse(args);
            if (!config.success()) {
                displayUsage(parser);
            } else {
                String contextPath = config.getString("context");

                ApplicationContext context;
                if (!new File(contextPath).exists()) {
                    context = new ClassPathXmlApplicationContext(contextPath);
                } else {
                    context = new FileSystemXmlApplicationContext(contextPath);
                }

                ETLObjectDAO dao = (ETLObjectDAO) context.getBean(
                        "ETLObjectDAO");
                IArchetypeService service = (IArchetypeService) context.getBean(
                        "archetypeService");
                Loader loader = new Loader(dao, service);
                loader.load();
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("context").setShortFlag('c')
                .setLongFlag("context")
                .setDefault(APPLICATION_CONTEXT)
                .setHelp("Application context path"));
        return parser;
    }

    /**
     * Prints usage information.
     */
    private static void displayUsage(JSAP parser) {
        System.err.println();
        System.err
                .println("Usage: java " + Loader.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.exit(1);
    }

    private static class State {

        private final IMObjectReference reference;
        private IMObject object;

        public State(IMObject object) {
            reference = object.getObjectReference();
            this.object = object;
        }

        public IMObjectReference getRef() {
            return reference;
        }

        public IMObject getObject() {
            return object;
        }

        public void setNull() {
            object = null;
        }
    }

}
