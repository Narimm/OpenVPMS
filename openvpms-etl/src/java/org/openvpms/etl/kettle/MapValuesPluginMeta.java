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
package org.openvpms.etl.kettle;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.database.DatabaseMeta;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleXMLException;
import be.ibridge.kettle.repository.Repository;
import be.ibridge.kettle.trans.Trans;
import be.ibridge.kettle.trans.TransMeta;
import be.ibridge.kettle.trans.step.BaseStepMeta;
import be.ibridge.kettle.trans.step.StepDataInterface;
import be.ibridge.kettle.trans.step.StepDialogInterface;
import be.ibridge.kettle.trans.step.StepInterface;
import be.ibridge.kettle.trans.step.StepMeta;
import be.ibridge.kettle.trans.step.StepMetaInterface;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Shell;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.etl.load.Mapping;
import org.openvpms.etl.load.Mappings;
import org.openvpms.etl.load.NodeParser;
import org.openvpms.etl.load.ReferenceParser;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Node;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;


/**
 * The 'Map Values' plugin meta data.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class MapValuesPluginMeta extends BaseStepMeta
        implements StepMetaInterface {

    /**
     * The database.
     */
    private DatabaseMeta database;

    /**
     * The mappings.
     */
    private Mappings mappings = new Mappings();

    /**
     * The logger.
     */
    private final LogWriter log = LogWriter.getInstance();

    /**
     * Repository attribute names.
     */
    private static final String CONNECTION = "connection"; // NON-NLS
    private static final String ID_COLUMN = "idColumn"; // NON-NLS
    private static final String SOURCE = "source";  // NON-NLS
    private static final String TARGET = "target"; // NON-NLS
    private static final String VALUE = "value"; // NON-NLS
    private static final String EXCLUDE_NULL = "excludeNull"; // NON-NLS
    private static final String SKIP_PROCESSED = "skipProcessed"; // NON-NLS
    private static final String REMOVE_DEFAULT_OBJECTS
            = "removeDefaultObjects"; // NON-NLS


    /**
     * Constructs a new <tt>MapValuesPluginMeta</tt>.
     */
    public MapValuesPluginMeta() {
    }

    /**
     * Constructs a new <tt>MapValuesPluginMeta</tt> from an XML Node.
     *
     * @param stepNode  the Node to get the info from
     * @param databases the available list of databases to reference
     * @param counters  counters to reference
     * @throws KettleXMLException for any XML error
     */
    public MapValuesPluginMeta(Node stepNode, ArrayList databases,
                               Hashtable counters)
            throws KettleXMLException {
        loadXML(stepNode, databases, counters);
    }

    /**
     * Constructs a new <tt>MapValuesPluginMeta</tt> from a Kettle repository.
     *
     * @param repository the repository to read from
     * @param stepId     the step ID
     * @param databases  the databases to reference
     * @param counters   the counters to reference
     * @throws KettleException for any error
     */
    public MapValuesPluginMeta(Repository repository, long stepId,
                               ArrayList databases,
                               Hashtable counters)
            throws KettleException {
        readRep(repository, stepId, databases, counters);
    }

    /**
     * Produces the XML string that describes this step's information.
     *
     * @return String containing the XML describing this step.
     */
    @Override
    public String getXML() {
        String result = "";
        if (mappings != null) {
            StringWriter writer = new StringWriter();
            try {
                Marshaller marshaller = new Marshaller(writer);
                marshaller.setSupressXMLDeclaration(true);
                marshaller.marshal(mappings);
                result = writer.toString();
            } catch (Throwable exception) {
                log.println(LogWriter.LOG_LEVEL_ERROR, getClass().getName(),
                            exception.getMessage());
            }
        }
        return result;
    }

    /**
     * Load the values for this step from an XML Node.
     *
     * @param stepNode  the Node to get the info from
     * @param databases the available list of databases to reference to
     * @param counters  counters to reference
     * @throws KettleXMLException for any XML error
     */
    public void loadXML(Node stepNode, ArrayList databases,
                        Hashtable counters) throws KettleXMLException {
        try {
            Node node = XMLHandler.getSubNode(stepNode, "mappings"); // NON-NLS
            mappings = (Mappings) Unmarshaller.unmarshal(Mappings.class, node);
            database = Const.findDatabase(databases, mappings.getConnection());
        } catch (Exception exception) {
            throw new KettleXMLException(
                    "Unable to read step info from XML node", exception);
        }
    }

    /**
     * Set default values.
     */
    public void setDefault() {
        mappings = new Mappings();
    }

    /**
     * Read the steps information from a Kettle repository.
     *
     * @param repository the repository to read from
     * @param stepId     the step ID
     * @param databases  the databases to reference
     * @param counters   the counters to reference
     * @throws KettleException for any error
     */
    public void readRep(Repository repository, long stepId, ArrayList databases,
                        Hashtable counters) throws KettleException {
        mappings = new Mappings();
        String connection = repository.getStepAttributeString(
                stepId, CONNECTION);
        mappings.setConnection(connection);
        database = Const.findDatabase(databases, connection);

        String idColumn = repository.getStepAttributeString(
                stepId, ID_COLUMN);
        mappings.setIdColumn(idColumn);

        mappings.setSkipProcessed(repository.getStepAttributeBoolean(
                stepId, SKIP_PROCESSED));

        int count = repository.countNrStepAttributes(stepId, SOURCE);
        for (int i = 0; i < count; ++i) {
            String source = repository.getStepAttributeString(
                    stepId, i, SOURCE);
            String target = repository.getStepAttributeString(
                    stepId, i, TARGET);
            String value = repository.getStepAttributeString(
                    stepId, i, VALUE);
            boolean excludeNull = repository.getStepAttributeBoolean(
                    stepId, i, EXCLUDE_NULL);
            boolean removeDefaultObjects
                    = repository.getStepAttributeBoolean(
                    stepId, i, REMOVE_DEFAULT_OBJECTS);
            Mapping mapping = new Mapping();
            mapping.setSource(source);
            mapping.setTarget(target);
            mapping.setValue(value);
            mapping.setExcludeNull(excludeNull);
            mapping.setRemoveDefaultObjects(removeDefaultObjects);
            mappings.addMapping(mapping);
        }
    }

    /**
     * Save the step's data into a Kettle repository.
     *
     * @param repository       the Kettle repository to save to
     * @param transformationId the transformation ID
     * @param stepId           the step ID
     * @throws KettleException for any error
     */
    public void saveRep(Repository repository, long transformationId,
                        long stepId) throws KettleException {
        repository.saveStepAttribute(transformationId, stepId, CONNECTION,
                                     mappings.getConnection());
        repository.saveStepAttribute(transformationId, stepId, ID_COLUMN,
                                     mappings.getIdColumn());

        repository.saveStepAttribute(transformationId, stepId, SKIP_PROCESSED,
                                     mappings.getSkipProcessed());

        for (int i = 0; i < mappings.getMappingCount(); ++i) {
            Mapping mapping = mappings.getMapping(i);
            repository.saveStepAttribute(transformationId, stepId, i,
                                         SOURCE, mapping.getSource());
            repository.saveStepAttribute(transformationId, stepId, i,
                                         TARGET, mapping.getTarget());
            repository.saveStepAttribute(transformationId, stepId, i,
                                         VALUE, mapping.getValue());
            repository.saveStepAttribute(
                    transformationId, stepId, i, EXCLUDE_NULL,
                    mapping.getExcludeNull());
            repository.saveStepAttribute(
                    transformationId, stepId, i, REMOVE_DEFAULT_OBJECTS,
                    mapping.getRemoveDefaultObjects());
        }
    }

    /**
     * Checks the settings of this step and puts the findings in a remarks List.
     *
     * @param remarks  the list to put the remarks in
     * @param stepMeta the stepMeta to help checking
     * @param prev     the fields coming from the previous step
     * @param input    the input step names
     * @param output   the output step names
     * @param info     the fields that are used as information by the step
     */
    @SuppressWarnings({"unchecked", "HardCodedStringLiteral"})
    public void check(ArrayList remarks, StepMeta stepMeta, Row prev,
                      String input[], String output[], Row info) {
        if (database == null) {
            addRemark(remarks, CheckResult.TYPE_RESULT_ERROR, stepMeta,
                      "MapValuesPluginMeta.NoConnection");
        }

        if (prev == null || prev.size() == 0) {
            addRemark(remarks, CheckResult.TYPE_RESULT_WARNING,
                      stepMeta, "MapValuesPluginMeta.NoFields");
        } else {
            // check mappings
            String idColumn = mappings.getIdColumn();
            if (StringUtils.isEmpty(idColumn)) {
                addRemark(remarks, CheckResult.TYPE_RESULT_ERROR, stepMeta,
                          "MapValuesPluginMeta.NoId");
            } else {
                if (prev.searchValueIndex(idColumn) == -1) {
                    addRemark(remarks, CheckResult.TYPE_RESULT_ERROR, stepMeta,
                              "MapValuesPluginMeta.NoField", idColumn);
                }
            }
            IArchetypeService service = getService();
            for (Mapping mapping : mappings.getMapping()) {
                checkMapping(remarks, mapping, prev, stepMeta, service);
            }
            addRemark(remarks, CheckResult.TYPE_RESULT_OK, stepMeta,
                      "MapValuesPluginMeta.StepConnected", prev.size());
        }
        // See if we have input streams leading to this step
        if (input.length > 0) {
            addRemark(remarks, CheckResult.TYPE_RESULT_OK, stepMeta,
                      "MapValuesPlugin.StepReceiveInput");
        } else {
            addRemark(remarks, CheckResult.TYPE_RESULT_OK, stepMeta,
                      "MapValuesPlugin.StepReceiveNoInput");
        }
    }

    /**
     * Creates the dialog.
     *
     * @param shell     The shell to open the dialog on
     * @param meta      The step info
     * @param transMeta The transformation meta-data
     * @param stepname  The name of the step
     * @return a new {@link MapValuesPluginDialog}
     */
    public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta,
                                         TransMeta transMeta, String stepname) {
        return new MapValuesPluginDialog(shell, (MapValuesPluginMeta) meta,
                                         transMeta, stepname);
    }

    /**
     * Get the executing step, needed by Trans to launch a step.
     *
     * @param stepMeta  The step info
     * @param stepData  the step data interface linked to this step.  Here the
     *                  step can store temporary data, database connections, etc
     * @param copyNr    The copy nr to get
     * @param transMeta The transformation info
     * @param trans     The launching transformation
     * @return a new {@link MapValuesPlugin}
     */
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepData,
                                 int copyNr, TransMeta transMeta, Trans trans) {
        MapValuesPluginData data = (MapValuesPluginData) stepData;
        data.setContext(getContext());
        return new MapValuesPlugin(stepMeta, data, copyNr, transMeta, trans);
    }

    /**
     * Returns a new instance of the appropriate data class.
     * This data class implements the StepDataInterface.
     * It basically contains the persisting data that needs to live on, even if
     * a worker thread is terminated.
     *
     * @return a new {@link MapValuesPluginData}
     */
    public StepDataInterface getStepData() {
        return new MapValuesPluginData();
    }

    /**
     * Returns the mappings.
     *
     * @return the mappings
     */
    public Mappings getMappings() {
        return mappings;
    }

    /**
     * Sets the mappings.
     *
     * @param mappings the mappings
     */
    public void setMappings(Mappings mappings) {
        this.mappings = mappings;
    }

    /**
     * Gets the application context.
     *
     * @return the application context, or <tt>null</tt> if the load fails or
     *         the database is not specified
     */
    protected ApplicationContext getContext() {
        ApplicationContext context = null;
        if (database != null) {
            try {
                context = ApplicationContextMgr.getContext(database);
            } catch (Throwable exception) {
                log.println(LogWriter.LOG_LEVEL_ERROR, getClass().getName(),
                            exception.getMessage());
            }
        } else {
            log.println(LogWriter.LOG_LEVEL_ERROR, getClass().getName(),
                        Messages.get("MapValuesPluginMeta.NoConnection"));
        }

        return context;
    }

    /**
     * Helper to check a mapping.
     *
     * @param remarks  the remarks to add to
     * @param mapping  the mapping to check
     * @param stepMeta the step meta
     * @param service  the archetype service. May be <tt>null</tt>
     */
    private void checkMapping(ArrayList remarks, Mapping mapping,
                              Row row, StepMeta stepMeta,
                              IArchetypeService service) {
        if (row.searchValueIndex(mapping.getSource()) == -1) {
            addRemark(remarks, CheckResult.TYPE_RESULT_ERROR, stepMeta,
                      "MapValuesPluginMeta.NoField",
                      mapping.getSource());
        }
        org.openvpms.etl.load.Node node = NodeParser.parse(mapping.getTarget());
        if (node == null) {
            addRemark(remarks, CheckResult.TYPE_RESULT_ERROR, stepMeta,
                      "MapValuesPluginMeta.InvalidMapping",
                      mapping.getSource(), mapping.getTarget());
        } else if (service != null) {
            while (node != null) {
                checkNode(remarks, node, mapping, stepMeta, service);
                node = node.getChild();
            }
        }
    }

    /**
     * Helper to check a node mapping.
     *
     * @param remarks  the remarks to add to
     * @param node     the node to check
     * @param mapping  the mapping to check
     * @param stepMeta the step meta
     * @param service  the archetype service
     */
    private void checkNode(ArrayList remarks, org.openvpms.etl.load.Node node,
                           Mapping mapping,
                           StepMeta stepMeta, IArchetypeService service) {
        ArchetypeDescriptor archetype
                = service.getArchetypeDescriptor(node.getArchetype());
        if (archetype == null) {
            addRemark(remarks, CheckResult.TYPE_RESULT_ERROR, stepMeta,
                      "MapValuesPluginMeta.InvalidArchetype",
                      node.getArchetype(), mapping.getTarget());
        } else {
            NodeDescriptor descriptor = archetype.getNodeDescriptor(
                    node.getName());
            if (descriptor == null) {
                addRemark(remarks, CheckResult.TYPE_RESULT_ERROR,
                          stepMeta,
                          "MapValuesPluginMeta.InvalidNode",
                          node.getArchetype(), node.getName(),
                          mapping.getTarget());
            } else {
                boolean checkReference = false;
                if (descriptor.isCollection()) {
                    checkReference = true;
                    if (node.getIndex() < 0) {
                        addRemark(remarks, CheckResult.TYPE_RESULT_ERROR,
                                  stepMeta,
                                  "MapValuesPluginMeta.ExpectedCollection",
                                  node.getArchetype(), node.getName(),
                                  mapping.getTarget());
                    }
                } else {
                    if (node.getIndex() >= 0) {
                        addRemark(remarks, CheckResult.TYPE_RESULT_ERROR,
                                  stepMeta,
                                  "MapValuesPluginMeta.NodeNotCollection",
                                  node.getArchetype(), node.getName(),
                                  mapping.getTarget());
                    }
                    if (descriptor.isObjectReference()) {
                        checkReference = true;
                    }
                }
                if (checkReference &&
                        !StringUtils.isEmpty(mapping.getValue())) {
                    if (ReferenceParser.parse(mapping.getValue()) == null) {
                        addRemark(remarks, CheckResult.TYPE_RESULT_ERROR,
                                  stepMeta,
                                  "MapValuesPluginMeta.InvalidReference",
                                  mapping.getValue(), mapping.getTarget());
                    }
                }
            }
        }
    }

    /**
     * Helper to add a new <tt>CheckResult</tt> to remarks.
     *
     * @param remarks  the remarks
     * @param type     the result type
     * @param stepMeta the step meta
     * @param key      the localisation message key
     * @param args     the localisation message args
     */
    @SuppressWarnings("unchecked")
    private void addRemark(ArrayList remarks, int type, StepMeta stepMeta,
                           String key, Object ... args) {
        CheckResult remark = new CheckResult(type, Messages.get(key, args),
                                             stepMeta);
        remarks.add(remark);
    }

    /**
     * Helper to return the archetype service.
     *
     * @return the archetype service, or <tt>null</tt> if it cannot be
     *         initialised
     */
    private IArchetypeService getService() {
        ApplicationContext context = getContext();
        if (context != null) {
            return (IArchetypeService) context.getBean(
                    "archetypeService"); // NON-NLS
        }
        return null;
    }


}
