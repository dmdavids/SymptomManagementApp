package com.skywomantech.app.symptommanagement.data;

import java.math.BigInteger;


public class StatusLog {

	private BigInteger id;
	private long created;
	private String note;
	private String image_location;

	public StatusLog() {
		super();
	}
	
	public StatusLog(String note, String image_location) {
		this.note = note;
		this.image_location = image_location;
		this.created = 0L;
	}

	public StatusLog(long created, String note, String image_location) {
		super();
		this.created = created;
		this.note = note;
		this.image_location = image_location;
	}

    public StatusLog(String note, long created) {
        super();
        this.created = created;
        this.note = note;
    }


    public BigInteger getId() {
		return id;
	}

	public void setId(BigInteger id) {
		this.id = id;
	}

	public long getCreated() {
		return created;
	}

	public void setCreated(long created) {
		this.created = created;
	}

	public String getNote() {
		return note;
	}
	public void setNote(String note) {
		this.note = note;
	}
	public String getImage_location() {
		return image_location;
	}
	public void setImage_location(String image_location) {
		this.image_location = image_location;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (created ^ (created >>> 32));
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((image_location == null) ? 0 : image_location.hashCode());
		result = prime * result + ((note == null) ? 0 : note.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof StatusLog))
			return false;
		StatusLog other = (StatusLog) obj;
		if (created != other.created)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (image_location == null) {
			if (other.image_location != null)
				return false;
		} else if (!image_location.equals(other.image_location))
			return false;
		if (note == null) {
			if (other.note != null)
				return false;
		} else if (!note.equals(other.note))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "StatusLog [id=" + id + ", created=" + created + ", note="
				+ note + ", image_location=" + image_location + "]";
	}

}
