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
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.app.SimpleServer;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.v25.message.RDE_O11;
import ca.uhn.hl7v2.model.v25.message.RDS_O13;
import ca.uhn.hl7v2.model.v25.segment.FT1;
import ca.uhn.hl7v2.model.v25.segment.MSH;
import ca.uhn.hl7v2.model.v25.segment.RXD;
import ca.uhn.hl7v2.model.v25.segment.RXE;
import ca.uhn.hl7v2.model.v25.segment.RXO;
import ca.uhn.hl7v2.protocol.ReceivingApplication;
import ca.uhn.hl7v2.protocol.ReceivingApplicationException;
import ca.uhn.hl7v2.util.DeepCopy;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Test service that accepts RDE messages and sends corresponding RDS messages.
 *
 * @author Tim Anderson
 */
public class TestPharmacyService {

    /**
     * The HL7 server.
     */
    private final SimpleServer server;

    /**
     * Executor to schedule dispensing.
     */
    private final ScheduledExecutorService executor;

    /**
     * The context used send dispense messages back to OpenVPMS,
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
     * The number of dispenses for an order.
     */
    private final int dispenses;

    /**
     * If {@code true}, generate a return after a dispense.
     */
    private final boolean returnAfterDispense;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(TestPharmacyService.class);


    /**
     * Constructs an {@link TestPharmacyService}.
     *
     * @param port                the port to listen to messages from OpenVPMS on
     * @param outboundHost        the host that OpenVPMS is running on
     * @param outboundPort        the port that OpenVPMS is listening on
     * @param dispenses           the number of dispenses for an order
     * @param returnAfterDispense if {@code true}, generate a return after a dispense
     */
    public TestPharmacyService(int port, String outboundHost, int outboundPort, int dispenses,
                               boolean returnAfterDispense) {
        this.outboundHost = outboundHost;
        this.outboundPort = outboundPort;
        this.dispenses = dispenses;
        this.returnAfterDispense = returnAfterDispense;
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
                TestPharmacyService service = new TestPharmacyService(config.getInt("port"),
                                                                      config.getString("outboundhost"),
                                                                      config.getInt("outboundport"),
                                                                      config.getInt("dispenses"),
                                                                      config.getBoolean("return"));
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
        if (message instanceof RDE_O11) {
            RDE_O11 rde = (RDE_O11) message;
            if ("NW".equals(rde.getORDER().getORC().getOrderControl().getValue())) {
                queueDispense(rde);
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
     * Queues a dispense for an order.
     *
     * @param order the order
     */
    private void queueDispense(final RDE_O11 order) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                dispense(order);
            }
        };
        executor.schedule(runnable, 10, TimeUnit.SECONDS);
    }

    /**
     * Dispenses an order.
     *
     * @param order the order
     */
    private void dispense(RDE_O11 order) {
        Connection connection = null;
        try {
            connection = sendContext.newClient(outboundHost, outboundPort, false);

            RXO rxo = order.getORDER().getORDER_DETAIL().getRXO();
            BigDecimal quantity = new BigDecimal(rxo.getRequestedDispenseAmount().getValue());
            if (quantity.compareTo(BigDecimal.ONE) != 0 && dispenses != 1) {
                BigDecimal[] decimals = quantity.divideAndRemainder(BigDecimal.valueOf(dispenses));
                for (int i = 0; i < dispenses; ++i) {
                    BigDecimal qty = (i == 0) ? decimals[0].add(decimals[1]) : decimals[0];
                    RDS_O13 dispense = createRDS(order, qty);
                    send(dispense, connection);
                }
            } else {
                RDS_O13 dispense = createRDS(order, quantity);
                send(dispense, connection);
            }
            if (returnAfterDispense) {
                quantity = quantity.negate();
                RDS_O13 dispense = createRDS(order, quantity);
                send(dispense, connection);
            }
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
     * Creates an RDS^O13 message from an RDE^O11 order.
     *
     * @param order    the order
     * @param quantity the quantity to dispense
     * @return a new message
     * @throws HL7Exception for any HL7 exception
     * @throws IOException  for any I/O exception
     */
    private RDS_O13 createRDS(RDE_O11 order, BigDecimal quantity) throws HL7Exception, IOException {
        RDS_O13 message = new RDS_O13(sendContext.getModelClassFactory());
        message.setParser(sendContext.getGenericParser());
        message.initQuickstart("RDS", "O13", "P");
        MSH msh = message.getMSH();
        MSH orderMSH = order.getMSH();
        RXD rxd = message.getORDER().getRXD();
        RXE rxe = message.getORDER().getENCODING().getRXE();
        FT1 ft1 = message.getORDER().getFT1();
        RXO rxo = order.getORDER().getORDER_DETAIL().getRXO();

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
        message.getORDER().getORC().getOrderControl().setValue("RE");

        // populate RXE
        DeepCopy.copy(rxo.getRequestedGiveCode(), rxe.getGiveCode());
        DeepCopy.copy(rxo.getRequestedDispenseUnits(), rxe.getGiveUnits());
        DeepCopy.copy(rxo.getProviderSAdministrationInstructions(0), rxe.getProviderSAdministrationInstructions(0));
        rxe.getDispenseAmount().setValue(quantity.toString());
        DeepCopy.copy(rxo.getRequestedDispenseUnits(), rxe.getDispenseUnits());

        // populate RXD
        DeepCopy.copy(rxo.getRequestedGiveCode(), rxd.getDispenseGiveCode());
        rxd.getActualDispenseAmount().setValue(quantity.toString());
        DeepCopy.copy(rxo.getRequestedDispenseUnits(), rxd.getActualDispenseUnits());

        // populate FT1
        ft1.getSetIDFT1().setValue("1");
        ft1.getTransactionType().setValue("CG");
        ft1.getTransactionQuantity().setValue(quantity.toString());
        DeepCopy.copy(rxe.getGiveCode(), ft1.getTransactionCode());
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
        System.err.println("Usage: java " + TestPharmacyService.class.getName());
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
                                         .setStringParser(JSAP.INTEGER_PARSER).setDefault("10001")
                                         .setShortFlag('p').setLongFlag("port")
                                         .setHelp("The port to listen for messages on."));
        parser.registerParameter(new FlaggedOption("outboundhost")
                                         .setDefault("localhost")
                                         .setShortFlag('h').setLongFlag("outboundhost")
                                         .setHelp("The host to send outbound messages to."));
        parser.registerParameter(new FlaggedOption("outboundport")
                                         .setStringParser(JSAP.INTEGER_PARSER).setDefault("10000")
                                         .setShortFlag('o').setLongFlag("outboundport")
                                         .setHelp("The port to send outbound messages to."));
        parser.registerParameter(new FlaggedOption("dispenses")
                                         .setStringParser(JSAP.INTEGER_PARSER).setDefault("1")
                                         .setShortFlag('d').setLongFlag("dispenses")
                                         .setHelp("The number of dispenses for an order, if the order quantity > 1"));
        parser.registerParameter(new Switch("return")
                                         .setShortFlag('r').setLongFlag("return")
                                         .setHelp("Generate a return after dispense."));
        return parser;
    }
}
