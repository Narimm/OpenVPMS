package org.openvpms.tools.archetype.diff;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.BasicConfigurator;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptorWriter;
import org.openvpms.component.business.domain.im.archetype.descriptor.AssertionDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.tools.archetype.comparator.ArchetypeChange;
import org.openvpms.tools.archetype.comparator.ArchetypeComparator;
import org.openvpms.tools.archetype.comparator.DescriptorChange;
import org.openvpms.tools.archetype.comparator.FieldChange;
import org.openvpms.tools.archetype.comparator.NodeChange;
import org.openvpms.tools.archetype.comparator.NodeFieldChange;
import org.openvpms.tools.archetype.io.ArchetypeServiceLoader;
import org.openvpms.tools.archetype.io.DescriptorLoader;
import org.openvpms.tools.archetype.io.FileSystemLoader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Tool to compare archetypes.
 *
 * @author Tim Anderson
 */
public class ArchDiff {

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(ArchDiff.class);

    /**
     * The default name of the application context file.
     */
    private final static String APPLICATION_CONTEXT = "applicationContext.xml";

    /**
     * The archetype descriptor comparator.
     */
    private final ArchetypeComparator comparator = new ArchetypeComparator();

    /**
     * The descriptor writer.
     */
    private ArchetypeDescriptorWriter writer;


    /**
     * Constructs an {@link ArchDiff}/
     */
    public ArchDiff() {
        writer = new ArchetypeDescriptorWriter(true, true);
    }

    /**
     * Compares two archetypes.
     *
     * @param oldVersion the old version of the descriptors
     * @param newVersion the new version of the descriptors
     * @param verbose    if {@code true} display all changes, otherwise display a synopsis
     * @throws IOException for any I/O error
     */
    public void compare(DescriptorLoader oldVersion, DescriptorLoader newVersion, boolean verbose) throws IOException {
        Map<String, ArchetypeDescriptor> oldDescriptors = oldVersion.getDescriptors();
        Map<String, ArchetypeDescriptor> newDescriptors = newVersion.getDescriptors();
        if (oldDescriptors.isEmpty()) {
            System.err.println("No archetypes found in " + oldVersion);
        } else if (newDescriptors.isEmpty()) {
            System.err.println("No archetypes found in " + newVersion);
        } else {
            Set<String> shortNames = new TreeSet<String>();
            if (oldVersion.isAll() == newVersion.isAll()) {
                // comparing:
                // . all known old descriptors with all known new descriptors, or
                // . a subset of old descriptors with a subset of new descriptors
                shortNames.addAll(oldDescriptors.keySet());
                shortNames.addAll(newDescriptors.keySet());
            } else if (oldVersion.isAll()) {
                // only comparing a subset of new descriptors
                shortNames.addAll(newDescriptors.keySet());
            } else {
                // only comparing a subset of old descriptors
                shortNames.addAll(oldDescriptors.keySet());
            }
            for (String shortName : shortNames) {
                ArchetypeDescriptor oldDescriptor = oldDescriptors.get(shortName);
                ArchetypeDescriptor newDescriptor = newDescriptors.get(shortName);
                compare(oldDescriptor, newDescriptor, verbose);
            }
        }

    }

    /**
     * Lists archetypes.
     *
     * @param loader the archetype loader
     * @throws IOException for any I/O error
     */
    public void list(DescriptorLoader loader, boolean verbose) throws IOException {
        if (verbose) {
            System.out.println("Retrieving archetype descriptors from: " + loader);
        }
        Map<String, ArchetypeDescriptor> descriptors = loader.getDescriptors();
        if (verbose) {
            System.out.println("Found " + descriptors.size() + " descriptors");
        }
        for (String shortName : new TreeSet<String>(descriptors.keySet())) {
            System.out.println(shortName);
        }
    }

