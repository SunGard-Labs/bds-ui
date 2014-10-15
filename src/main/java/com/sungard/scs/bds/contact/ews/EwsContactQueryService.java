package com.sungard.scs.bds.contact.ews;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import microsoft.exchange.webservices.data.EmailAddress;
import microsoft.exchange.webservices.data.ExchangeCredentials;
import microsoft.exchange.webservices.data.ExchangeService;
import microsoft.exchange.webservices.data.ExchangeVersion;
import microsoft.exchange.webservices.data.NameResolution;
import microsoft.exchange.webservices.data.NameResolutionCollection;
import microsoft.exchange.webservices.data.ResolveNameSearchLocation;
import microsoft.exchange.webservices.data.WebCredentials;

import com.sungard.scs.bds.contact.Contact;
import com.sungard.scs.bds.contact.ContactImpl;
import com.sungard.scs.bds.contact.ContactQueryService;

public class EwsContactQueryService implements ContactQueryService {
	private ExchangeService service;
	
	@Override
	public void init(URI uri, String username, String password) {
		service = new ExchangeService(ExchangeVersion.Exchange2010_SP2);

		ExchangeCredentials credentials = new WebCredentials(username, password);
		service.setCredentials(credentials);
		
		service.setUrl(uri);
	}
	
	@Override
	public List<Contact> findContacts(String name) throws Exception {
		List<Contact> retValue = new ArrayList<>();
		
		//Contact seems to be always null even with returnContactDetails=true
		NameResolutionCollection searchResults = service.resolveName(name, ResolveNameSearchLocation.ContactsThenDirectory, false);

		for(NameResolution searchResult : searchResults) {
			 EmailAddress mailbox = searchResult.getMailbox();
			 
			 String label = mailbox.getName();
			 String address = mailbox.getAddress();
			 
			 Contact c = new ContactImpl(address, label);
			 retValue.add(c);
		}
		
		return retValue;
	}

}
