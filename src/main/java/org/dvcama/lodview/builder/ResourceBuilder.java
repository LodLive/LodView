package org.dvcama.lodview.builder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.jena.riot.Lang;
import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.ResultBean;
import org.dvcama.lodview.bean.TripleBean;
import org.dvcama.lodview.conf.ConfigurationBean;
import org.dvcama.lodview.endpoint.SPARQLEndPoint;
import org.dvcama.lodview.utils.Misc;
import org.springframework.context.MessageSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;

public class ResourceBuilder {

	private MessageSource messageSource;

	public ResourceBuilder() {
	}

	public ResourceBuilder(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public ResultBean buildHtmlResource(String IRI, Locale locale, ConfigurationBean conf, OntologyBean ontoBean) throws Exception {
		return buildHtmlResource(IRI, locale, conf, ontoBean, false);
	}

	public ResultBean buildHtmlResource(String IRI, Locale locale, ConfigurationBean conf, OntologyBean ontoBean, boolean localMode) throws Exception {
		ResultBean result = new ResultBean();
		List<String> videos = new ArrayList<String>();
		List<String> audios = new ArrayList<String>();
		List<String> images = new ArrayList<String>();
		List<String> linking = new ArrayList<String>();
		SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());
		result.setMainIRI(IRI);

		String preferredLanguage = conf.getPreferredLanguage();
		if (preferredLanguage.equals("auto")) {
			preferredLanguage = locale.getLanguage();
		}
		List<TripleBean> triples = new ArrayList<TripleBean>();
		if (conf.getEndPointUrl() != null && conf.getEndPointUrl().equals("<>")) {
			localMode = true;
		}
		if (localMode) {
			/* looking for data via content negotiation */
			Model m = ModelFactory.createDefaultModel();
			try {
				m.read(IRI);
			} catch (Exception e) {
				throw new Exception(messageSource.getMessage("error.noContentNegotiation", null, "sorry but content negotiation is not supported by the IRI", locale));
			}
			triples = se.doLocalQuery(m, IRI, conf.getDefaultQueries());
		} else {
			triples = se.doQuery(IRI, conf.getDefaultQueries(), null);
		}
		boolean betterTitleMatch = false, betterDescrMatch = false;
		for (TripleBean tripleBean : triples) {

			if (tripleBean.getIRI() == null) {
				tripleBean.setIRI(IRI);
				tripleBean.setNsIRI(Misc.toNsResource(tripleBean.getIRI(), conf));
			}

			if (conf.getTitleProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getTitleProperties().contains(tripleBean.getProperty().getProperty())) {
				if (tripleBean.getIRI().equals(IRI) && !betterTitleMatch && (result.getTitle() == null || result.getTitle().trim().equals("") || (tripleBean.getLang() != null && (preferredLanguage.equals(tripleBean.getLang()) || tripleBean.getLang().equals("en"))))) {
					result.setTitle(Misc.stripHTML(tripleBean.getValue()));
					if (preferredLanguage.equals(tripleBean.getLang())) {
						betterTitleMatch = true;
					}
				}
			} else if (conf.getDescriptionProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getDescriptionProperties().contains(tripleBean.getProperty().getProperty())) {
				if (tripleBean.getIRI().equals(IRI) && !betterDescrMatch && (result.getDescriptionProperty() == null || (tripleBean.getLang() != null && (preferredLanguage.equals(tripleBean.getLang()) || tripleBean.getLang().equals("en"))))) {
					result.setDescriptionProperty(tripleBean.getProperty());
					if (preferredLanguage.equals(tripleBean.getLang())) {
						betterDescrMatch = true;
					}
				}
			} else if (conf.getLatitudeProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getLatitudeProperties().contains(tripleBean.getProperty().getProperty())) {
				result.setLatitude(tripleBean.getValue());
			} else if (conf.getLongitudeProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getLongitudeProperties().contains(tripleBean.getProperty().getProperty())) {
				result.setLongitude(tripleBean.getValue());
			} else if (conf.getImageProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getImageProperties().contains(tripleBean.getProperty().getProperty())) {
				images.add(tripleBean.getValue());
			}  else if (conf.getAudioProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getAudioProperties().contains(tripleBean.getProperty().getProperty())) {
				audios.add(tripleBean.getValue());
			} else if (conf.getVideoProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getVideoProperties().contains(tripleBean.getProperty().getProperty())) {
				videos.add(tripleBean.getValue());
			}  else if (conf.getLinkingProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getLinkingProperties().contains(tripleBean.getProperty().getProperty())) {
				linking.add(tripleBean.getValue());
			} else if (conf.getTypeProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getTypeProperties().contains(tripleBean.getProperty().getProperty())) {
				result.setTypeProperty(tripleBean.getProperty());
			}

			if (tripleBean.getType().equals("iri")) {
				tripleBean.setUrl(Misc.toBrowsableUrl(tripleBean.getValue(), conf));
				tripleBean.setNsValue(Misc.toNsResource(tripleBean.getValue(), conf));
				if (!tripleBean.getUrl().equals(tripleBean.getValue()) || tripleBean.getValue().startsWith(conf.getPublicUrlPrefix())) {
					tripleBean.setLocal(true);
				}
				result.addResource(tripleBean, tripleBean.getIRI());
			} else if (tripleBean.getType().equals("literal")) {
				result.addLiteral(tripleBean, tripleBean.getIRI());
			} else if (tripleBean.getType().equals("bnode")) {
				result.addBnode(tripleBean, tripleBean.getIRI());
			}
		}

		result.setImages(images);
		result.setLinking(linking);
		result.setVideos(videos);
		result.setAudios(audios);

		return result;
	}

