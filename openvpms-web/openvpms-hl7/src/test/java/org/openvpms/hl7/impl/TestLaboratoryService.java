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

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.ORM_O01;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.util.DeepCopy;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Test service that accepts ORM messages, and may be configured to send a cancellation for every N order.
 *
 * @author Tim Anderson
 */
public class TestLaboratoryService {

    /**
     * The HL7 server.
     */
    private final SimpleServer server;

    /**
     * Executor to schedule dispensing.
     */
    private final ScheduledExecutorService executor;

    /**
     * The context used send cancellation messages back to OpenVPMS,
     */
    private final HapiContext sendContext;

    /**
     * The outbound host name.
     */
    private final String outboundHost;

    /**
     * The outbound port.
     */
    private final int outboundPort;

    /**
     * Cancel every N orders.
     */
    private final int cancelEvery;

    /**
     * The no. of received orders.
     */
    private int received;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(TestLaboratoryService.class);


    /**
     * Constructs an {@link TestLaboratoryService}.
     *
     * @param port         the port to listen to messages from OpenVPMS on
     * @param outboundHost the host that OpenVPMS is running on
     * @param outboundPort the port that OpenVPMS is listening on
     * @param cancelEvery  cancel every Nth order. Use {@code 0} to never initiate a cancel
     */
    public TestLaboratoryService(int port, String outboundHost, int outboundPort, int cancelEvery) {
        this.outboundHost = outboundHost;
        this.outboundPort = outboundPort;
        this.cancelEvery = cancelEvery;
        executor = Executors.newSingleThreadScheduledExecutor();
        server = new SimpleServer(port);
        sendContext = HapiContextFactory.create();
        server.registerApplication(new ReceivingApplication() {
            @Override
            public Message processMessage(Message message, Map<String, Object> metaData)
                    throws ReceivingApplicationException, HL7Exception {
                return process(message);
            }

            @Override
            public boolean canProcess(Message theMessage) {
                return true;
            }
        });
    }

    /**
     * Start listening for connections.
     */
    public void start() {
        server.start();
    }

    /**
     * Main line.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            JSAP parser = createParser();
            JSAPResult config = parser.parse(args);
            if (config.success()) {
                TestLaboratoryService service = new TestLaboratoryService(config.getInt("port"),
                                                                          config.getString("outboundhost"),
                                                                          config.getInt("outboundport"),
                                                                          config.getInt("cancel"));
                service.start();
            } else {
                displayUsage(parser, config);
                System.exit(1);
            }
        } catch (Throwable exception) {
            exception.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Processes a message.
     *
     * @param message the message to process
     * @return the response
     * @throws HL7Exception                  for any HL7 error
     * @throws ReceivingApplicationException for any application error
     */
    private Message process(Message message) throws HL7Exception, ReceivingApplicationException {
        log.info("received: " + HL7MessageHelper.toString(message));
        if (message instanceof ORM_O01) {
            ++received;
            if (cancelEvery != 0 && (received % cancelEvery == 0)) {
                queueCancel((ORM_O01) message);
            }
        }

        try {
            Message response = message.generateACK();
            log.info("sending: " + HL7MessageHelper.toString(response));
            return response;
        } catch (IOException exception) {
            throw new ReceivingApplicationException(exception);
        }
    }

    /**
     * Queues a cancellation for an order.
     *
     * @param order the order
     */
    private void queueCancel(final ORM_O01 order) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                cancel(order);
            }
        };
        executor.schedule(runnable, 10, TimeUnit.SECONDS);
    }

    /**
     * Cancels an order.
     *
     * @param order the order
     */
    private void cancel(ORM_O01 order) {
        Connection connection = null;
        try {
            connection = sendContext.newClient(outboundHost, outboundPort, false);
            ORM_O01 cancellation = createCancellation(order);
            send(cancellation, connection);
        } catch (Throwable exception) {
            exception.printStackTrace();
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Sends a message.
     *
     * @param message    the message to send
     * @param connection the connection to use
     * @throws HL7Exception for any HL7 error
     * @throws LLPException for any LLP error
     * @throws IOException  for any I/O erro
     */
    private void send(Message message, Connection connection) throws HL7Exception, LLPException, IOException {
        log.info("sending: " + HL7MessageHelper.toString(message));
        Message response = connection.getInitiator().sendAndReceive(message);
        log.info("received: " + HL7MessageHelper.toString(response));
    }

    /**
     * Creates a new ORM_O01 to cancel an existing order.
     *
     * @param order the order
     * @return a new message
     * @throws HL7Exception for any HL7 exception
     * @throws IOException  for any I/O exception
     */
    private ORM_O01 createCancellation(ORM_O01 order) throws HL7Exception, IOException {
        ORM_O01 message = new ORM_O01(sendContext.getModelClassFactory());
        message.setParser(sendContext.getGenericParser());
        message.initQuickstart("ORM", "O01", "P");
        MSH msh = message.getMSH();
        MSH orderMSH = order.getMSH();

        // populate header
        DeepCopy.copy(orderMSH.getReceivingApplication(), msh.getSendingApplication());
        DeepCopy.copy(orderMSH.getReceivingFacility(), msh.getSendingFacility());
        DeepCopy.copy(orderMSH.getSendingApplication(), msh.getReceivingApplication());
        DeepCopy.copy(orderMSH.getSendingFacility(), msh.getReceivingFacility());

        // populate PID
        DeepCopy.copy(order.getPATIENT().getPID(), message.getPATIENT().getPID());

        // populate PV1
        DeepCopy.copy(order.getPATIENT().getPATIENT_VISIT().getPV1(),
                      message.getPATIENT().getPATIENT_VISIT().getPV1());

        // populate ORC
        DeepCopy.copy(order.getORDER().getORC(), message.getORDER().getORC());
        message.getORDER().getORC().getOrderControl().setValue("CA");

        return message;
    }

    /**
     * Prints usage information.
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
        System.err.println("Usage: java " + TestLaboratoryService.class.getName());
        System.err.println("                " + parser.getUsage());
        System.err.println();
        System.err.println(parser.getHelp());
    }

    /**
     * Creates a new command line parser.
     *
     * @return a new parser
     * @throws JSAPException for any JSAP error
     */
    private static JSAP createParser() throws JSAPException {
        JSAP parser = new JSAP();
        parser.registerParameter(new FlaggedOption("port")
                                         .setStringParser(JSAP.INTEGER_PARSER).setDefault("10003")
                                         .setShortFlag('p').setLongFlag("port")
                                         .setHelp("The port to listen for messages on."));
        parser.registerParameter(new FlaggedOption("outboundhost")
                                         .setDefault("localhost")
                                         .setShortFlag('h').setLongFlag("outboundhost")
                                         .setHelp("The host to send outbound messages to."));
        parser.registerParameter(new FlaggedOption("outboundport")
                                         .setStringParser(JSAP.INTEGER_PARSER).setDefault("10002")
                                         .setShortFlag('o').setLongFlag("outboundport")
                                         .setHelp("The port to send outbound messages to."));
        parser.registerParameter(new FlaggedOption("cancel")
                                         .setStringParser(JSAP.INTEGER_PARSER).setDefault("0")
                                         .setShortFlag('c').setLongFlag("cancel")
                                         .setHelp("Cancel every N orders"));
        return parser;
    }
}
