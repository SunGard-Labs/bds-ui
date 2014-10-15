package com.sungard.scs.bds.contact;

public class ContactImpl implements Contact {
	private static final long serialVersionUID = 1824474473284679370L;
	private final String address;
	private final String name;
	
	public ContactImpl(String address) {
		this(address, address);
	}
		
	public ContactImpl(String address, String name) {
		this.address = address;
		this.name = name;
	}
	
	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ContactImpl other = (ContactImpl) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ContactImpl [address=" + address + ", name=" + name + "]";
	}

}
