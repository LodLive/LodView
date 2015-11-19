package org.dvcama.lodview.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResultBean {

	private String title, latitude = null, longitude = null, mainIRI = null;
	private PropertyBean descriptionProperty = null, typeProperty = null;
	private List<String> images = null, linking = null, videos = null, audios = null;
	private Map<String, LinkedHashMap<PropertyBean, List<TripleBean>>> literals = new HashMap<String, LinkedHashMap<PropertyBean, List<TripleBean>>>(), resources = new HashMap<String, LinkedHashMap<PropertyBean, List<TripleBean>>>(), bnodes = new HashMap<String, LinkedHashMap<PropertyBean, List<TripleBean>>>();

	private Map<String, LinkedHashMap<PropertyBean, List<TripleBean>>> addEle(String IRI, TripleBean tripleBean, Map<String, LinkedHashMap<PropertyBean, List<TripleBean>>> ele) {
		if (ele.get(IRI) == null || ele.get(IRI).get(tripleBean.getProperty()) == null) {
			LinkedHashMap<PropertyBean, List<TripleBean>> a = ele.get(IRI);
			if (a == null) {
				a = new LinkedHashMap<PropertyBean, List<TripleBean>>();
			}
			List<TripleBean> b = new ArrayList<TripleBean>();
			b.add(tripleBean);
			a.put(tripleBean.getProperty(), b);
			ele.put(IRI, a);
		} else {
			LinkedHashMap<PropertyBean, List<TripleBean>> a = ele.get(IRI);
			List<TripleBean> b = a.get(tripleBean.getProperty());
			b.add(tripleBean);
			a.put(tripleBean.getProperty(), b);
			ele.put(IRI, a);
		}
		return ele;
	}

	private Map<String, LinkedHashMap<PropertyBean, List<TripleBean>>> removeEle(String IRI, TripleBean tripleBean, Map<String, LinkedHashMap<PropertyBean, List<TripleBean>>> ele) {
		if (ele.get(IRI) == null || ele.get(IRI).get(tripleBean.getProperty()) == null) {

		} else {
			LinkedHashMap<PropertyBean, List<TripleBean>> a = ele.get(IRI);
			List<TripleBean> b = a.get(tripleBean.getProperty());
			b.remove(tripleBean);
			a.put(tripleBean.getProperty(), b);
			ele.put(IRI, a);
		}
		return ele;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public void setLiterals(String IRI, List<TripleBean> localLiterals) {
		for (TripleBean tripleBean : localLiterals) {
			literals = addEle(IRI, tripleBean, literals);
		}

	}

	public void setBnodes(String IRI, List<TripleBean> localBnodes) {
		for (TripleBean tripleBean : localBnodes) {
			bnodes = addEle(IRI, tripleBean, bnodes);
		}

	}

	public void setResources(String IRI, List<TripleBean> Localresources) {
		for (TripleBean tripleBean : Localresources) {
			resources = addEle(IRI, tripleBean, resources);
		}

	}

	public LinkedHashMap<PropertyBean, List<TripleBean>> getResources(String IRI) {
		return resources.get(IRI);
	}

	public LinkedHashMap<PropertyBean, List<TripleBean>> getLiterals(String IRI) {
		return literals.get(IRI);
	}

	public  Map<String, LinkedHashMap<PropertyBean, List<TripleBean>>> getLiterals() {
		return literals;
	}
	public LinkedHashMap<PropertyBean, List<TripleBean>> getBnodes(String IRI) {
		return bnodes.get(IRI);
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public List<String> getAudios() {
		return audios;
	}

	public List<String> getVideos() {
		return videos;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public List<String> getLinking() {
		return linking;
	}

	public void setLinking(List<String> linking) {
		this.linking = linking;
	}

	@Override
	public String toString() {
		return "ResultBean [title=" + title + ", \ndescriptionProperty=" + descriptionProperty + ", \nlatitude=" + latitude + ", \nlongitude=" + longitude + ", \nimages=" + images + ", \nlinking=" + linking + ", \nliterals=" + literals + ", \nresources=" + resources + ", \nbnodes=" + bnodes + "]";
	}

	public String getMainIRI() {
		return mainIRI;
	}

	public void setMainIRI(String mainIRI) {
		this.mainIRI = mainIRI;
	}

	public void addBnode(TripleBean tripleBean, String IRI) {

		bnodes = addEle(IRI, tripleBean, bnodes);
	}

	public void addLiteral(TripleBean tripleBean, String IRI) {
		literals = addEle(IRI, tripleBean, literals);

	}

	public void addResource(TripleBean tripleBean, String IRI) {
		resources = addEle(IRI, tripleBean, resources);
	}

	public void removeBnode(TripleBean tripleBean, String IRI) {

		bnodes = removeEle(IRI, tripleBean, bnodes);
	}

	public void removeLiteral(TripleBean tripleBean, String IRI) {
		literals = removeEle(IRI, tripleBean, literals);

	}

	public void removeResource(TripleBean tripleBean, String IRI) {
		resources = removeEle(IRI, tripleBean, resources);
	}

	public PropertyBean getDescriptionProperty() {
		return descriptionProperty;
	}

	public void setDescriptionProperty(PropertyBean descriptionProperty) {
		this.descriptionProperty = descriptionProperty;
	}

	public PropertyBean getTypeProperty() {
		return typeProperty;
	}

	public void setTypeProperty(PropertyBean typeProperty) {
		this.typeProperty = typeProperty;
	}

	public void setVideos(List<String> videos) {
		this.videos = videos;
	}

	public void setAudios(List<String> audios) {
		this.audios = audios;
	}

}