	public String buildRDFResource(String IRI, String sparql, Lang lang, ConfigurationBean conf) throws Exception {
		String result = "empty content";
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefixes(conf.getPrefixes());

		SPARQLEndPoint se = new SPARQLEndPoint(conf, null, null);
		model = se.extractData(model, IRI, sparql, conf.getDefaultRawDataQueries());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFWriter rdfWriter = model.getWriter(lang.getName());
		rdfWriter.setProperty("showXMLDeclaration","true");
		rdfWriter.setProperty("relativeURIs","");
 
		rdfWriter.write(model, baos, conf.getIRInamespace());

		byte[] resultByteArray = baos.toByteArray();
		result = new String(resultByteArray);

		return result;
	}

	public String buildRDFResource(String IRI, Model m, Lang lang, ConfigurationBean conf) throws Exception {
		String result = "empty content";
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefixes(conf.getPrefixes());

		SPARQLEndPoint se = new SPARQLEndPoint(conf, null, null);
		model = se.extractLocalData(model, IRI, m, conf.getDefaultRawDataQueries());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFWriter rdfWriter = model.getWriter(lang.getName());
		rdfWriter.setProperty("showXMLDeclaration","true");
		rdfWriter.setProperty("relativeURIs","");

		rdfWriter.write(model, baos, conf.getIRInamespace());
		rdfWriter.setProperty("showXMLDeclaration","true");
		rdfWriter.setProperty("relativeURIs","");

		byte[] resultByteArray = baos.toByteArray();
		result = new String(resultByteArray);

		return result;
	}

	public ResultBean buildPartialHtmlResource(String IRI, String[] abouts, Locale locale, ConfigurationBean conf, OntologyBean ontoBean, List<String> filterProperties) throws Exception {

		SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());
		ResultBean result = new ResultBean();
		List<TripleBean> literals = new ArrayList<TripleBean>();

		String preferredLanguage = conf.getPreferredLanguage();
		if (preferredLanguage.equals("auto")) {
			preferredLanguage = locale.getLanguage();
		}

		List<TripleBean> triples = new ArrayList<TripleBean>();

		/*
		 * FIXME: make more distinct queries to avoid length limits, eg
		 * http://dati.camera.it/ocd/assemblea.rdf/a16
		 */

		StringBuilder filter = new StringBuilder();
		for (String titleProperty : filterProperties) {
			if (titleProperty.toLowerCase().startsWith("http:")) {
				filter.append("(?filterProperty = <" + titleProperty + ">)");
			} else {
				filter.append("(?filterProperty = " + titleProperty + ")");
			}
			filter.append(" || ");
		}

		for (String about : abouts) {
			StringBuilder sparqlQuery = new StringBuilder("select distinct ?o  ");
			sparqlQuery.append("{ <" + about + "> ?filterProperty ?o. FILTER (" + filter + "))}  ");
			List<String> sparqlQueries = new ArrayList<String>();
			sparqlQueries.add(sparqlQuery.toString().replaceAll("\\|\\| \\)", ""));
			try {

				if (conf.getEndPointUrl().equals("<>")) {
					/* looking for data via content negotiation */
					Model m = ModelFactory.createDefaultModel();
					try {
						m.read(about);
					} catch (Exception e) {
						throw new Exception(messageSource.getMessage("error.noContentNegotiation", null, "sorry but content negotiation is not supported by the IRI", locale));
					}
					triples.addAll(se.doLocalQuery(m, about, sparqlQueries, about));
				} else {
					triples.addAll(se.doQuery(null, sparqlQueries, about));
				}

			} catch (Exception e) {
			}
		}

