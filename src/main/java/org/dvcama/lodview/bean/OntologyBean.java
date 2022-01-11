package org.dvcama.lodview.bean;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.web.context.ServletContextAware;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntologyBean implements ServletContextAware {

	private String ontoDir;
	private ServletContext context;
	private Model model;
 	final Logger logger = LoggerFactory.getLogger(OntologyBean.class);

	public void init() { 
		File ontoDirFile = new File(ontoDir);
		if (!ontoDirFile.isAbsolute()) {
			ontoDirFile = new File(context.getRealPath("/") + "/WEB-INF/" + ontoDir);
		}
		model = ModelFactory.createDefaultModel();
		if (ontoDirFile.exists()) {
			logger.debug("ontologies dir founded!");
			File[] list = ontoDirFile.listFiles();
			for (File file : list) {
				if (!file.isDirectory()) {
					try {
						logger.debug("loading " + file.getCanonicalPath());
						FileManager.get().readModel(model, file.getAbsolutePath());
						logger.debug("read successfully!");
					} catch (Exception e) {
						logger.error("error loading " + e.getMessage());
						// e.printStackTrace();
					}
				}
			}
		} else {
			logger.debug("no ontologies founded " + ontoDirFile.getAbsolutePath());
		}

		// logger.debug("------------------- " + getHashResult("en",
		// "http://dati.camera.it/ocd/parentCountry"));
		// logger.debug("------------------- " + getHashResult("it",
		// "http://dati.camera.it/ocd/parentCountry"));

	}

	@Override
	public void setServletContext(ServletContext arg0) {
		this.context = arg0;
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getOntoDir() {
		return ontoDir;
	}

	public void setOntoDir(String ontoDir) {
		this.ontoDir = ontoDir;
	}

	public String getValue(String what, String preferredLanguage, String IRI) {
		Resource IRIresource = model.createResource(IRI);
		return getSingleValue(preferredLanguage, IRIresource, "http://www.w3.org/2000/01/rdf-schema#" + what, "");
	}

	public String getEscapedValue(String what, String preferredLanguage, String IRI) {
		return StringEscapeUtils.escapeHtml4(getValue(what, preferredLanguage, IRI));
	}

	public Map<String, String> getHashResult(String preferredLanguage, String IRI) {
		Map<String, String> result = new HashMap<String, String>();
		Resource IRIresource = model.createResource(IRI);
		result.put("label", getSingleValue(preferredLanguage, IRIresource, "http://www.w3.org/2000/01/rdf-schema#label", ""));
		result.put("comment", getSingleValue(preferredLanguage, IRIresource, "http://www.w3.org/2000/01/rdf-schema#comment", ""));
		return result;
	}

	private String getSingleValue(String preferredLanguage, Resource IRI, String prop, String defaultValue) {
		NodeIterator iter = model.listObjectsOfProperty(IRI, model.createProperty(prop));
		String result = defaultValue;
		boolean betterTitleMatch = false;
		while (iter.hasNext()) {
			RDFNode node = iter.nextNode();
			Literal l = node.asLiteral();
			//logger.debug(IRI + " " + preferredLanguage + " --> " + l.getLanguage() + " --> " + l.getLexicalForm());
			if (!betterTitleMatch && (result.equals(defaultValue) || l.getLanguage().equals("en") || l.getLanguage().equals(preferredLanguage))) {
				if (preferredLanguage.equals(l.getLanguage())) {
					betterTitleMatch = true;
				}
				result = l.getLexicalForm();
			}

		}
		return result;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

}
