package com.sungard.scs.bds.contact.ews;

import java.net.URI;

import microsoft.exchange.webservices.data.Contact;
import microsoft.exchange.webservices.data.EmailAddressKey;
import microsoft.exchange.webservices.data.ExchangeCredentials;
import microsoft.exchange.webservices.data.ExchangeService;
import microsoft.exchange.webservices.data.ExchangeVersion;
import microsoft.exchange.webservices.data.FindItemsResults;
import microsoft.exchange.webservices.data.Item;
import microsoft.exchange.webservices.data.ItemSchema;
import microsoft.exchange.webservices.data.ItemView;
import microsoft.exchange.webservices.data.LogicalOperator;
import microsoft.exchange.webservices.data.SearchFilter;
import microsoft.exchange.webservices.data.WebCredentials;
import microsoft.exchange.webservices.data.WellKnownFolderName;

import org.junit.Test;
import org.junit.Ignore;

@Ignore
public class AddressBookTest {

	@Test
	public void testAddressBookAccess() throws Exception {
		ExchangeService service = new ExchangeService(ExchangeVersion.Exchange2010);

		ExchangeCredentials credentials = new WebCredentials("Username", "Password");
		service.setCredentials(credentials);
		service.setUrl(new URI("https://domain.com/EWS/Exchange.asmx"));
		ItemView view = new ItemView (10);
		
		
		FindItemsResults<Item> findResults = service.findItems(WellKnownFolderName.Contacts, 
				 new SearchFilter.ContainsSubstring(ItemSchema.Subject, "peter"),view);
					System.out.println("Found items: " + findResults.getTotalCount());
			for(Item item : findResults.getItems())
			 {
				//Do something with the item as shown
				System.out.println("id==========" + item.getId());
				System.out.println("sub==========" + item.getSubject());
				Contact contact = (Contact) item;
				if (contact.getEmailAddresses().contains(EmailAddressKey.EmailAddress1))
					System.out.println("sub==========" + contact.getEmailAddresses().getEmailAddress(EmailAddressKey.EmailAddress1));
			 }


		
	}
}
