package com.sungard.scs.bds.contact;

import java.net.URI;
import java.util.List;
import java.util.ServiceLoader;

import org.jdesktop.application.Application;
import org.jdesktop.application.Task;

public class ContactQueryTask extends Task<List<Contact>, Void>{
	private final String uri;
	private final String username;
	private final String password;
	private final String contactToFind;
	
	public ContactQueryTask(Application application, String uri, String username, String password, String contactToFind) {
		super(application);
		this.uri = uri;
		this.username = username;
		this.password = password;
		
		this.contactToFind = contactToFind;
	}
	
	@Override
	protected List<Contact> doInBackground() throws Exception {
		//Load service
		ServiceLoader<ContactQueryService> sl = ServiceLoader.load(ContactQueryService.class);
		if (sl.iterator().hasNext()) {
			ContactQueryService service = sl.iterator().next();
			service.init(new URI(uri), username, password);
			//query and search
			return service.findContacts(contactToFind);
		} else {
			throw new IllegalStateException("No ContactQueryService found!");
		}
	}

}
