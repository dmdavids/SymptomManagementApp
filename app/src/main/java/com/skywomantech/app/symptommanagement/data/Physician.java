package com.skywomantech.app.symptommanagement.data;


import java.util.Collection;


public class Physician {

    private String id;
    private String firstName;
    private String lastName;
    private Collection<Patient> patients;

    public Physician() {
        super();
    }

    public Physician(String firstName, String lastName) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.patients = null;
    }

    public Physician(String firstName, String lastName, Collection<Patient> patients) {
        super();
        this.firstName = firstName;
        this.lastName = lastName;
        this.patients = patients;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
        result = prime * result
                + ((firstName == null) ? 0 : firstName.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result
                + ((lastName == null) ? 0 : lastName.hashCode());
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
        if (firstName == null) {
            if (other.firstName != null)
                return false;
        } else if (!firstName.equals(other.firstName))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (lastName == null) {
            if (other.lastName != null)
                return false;
        } else if (!lastName.equals(other.lastName))
            return false;
        return true;
    }

    public String getName() {
        String name = "";
        if (this.firstName != null || !this.firstName.isEmpty()) name += this.firstName;
        if (!name.isEmpty()) name += " ";
        if (this.lastName != null  || !this.lastName.isEmpty()) name+= this.lastName;
        return name;
    }

    public String toDebugString() {
        return "Physician [id=" + id + ", firstName=" + firstName
                + ", lastName=" + lastName + ", patients=" + patients + "]";
    }

	@Override
	public String toString() {
		return getName();
	}

}