    /**
     * Main line.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        BasicConfigurator.configure();

        try {
            JSAP parser = createParser();
            JSAPResult config = parser.parse(args);
            if (!config.success()) {
                displayUsage(parser, config);
            } else {
                boolean recurse = !config.getBoolean("no-recurse");
                boolean verbose = config.getBoolean("verbose");
                String contextPath = config.getString("context");
                String version1 = config.getString("version1");
                String version2 = config.getString("version2");

                if (!StringUtils.isEmpty(version1) && !StringUtils.isEmpty(version2)) {
                    ApplicationContext context;
                    if (!new File(contextPath).exists()) {
                        context = new ClassPathXmlApplicationContext(contextPath);
                    } else {
                        context = new FileSystemXmlApplicationContext(contextPath);
                    }
                    IArchetypeService service = (IArchetypeService) context.getBean("archetypeService");
                    ArchDiff diff = new ArchDiff();
                    if (config.getBoolean("list")) {
                        DescriptorLoader loader = getDescriptorLoader(version1, service, recurse);
                        diff.list(loader, verbose);
                    } else {
                        DescriptorLoader oldVersion = getDescriptorLoader(version1, service, recurse);
                        DescriptorLoader newVersion = getDescriptorLoader(version2, service, recurse);
                        diff.compare(oldVersion, newVersion, verbose);
                    }
                } else {
                    displayUsage(parser, config);
                }
            }
        } catch (Throwable throwable) {
            log.error(throwable, throwable);
        }
    }

    /**
     * Compares two archetypes.
     *
     * @param oldVersion the old version. May be {@code null}
     * @param newVersion the new version. May be {@code null}
     * @param verbose    if {@code true} display all changes, otherwise display a synopsis
     */
    private void compare(ArchetypeDescriptor oldVersion, ArchetypeDescriptor newVersion, boolean verbose)
            throws IOException {
        ArchetypeChange change = comparator.compare(oldVersion, newVersion);
        if (change != null) {
            System.out.println(getChangeType(change) + " " + change.getShortName());
            if (verbose && change.isUpdate()) {
                for (FieldChange field : change.getFieldChanges()) {
                    System.out.println("  " + getChangeType(field) + " " + field.getField() + " "
                                       + field.getOldVersion() + " -> " + field.getNewVersion());
                }
                for (NodeChange node : change.getNodeChanges()) {
                    System.out.println("  " + getChangeType(node) + " " + node.getName());
                    for (NodeFieldChange fieldChange : node.getChanges()) {
                        System.out.println("    " + fieldChange.getField());
                        print(fieldChange.getField(), fieldChange.getOldVersion());
                        System.out.println("    ->");
                        print(fieldChange.getField(), fieldChange.getNewVersion());
                    }
                }
            }
        }
    }

    private void print(NodeFieldChange.Field type, Object value) throws IOException {
        String result;
        if (type == NodeFieldChange.Field.ASSERTION && value instanceof AssertionDescriptor) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            writer.write((AssertionDescriptor) value, stream);
            result = stream.toString("UTF-8");
        } else {
            result = value != null ? value.toString() : "<empty>";
        }
        String[] values = result.split("\n");
        for (String line : values) {
            System.out.println("       " + line);
        }
    }

    private static DescriptorLoader getDescriptorLoader(String source, IArchetypeService service, boolean recurse) {
        if ("db".equals(source)) {
            return new ArchetypeServiceLoader(service);
        } else {
            return new FileSystemLoader(source, recurse);
        }
    }

    /**
     * Returns the change type.
     *
     * @param change the change
     * @return "A", "U", or "D", denoting an addition, update or delete
     */
    private String getChangeType(DescriptorChange change) {
        String type;
        if (change.isAdd()) {
            type = "A";
        } else if (change.isUpdate()) {
            type = "U";
        } else {
            type = "D";
        }
        return type;
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException if the parser can't be created
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new Switch("verbose")
                                         .setShortFlag('v')
                                         .setLongFlag("verbose")
                                         .setDefault("false")
                                         .setHelp("Displays verbose info to the console."));
        parser.registerParameter(new Switch("no-recurse")
                                         .setShortFlag('n')
                                         .setLongFlag("no-recurse")
                                         .setDefault("false")
                                         .setHelp("Disable search of subdirectories."));
        parser.registerParameter(new Switch("list")
                                         .setShortFlag('l')
                                         .setLongFlag("list")
                                         .setDefault("false")
                                         .setHelp("List archetypes."));
        parser.registerParameter(new FlaggedOption("context")
                                         .setLongFlag("context")
                                         .setDefault(APPLICATION_CONTEXT)
                                         .setHelp("The application context path"));
        parser.registerParameter(new UnflaggedOption("version1")
                                         .setRequired(true)
                                         .setHelp("The first version to compare."));
        parser.registerParameter(new UnflaggedOption("version2")
                                         .setRequired(false)
                                         .setDefault("database")
                                         .setHelp("The second version to compare."));
        return parser;
    }

    /**
     * Prints usage information and exits.
     *
     * @param parser the parser
     * @param result the parse result
     */
    private static void displayUsage(JSAP parser, JSAPResult result) {
        Iterator iter = result.getErrorMessageIterator();
        while (iter.hasNext()) {
            System.err.println(iter.next());
        }
        System.err.println();
        System.err.println("Usage: archdiff " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
        System.err.println("Versions");
        System.err.println();
        System.err.println("The version1 and version2 arguments specify the older and newer versions of archetypes.");
        System.err.println("They may be:");
        System.err.println(". an .adl file");
        System.err.println(". a directory. All .adl files in the directory will be read.");
        System.err.println(". database (or db) - all archetypes in the database will be read");

        System.err.println("Examples:");
        System.err.println("1. Compare a directory with archetypes in the database");
        System.err.println("> archdiff -v ../archetypes");
        System.err.println(" The above is short-hand for: ");
        System.err.println("> archdiff -v ../archetypes db");
        System.err.println();
        System.err.println("2. Compare a file with a prior version in the database");
        System.err.println("> archdiff -v db ../archetypes/contact.location.adl");
        System.err.println();
        System.err.println("3. Compare two directories containing archetypes");
        System.err.println("> archdiff 1.5/archetypes 1.6/archetypes");
        System.err.println();
        System.err.println("4. List archetypes in the database");
        System.err.println("> archdiff -l db");

        System.exit(1);
    }

}
