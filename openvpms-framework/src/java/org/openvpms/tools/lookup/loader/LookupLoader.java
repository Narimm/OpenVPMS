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

package org.openvpms.tools.lookup.loader;

// java core
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

// log4j
import org.apache.log4j.Logger;

// hibernate
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.Query;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.tools.lookup.loader.LookupData;
import org.openvpms.tools.lookup.loader.LookupItem;
import org.openvpms.tools.lookup.loader.LookupRel;

/**
 * This tool will process an XML data and load all the lookup data and lookup
 * relationships in to the database.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class LookupLoader {
    /**
     * Define a logger for this class
     */
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(LookupLoader.class);

    /**
     * A Hibernate session factory.
     */
    private SessionFactory sessionFactory;

    /**
     * A reference ot the lookup data, which will be processed
     */
    private LookupData data;

    /**
     * static to hold all session
     */
    public static final ThreadLocal<Session> session = new ThreadLocal<Session>();

    /**
     * Process the data in the specified file.
     * 
     * @param fileName
     *            the file that holds the lookup data
     */
    public LookupLoader(String fileName) throws Exception {
        // init
        init();

        // read the lookup data
        data = (LookupData) LookupData.unmarshal(new FileReader(fileName));
    }

    /**
     * The main line
     * 
     * @param args
     *            the file where the data is stored is passed in as the first
     *            argument
     */
    public static void main(String[] args) throws Exception {
        logger.info("Start Loading Lookup Data");
        LookupLoader loader = new LookupLoader(args[0]);
        loader.processLookups();
        loader.processRelationships();
        logger.info("End Loading Lookup Data");
    }

    /**
     * Process the lookup elements
     */
    protected void processLookups() throws Exception {
        Session session = currentSession();
        for (LookupItem item : data.getLookupItem()) {

            // only process new entries
            if (findLookup(session, item.getId(), item.getValue()) != null) {
                continue;
            }

            Transaction tx = session.beginTransaction();
            Lookup lookup = new Lookup();
            lookup.setValue(item.getValue());
            lookup.setCode(item.getValue());
            lookup.setArchetypeId(new ArchetypeId("openvpms-lookup-lookup."
                    + item.getId() + ".1.0"));
            session.saveOrUpdate(lookup);
            tx.commit();
            logger.info("Loaded lookup " + lookup.toString());
        }
    }

    /**
     * Process the look up relationships.
     */
    protected void processRelationships() throws Exception {
        for (LookupRel rel : data.getLookupRel()) {
            Session session = currentSession();
            // find the source lookup
            Lookup source = findLookup(session, rel.getSource().getId(), 
                    rel.getSource().getValue());
            if (source == null) {
                throw new RuntimeException("Cannot find source lookup "
                        + "[concept:" + rel.getSource().getId() + " value:"
                        + rel.getSource().getValue() + "]");
            }

            // find the target lookup
            Lookup target = findLookup(session, rel.getTarget().getId(), 
                    rel.getTarget().getValue());
            if (source == null) {
                throw new RuntimeException("Cannot find target lookup "
                        + "[concept:" + rel.getTarget().getId() + " value:"
                        + rel.getTarget().getValue() + "]");
            }

            // check if such a relationship exists and if it does then
            // skip it
            if (relationshipExists(session, source, target)) {
                continue;
            }
            
            // set up the relationship
            Transaction tx = session.beginTransaction();
            LookupRelationship relationship = new LookupRelationship(source,
                    target);
            session.saveOrUpdate(relationship);
            tx.commit();
            logger.info("Loaded relationship between " + source.toString()
                    + " and " + target.toString());
        }

    }

    /**
     * Initialise the sesion factory
     */
    private void init() throws Exception {
        Configuration config = new Configuration();
        config.addClass(Lookup.class);
        config.addClass(LookupRelationship.class);
        sessionFactory = config.buildSessionFactory();
    }

    /**
     * Get the current hibernate session
     * 
     * @return Session
     * @throws Exception
     */
    private Session currentSession() throws Exception {
        Session s = (Session) session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
            s = sessionFactory.openSession();
            session.set(s);
        }
        return s;
    }

    /**
     * Close the current hibernate session
     * 
     * @throws Exception
     */
    public void closeSession() throws Exception {
        Session s = (Session) session.get();
        session.set(null);
        if (s != null)
            s.close();
    }

    /**
     * Locate a lookup with the specified concept and value.
     * 
     * @param session
     *            the session to use
     * @param concept
     *            the conept to look for
     * @param value
     *            the value to search for
     * @return Lookup the lookup or null if one does not exist
     * @throws Exception
     *             propagate exception to caller
     */
    private Lookup findLookup(
            Session session, String concept, String value) {
        Query query = session
                .getNamedQuery("lookup.getLookupForConceptAndValue");
        query.setString("concept", concept);
        query.setString("value", value);

        List list = query.list();

        return list.size() == 0 ? null
                : (Lookup) list.get(0);
    }
    
    /**
     * Determine if there is a relationship between the specified source and 
     * target lookups. 
     * 
     * @param session
     *            the session to use
     * @param source
     *            the source lookup
     * @param target 
     *            the target lookup
     * @return boolean
     *            true if a relatioship already exists
     * @throws Exception
     *            propagate all exceptions to caller                                                
     */
    private boolean relationshipExists(Session session, Lookup source, Lookup target)
    throws Exception {
        Query query = session.getNamedQuery("lookupRelationship.getTargetLookups");
        query.setParameter("uid", source.getUid());
        query.setParameter("type", new StringBuffer(
                source.getArchetypeId().getConcept())
                .append(".")
                .append(target.getArchetypeId().getConcept())
                .toString());
        
        List list = query.list();
        Iterator iter = list.iterator();
        while (iter.hasNext()) {
            Lookup lookup = (Lookup)iter.next();
            if (lookup.equals(target)) {
                return true;
            }
        }
        
        return false;
    }
}
