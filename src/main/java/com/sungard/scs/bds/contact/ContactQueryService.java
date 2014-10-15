package com.sungard.scs.bds.contact;

import java.net.URI;
import java.util.List;

public interface ContactQueryService {
	
	void init(URI uri, String username, String password) throws Exception;
	
	List<Contact> findContacts(String name) throws Exception;
	
}
