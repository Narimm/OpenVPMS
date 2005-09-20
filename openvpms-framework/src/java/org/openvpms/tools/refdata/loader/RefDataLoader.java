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


package org.openvpms.tools.refdata.loader;

// java core
import java.io.FileReader;
import java.util.List;

// hibernate
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;

//openvpms-framework
import org.apache.log4j.Logger;
import org.openvpms.component.business.domain.refdata.animal.Breed;
import org.openvpms.component.business.domain.refdata.animal.BreedColour;
import org.openvpms.component.business.domain.refdata.animal.Species;
import org.openvpms.component.business.domain.refdata.demographics.Country;
import org.openvpms.component.business.domain.refdata.demographics.State;
import org.openvpms.component.business.domain.refdata.demographics.Suburb;
import org.openvpms.tools.refdata.loader.ReferenceData;


/**
 * This is a tool for loading reference data into the database. 
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class RefDataLoader {
    /**
     * Define a logger for this class
     */
    private static final Logger logger = Logger
            .getLogger(RefDataLoader.class);

    /**
     * The name of the file, which holds the reference data
     */
    private String fileName;
    
    /**
     * Referene to the hibernate session factory
     */
    private SessionFactory sessionFactory;
    
    /**
     * static to hold all session
     */
    public static final ThreadLocal<Session> session = new ThreadLocal<Session>();
    
    /**
     * The name of the file that holds the reference data. It is an XML 
     * document that complies with the refdata-loader.xsd XML Schema
     * 
     * @param fileName
     *            the name of the file
     * @throws Exception             
     */
    public RefDataLoader(String fileName) 
    throws Exception {
        this.fileName = fileName;
        
        // create the hibernate session factory
        Configuration config = new Configuration();
        config.addClass(Species.class);
        config.addClass(Breed.class);
        config.addClass(BreedColour.class);
        
        config.addClass(Country.class);
        config.addClass(State.class);
        config.addClass(Suburb.class);
        
        this.sessionFactory = config.buildSessionFactory();
        
    }

    /**
     * This is the entry point for the loader
     * 
     * @param args
     *            the args must have a valid non-null file name
     * @throws Exception
     *            exceptions are propagated to caller.            
     */
    public static void main(String[] args) 
    throws Exception {
        if ((args == null) ||
            (args.length == 0)) {
            throw new RuntimeException("Must supply a file name");
        }
        
        logger.debug("Processing the reference data in file " + args[0]);
        new RefDataLoader(args[0]).load();
    }

    
    /**
     * Load all the reference data into the database
     * 
     * @throws Exception
     */
    private void load() 
    throws Exception {
        ReferenceData refData = (ReferenceData) ReferenceData.unmarshal(
                new FileReader(this.fileName));
        
        // process the country elements
        if (refData.getCountryRefDataCount() > 0) {
            processCountries(currentSession(), refData.getCountryRefData());
            
        }
        
        // process the species elements
        if (refData.getSpeciesRefDataCount() > 0) {
            processSpecies(currentSession(), refData.getSpeciesRefData());
        }
    }

    /**
     * Process all the  species records
     * 
     * @param session
     *            the hibernate session to use
     * @param speciesRefData
     *            the species records
     * @throws Exception
     *            propagate session to caller            
     */
    private void processSpecies(Session session, SpeciesRefData[] speciesRefData) 
    throws Exception {
        try {
            for (SpeciesRefData speciesElem : speciesRefData) {
                Transaction tx = currentSession().beginTransaction();
                Species species = findSpeciesByName(session, speciesElem.getName());
                if (species == null) {
                    species = new Species(speciesElem.getName(),
                            speciesElem.getAlternateName());
                    session.save(species);
                } else {
                    species.setAlternateName(speciesElem.getAlternateName());
                    species.getBreeds().clear();
                    session.update(species);
                }
                logger.debug("Loading Species Record: " + species.toString());
                
                // proces the breed associated with the specieis
                if (speciesElem.getBreedRefDataCount() > 0) {
                    for (BreedRefData breedElem : speciesElem.getBreedRefData()) {
                        Breed breed = findBreedByName(session, breedElem.getName());
                        if (breed == null) {
                            breed = new Breed(breedElem.getName(), 
                                breedElem.getCrossBreed());
                            session.save(breed);
                        } else {
                            breed.setCrossBreed(breedElem.getCrossBreed());
                            breed.getBreedColour().clear();
                            session.update(breed);
                        }
                        logger.debug("Loading Breed Record: " + breed);
                        
                        // process the colours for the breed
                        if (breedElem.getBreedColourRefDataCount() > 0) {
                            for (BreedColourRefData colourElem : breedElem.getBreedColourRefData()) {
                                BreedColour colour = findBreedColourByName(session, colourElem.getName());
                                if (colour == null) {
                                    colour = new BreedColour(
                                        colourElem.getName());
                                    session.save(colour);
                                }
                                breed.addBreedColour(colour);
                                logger.debug("Loading Breed Colour Record: " +
                                        colour);
                            }
                            session.update(breed);
                        }
                    }
                }
                tx.commit();
            }
        } finally {
            closeSession();
        }
    }

    /**
     * Process all the country records
     * 
     * @param session
     *            the hibernate session to use
     * @param countries
     *            the country records
     * @throws Exception
     *            propagate session to caller            
     */
    private void processCountries(Session session, CountryRefData[] countryRefData) 
    throws Exception {
        try {
            for (CountryRefData countryElem : countryRefData) {
                Transaction tx = currentSession().beginTransaction();
                Country country = findCountryByName(session, countryElem.getName());
                if (country == null) {
                    country = new Country(countryElem.getName(), 
                        countryElem.getCode(), countryElem.getCurrency());
                    session.save(country);
                } else {
                    // prep the country element
                    country.getStates().clear();
                    country.setCode(countryElem.getCode());
                    country.setCurrency(countryElem.getCurrency());
                    session.update(country);
                }
                logger.debug("Loading Country Record: " + country);
                
                // process the state reference data
                if (countryElem.getStateRefDataCount() > 0) {
                    for (StateRefData stateElem : countryElem.getStateRefData()) {
                        State state = findStateByName(session, stateElem.getName());
                        if (state == null) {
                            state = new State(stateElem.getName());
                            country.addState(state);
                            session.save(state);
                        } else {
                            state.getSuburbs().clear();
                            country.addState(state);
                            session.update(state);
                        }
                        logger.debug("Loading State Record: " + state);
                        
                        // process the suburb reference data
                        if (stateElem.getSuburbRefDataCount() > 0) {
                            for (SuburbRefData suburbElem : stateElem.getSuburbRefData()) {
                                Suburb suburb = findSuburbByPostCode(session, suburbElem.getPostCode());
                                if (suburb == null) {
                                    suburb = new Suburb(suburbElem.getName(), 
                                        suburbElem.getPostCode());
                                    state.addSuburb(suburb);
                                    session.save(suburb);
                                } else {
                                    suburb.setName(suburbElem.getName());
                                    suburb.setPostCode(suburbElem.getPostCode());
                                    state.addSuburb(suburb);
                                    session.update(suburb);
                                }
                                logger.debug("Loading Suburb Record: " + suburb);
                            }
                        }
                    }
                }
                tx.commit();
            }
        } finally {
            closeSession();
        }
    }
    
    /**
     * Find a suburb by postcode
     * 
     * @param session
     *            the hibernate session
     * @param postcode
     *            the postcode of the suburb
     * @return Suburb
     *            the suburb or null if it does not exist
     * @throws Exception            
     */
    private Suburb findSuburbByPostCode(Session session, String postCode) 
    throws Exception {
        Query query = session.getNamedQuery("suburb.getSuburbByPostCode");
        query.setParameter("postCode", postCode);
        List results = query.list();
        if (results.size() == 0) {
            return null;
        } else if (results.size() == 1) {
            return (Suburb)results.get(0);
        } else {
            throw new RuntimeException("Reference Data is inconsistent. " +
                    "Multiple suburbs with postCode " + postCode);
        }
    }

    /**
     * Find a state by name
     * 
     * @param session
     *            the hibernate session
     * @param name
     *            the name of the state
     * @return State
     *            the state or null if it does not exist
     * @throws Exception            
     */
    private State findStateByName(Session session, String name) 
    throws Exception {
        Query query = session.getNamedQuery("state.getStateByName");
        query.setParameter("name", name);
        List results = query.list();
        if (results.size() == 0) {
            return null;
        } else if (results.size() == 1) {
            return (State)results.get(0);
        } else {
            throw new RuntimeException("Reference Data is inconsistent. " +
                    "Multiple states with name " + name);
        }
    }

    /**
     * Find a country by name
     * 
     * @param session
     *            the hibernate session
     * @param name
     *            the name of the country
     * @return Country
     *            the country or null if it does not exist
     * @throws Exception            
     */
    private Country findCountryByName(Session session, String name)
    throws Exception {
        Query query = session.getNamedQuery("country.getCountryByName");
        query.setParameter("name", name);
        List results = query.list();
        if (results.size() == 0) {
            return null;
        } else if (results.size() == 1) {
            return (Country)results.get(0);
        } else {
            throw new RuntimeException("Reference Data is inconsistent. " +
                    "Multiple countries with name " + name);
        }
    }

    /**
     * Find a species by name
     * 
     * @param session
     *            the hibernate session
     * @param name
     *            the name of the species
     * @return Species
     *            the species or null if it does not exist
     * @throws Exception            
     */
    private Species findSpeciesByName(Session session, String name)
    throws Exception {
            Query query = session.getNamedQuery("species.getSpeciesByName");
            query.setParameter("name", name);
            List results = query.list();
            if (results.size() == 0) {
                return null;
            } else if (results.size() == 1) {
                return (Species)results.get(0);
            } else {
                throw new RuntimeException("Reference Data is inconsistent. " +
                        "Multiple species with name " + name);
            }
    }

    /**
     * Find a breed colour by name
     * 
     * @param session
     *            the hibernate session
     * @param name
     *            the name of the breed colour
     * @return BreedColour
     *            the species or null if it does not exist
     * @throws Exception            
     */
    private BreedColour findBreedColourByName(Session session, String name) 
    throws Exception {
        Query query = session.getNamedQuery("breedColour.getBreedColourByName");
        query.setParameter("name", name);
        List results = query.list();
        if (results.size() == 0) {
            return null;
        } else if (results.size() == 1) {
            return (BreedColour)results.get(0);
        } else {
            throw new RuntimeException("Reference Data is inconsistent. " +
                    "Multiple breed colours with name " + name);
        }
    }

    /**
     * Find a breed by name
     * 
     * @param session
     *            the hibernate session
     * @param name
     *            the name of the breed
     * @return Breed
     *            the species or null if it does not exist
     * @throws Exception            
     */
    private Breed findBreedByName(Session session, String name)
    throws Exception {
        Query query = session.getNamedQuery("breed.getBreedByName");
        query.setParameter("name", name);
        List results = query.list();
        if (results.size() == 0) {
            return null;
        } else if (results.size() == 1) {
            return (Breed)results.get(0);
        } else {
            throw new RuntimeException("Reference Data is inconsistent. " +
                    "Multiple breeds with name " + name);
        }
    }

    /**
     * Return a reference to the session factory
     * 
     * @return SessionFactory
     */
    private SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Get the current hibernate session
     * 
     * @return Session
     * @throws Exception
     */
    public Session currentSession() throws Exception {
        Session s = (Session) session.get();
        // Open a new Session, if this Thread has none yet
        if (s == null) {
            s = getSessionFactory().openSession();
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
}
