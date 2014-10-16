package com.sungard.scs.bds.contact.ews;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import com.sungard.scs.bds.contact.Contact;

@Ignore
public class EwsContactQueryServiceTest {

	private EwsContactQueryService ewsContactQueryService;

	@Before
	public void setUp() throws URISyntaxException {
		ewsContactQueryService = new EwsContactQueryService();
		ewsContactQueryService.init(new URI("https://owa.domain.com/ews/exchange.asmx"), "username", "password");
	}
	
	@Test
	public void simpleTest() throws Exception {
		List<Contact> result = ewsContactQueryService.findContacts("Firstname");
		
		System.out.println(result);
		
		assertTrue(result.size() > 0);
	}
	
}
