package com.sungard.scs.bds.contact;

import java.io.Serializable;

public interface Contact extends Serializable, Comparable<Contact> {

	String getAddress();
	String getName();
	
}
