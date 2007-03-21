package org.openvpms.etl.kettle;

import be.ibridge.kettle.core.CheckResult;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.Row;
import be.ibridge.kettle.core.exception.KettleException;
import be.ibridge.kettle.core.exception.KettleStepException;
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
import org.exolab.castor.xml.Unmarshaller;
import org.w3c.dom.Node;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;

public class MapValuesPluginMeta extends BaseStepMeta
        implements StepMetaInterface {

    /**
     * The mappings.
     */
    private Mappings mappings = new Mappings();

    /**
     * The logger.
     */
    private final LogWriter log = LogWriter.getInstance();


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
                mappings.marshal(writer);
                result = writer.toString();
            } catch (Throwable exception) {
                log.println(LogWriter.LOG_LEVEL_ERROR, getClass().getName(),
                            exception.getMessage());
            }
        }
        return result;
    }

    /**
     * Determines which fields are added to the stream.
     *
     * @param row  the row containing fields that are used as input for the
     *             step.
     * @param name the name of the step
     * @param info the fields used as extra lookup information
     * @return the fields that are being put out by this step
     */
    @Override
    public Row getFields(Row row, String name, Row info)
            throws KettleStepException {
        if (row == null) {
            row = new Row();
        } else {
            // Note. The implementation of this is wierd. Kettle wants the input
            // row modified.
            //row.clear();
        }
        return row;
    }

    /**
     * Clones this.
     *
     * @return a clone of this
     */
    public Object clone() {
        return super.clone();
    }

    /**
     * Load the values for this step from an XML Node
     *
     * @param stepNode  the Node to get the info from
     * @param databases The available list of databases to reference to
     * @param counters  Counters to reference.
     * @throws KettleXMLException When an unexpected XML error occurred. (malformed etc.)
     */
    public void loadXML(Node stepNode, ArrayList databases,
                        Hashtable counters) throws KettleXMLException {
        try {
            mappings = (Mappings) Unmarshaller.unmarshal(Mappings.class,
                                                         stepNode);
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
     * @throws KettleException When an unexpected error occurred
     *                         (database, network, etc)
     */
    public void readRep(Repository repository, long stepId, ArrayList databases,
                        Hashtable counters) throws KettleException {
        mappings = new Mappings();

        String connection = repository.getStepAttributeString(stepId,
                                                              "connection");
        mappings.setConnection(connection);

        String idColumn = repository.getStepAttributeString(stepId, "idColumn");
        mappings.setIdColumn(idColumn);

        int count = repository.countNrStepAttributes(stepId, "source");
        for (int i = 0; i < count; ++i) {
            String source = repository.getStepAttributeString(stepId, i,
                                                              "source");
            String target = repository.getStepAttributeString(stepId, i,
                                                              "target");
            String value = repository.getStepAttributeString(stepId, i,
                                                             "value");
            boolean reference = repository.getStepAttributeBoolean(
                    stepId, i, "isReference");
            boolean excludeNull = repository.getStepAttributeBoolean(
                    stepId, i, "excludeNull");
            Mapping mapping = new Mapping();
            mapping.setSource(source);
            mapping.setTarget(target);
            mapping.setValue(value);
            mapping.setIsReference(reference);
            mapping.setExcludeNull(excludeNull);
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
        repository.saveStepAttribute(transformationId, stepId, "connection",
                                     mappings.getConnection());
        repository.saveStepAttribute(transformationId, stepId, "idColumn",
                                     mappings.getIdColumn());
        for (int i = 0; i < mappings.getMappingCount(); ++i) {
            Mapping mapping = mappings.getMapping(i);
            repository.saveStepAttribute(transformationId, stepId, i,
                                         "source", mapping.getSource());
            repository.saveStepAttribute(transformationId, stepId, i,
                                         "target", mapping.getTarget());
            repository.saveStepAttribute(transformationId, stepId, i,
                                         "value", mapping.getValue());
            repository.saveStepAttribute(
                    transformationId, stepId, i, "isReference",
                    mapping.getIsReference());
            repository.saveStepAttribute(
                    transformationId, stepId, i, "excludeNull",
                    mapping.getExcludeNull());
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
    @SuppressWarnings("unchecked")
    public void check(ArrayList remarks, StepMeta stepMeta, Row prev,
                      String input[], String output[], Row info) {
        CheckResult cr;
        if (prev == null || prev.size() == 0) {
            cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING,
                                 "Not receiving any fields from previous steps",
                                 stepMeta);
            remarks.add(cr);
        } else {
            String idColumn = mappings.getIdColumn();
            if (StringUtils.isEmpty(idColumn)) {
                cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
                                     "No id column specified", stepMeta);
                remarks.add(cr);
            } else {
                if (prev.searchValueIndex(idColumn) == -1) {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
                                         "Field not in previous step: "
                                                 + idColumn, stepMeta);
                    remarks.add(cr);
                }
            }
            for (Mapping mapping : mappings.getMapping()) {
                if (prev.searchValueIndex(mapping.getSource()) == -1) {
                    cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
                                         "Field not in previous step: "
                                                 + mapping.getSource(),
                                         stepMeta);
                    remarks.add(cr);
                }
            }
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
                                 "Step is connected to previous one, receiving "
                                         + prev.size() + " fields",
                                 stepMeta);
            remarks.add(cr);
        }
        // See if we have input streams leading to this step
        if (input.length > 0) {
            cr = new CheckResult(CheckResult.TYPE_RESULT_OK,
                                 "Step is receiving info from other steps.",
                                 stepMeta);
            remarks.add(cr);
        } else {
            cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR,
                                 "No input received from other steps",
                                 stepMeta);
            remarks.add(cr);
        }
    }

    /**
     * We know which dialog to open...
     *
     * @param shell     The shell to open the dialog on
     * @param meta      The step info
     * @param transMeta The transformation meta-data
     * @param stepname  The name of the step
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
     */
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepData,
                                 int copyNr, TransMeta transMeta, Trans trans) {
        MapValuesPluginData data = (MapValuesPluginData) stepData;
        if (mappings.getConnection() != null) {
            data.setDatabase(transMeta.findDatabase(mappings.getConnection()));
        }
        return new MapValuesPlugin(stepMeta, data,
                                   copyNr, transMeta, trans);
    }

    /**
     * Returns a new instance of the appropriate data class.
     * This data class implements the StepDataInterface.
     * It basically contains the persisting data that needs to live on, even if
     * a worker thread is terminated.
     *
     * @return The appropriate StepDataInterface class.
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

}
