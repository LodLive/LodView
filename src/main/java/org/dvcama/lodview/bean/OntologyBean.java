package org.dvcama.lodview.bean;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.web.context.ServletContextAware;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

public class OntologyBean implements ServletContextAware {

	private String ontoDir;
	private ServletContext context;
	private Model model;

	public void init() { 
		File ontoDirFile = new File(ontoDir);
		if (!ontoDirFile.isAbsolute()) {
			ontoDirFile = new File(context.getRealPath("/") + "/WEB-INF/" + ontoDir);
		}
		model = ModelFactory.createDefaultModel();
		if (ontoDirFile.exists()) {
			System.out.println("ontologies dir founded!");
			File[] list = ontoDirFile.listFiles();
			for (File file : list) {
				if (!file.isDirectory()) {
					try {
						System.out.println("loading " + file.getCanonicalPath());
						FileManager.get().readModel(model, file.getAbsolutePath());
						System.out.println("read successfully!");
					} catch (Exception e) {
						System.err.println("error loading " + e.getMessage());
						// e.printStackTrace();
					}
				}
			}
		} else {
			System.out.println("no ontologies founded " + ontoDirFile.getAbsolutePath());
		}

		// System.out.println("------------------- " + getHashResult("en",
		// "http://dati.camera.it/ocd/parentCountry"));
		// System.out.println("------------------- " + getHashResult("it",
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
			//System.out.println(IRI + " " + preferredLanguage + " --> " + l.getLanguage() + " --> " + l.getLexicalForm());
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
