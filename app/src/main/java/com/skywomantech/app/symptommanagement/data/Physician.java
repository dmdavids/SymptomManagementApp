package com.skywomantech.app.symptommanagement.data;


import java.util.Collection;



public class Physician {

	private String id;
	private String name;
	private Collection<Patient> patients;
	
	
	public Physician() {
		super();
	}

	public Physician(String name) {
		super();
		this.name = name;
		this.patients = null;
	}

	public Physician(String name, Collection<Patient> patients) {
		super();
		this.name = name;
		this.patients = patients;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public Collection<Patient> getPatients() {
		return patients;
	}


	public void setPatients(Collection<Patient> patients) {
		this.patients = patients;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Physician))
			return false;
		Physician other = (Physician) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
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
		return name;
	}

    public String toDebugString() {
        return "Physician [id=" + id + ", name=" + name + ", patients="
                + patients + "]";
    }
}
