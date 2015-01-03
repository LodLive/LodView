package org.dvcama.lodview.bean;

public class PropertyBean {

	private String nsProperty = null, property = null, propertyUrl = null;
	private String label = "";
	private String comment = "";

	public String getPropertyUrl() {
		return propertyUrl;
	}

	public void setPropertyUrl(String propertyUrl) {
		this.propertyUrl = propertyUrl;
	}

	public String getNsProperty() {
		return nsProperty;
	}

	public void setNsProperty(String nsProperty) {
		this.nsProperty = nsProperty;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nsProperty == null) ? 0 : nsProperty.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		result = prime * result + ((propertyUrl == null) ? 0 : propertyUrl.hashCode());
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
		PropertyBean other = (PropertyBean) obj;
		if (nsProperty == null) {
			if (other.nsProperty != null)
				return false;
		} else if (!nsProperty.equals(other.nsProperty))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		if (propertyUrl == null) {
			if (other.propertyUrl != null)
				return false;
		} else if (!propertyUrl.equals(other.propertyUrl))
			return false;
		return true;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
