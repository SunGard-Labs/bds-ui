package com.sungard.scs.bds;

import java.io.PrintStream;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

public class SOAPLoggingHandler implements SOAPHandler<SOAPMessageContext> {
	private static PrintStream out = System.out;

	public Set<QName> getHeaders() {
		return null;
	}

	public boolean handleMessage(SOAPMessageContext smc) {
		logToSystemOut(smc);
		return true;
	}

	public boolean handleFault(SOAPMessageContext smc) {
		logToSystemOut(smc);
		return true;
	}

	public void close(MessageContext messageContext) {
	}

	private void logToSystemOut(SOAPMessageContext smc) {
		Boolean outboundProperty = (Boolean) smc.get("javax.xml.ws.handler.message.outbound");

		if (outboundProperty.booleanValue())
			out.println("\nOutbound message:");
		else {
			out.println("\nInbound message:");
		}

		SOAPMessage message = smc.getMessage();
		try {
			message.writeTo(out);
			out.println("");
		} catch (Exception e) {
			out.println("Exception in handler: " + e);
		}
	}
}