		Map<String, List<TripleBean>> l = new HashMap<String, List<TripleBean>>();

		for (TripleBean tripleBean : triples) {
			if (tripleBean.getType().equals("literal")) {
				List<TripleBean> al = l.get(tripleBean.getProperty().getProperty());
				if (al == null) {
					al = new ArrayList<TripleBean>();
				}
				al.add(tripleBean);
				l.put(tripleBean.getProperty().getProperty(), al);
			}
		}
		for (String about : l.keySet()) {
			List<TripleBean> al = l.get(about);
			boolean betterTitleMatch = false;
			TripleBean title = null;
			for (TripleBean tripleBean : al) {
				if (!betterTitleMatch && (title == null || title.getValue() == null || title.getValue().trim().equals("") || preferredLanguage.equals(tripleBean.getLang()) || tripleBean.getLang().equals("en"))) {
					title = tripleBean;
					if (preferredLanguage.equals(tripleBean.getLang())) {
						betterTitleMatch = true;
					}
				}
			}
			if (title != null) {
				literals.add(title);
			}
		}
		result.setLiterals(IRI, literals);
		return result;
	}

	public ResultBean buildHtmlInverseResource(String IRI, String property, int start, Locale locale, ConfigurationBean conf, OntologyBean ontoBean) throws Exception {
		ResultBean result = new ResultBean();

		SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());
		String preferredLanguage = conf.getPreferredLanguage();
		if (preferredLanguage.equals("auto")) {
			preferredLanguage = locale.getLanguage();
		}
		if (property == null) {
			/* counting */
			List<TripleBean> resources = new ArrayList<TripleBean>();
			List<TripleBean> triples = new ArrayList<TripleBean>();

			if (conf.getEndPointUrl().equals("<>")) {
				/* looking for data via content negotiation */
				Model m = ModelFactory.createDefaultModel();
				try {
					m.read(IRI);
				} catch (Exception e) {
					throw new Exception(messageSource.getMessage("error.noContentNegotiation", null, "sorry but content negotiation is not supported by the IRI", locale));
				}
				triples = se.doLocalQuery(m, IRI, conf.getDefaultInversesCountQueries());
			} else {
				triples = se.doQuery(IRI, conf.getDefaultInversesCountQueries(), null);
			}

			for (TripleBean tripleBean : triples) {
				if (tripleBean.getType().equals("literal")) {
					resources.add(tripleBean);
				}
			}

			result.setResources(IRI, resources);

		} else {
			/* listing */
			List<TripleBean> resources = new ArrayList<TripleBean>();
			List<TripleBean> triples = new ArrayList<TripleBean>();

			if (conf.getEndPointUrl().equals("<>")) {
				/* looking for data via content negotiation */
				Model m = ModelFactory.createDefaultModel();
				try {
					m.read(IRI);
				} catch (Exception e) {
					throw new Exception(messageSource.getMessage("error.noContentNegotiation", null, "sorry but content negotiation is not supported by the IRI", locale));
				}
				triples = se.doLocalQuery(m, IRI, property, start, conf.getDefaultInversesQueries(), null);
			} else {
				triples = se.doQuery(IRI, property, start, conf.getDefaultInversesQueries(), null, null);
			}

			Map<String, TripleBean> controlList = new HashMap<String, TripleBean>();
			for (TripleBean tripleBean : triples) {
				if (tripleBean.getType().equals("literal")) {
					if (controlList.get(tripleBean.getProperty().getProperty()) == null || preferredLanguage.equals(tripleBean.getLang())) {
						controlList.put(tripleBean.getProperty().getProperty(), tripleBean);
					}
				}
			}

			for (String at : controlList.keySet()) {
				resources.add(controlList.get(at));
			}

			result.setResources(IRI, resources);

		}

		return result;
	}

	public ResultBean buildHtmlInverseResource(String IRI, Locale locale, ConfigurationBean conf, OntologyBean ontoBean) throws Exception {
		return buildHtmlInverseResource(IRI, null, -1, locale, conf, ontoBean);
	}
}
