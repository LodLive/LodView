package org.dvcama.lodview.utils;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;

import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.PropertyBean;
import org.dvcama.lodview.bean.ResultBean;
import org.dvcama.lodview.bean.TripleBean;
import org.dvcama.lodview.conf.ConfigurationBean;

import com.hp.hpl.jena.rdf.model.Model;

public class Misc {

	public static String toNsResource(String iri, ConfigurationBean conf) {
		if (iri != null && !iri.equals("")) {
			if (iri.startsWith(conf.getIRInamespace())) {
				return conf.getNsURIPrefix(conf.getIRInamespace()) + ":" + iri.replaceAll(conf.getIRInamespace() + "(.+)$", "$1");
			} else {
				return conf.getNsURIPrefix(iri.replaceAll("[^/#]+$", "")) + ":" + iri.replaceAll(".+[/|#]([^/#]+)$", "$1");
			}
		} else {
			return null;
		}
	}

	public static String toBrowsableUrl(String value, ConfigurationBean conf) {

		if (!conf.getPublicUrlSuffix().equals("") && value.startsWith(conf.getIRInamespace())) {
			try {
				return conf.getPublicUrlPrefix() + "?" + conf.getPublicUrlSuffix() + "IRI=" + java.net.URLEncoder.encode(value, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return value;
			}
		} else {
			return value.replaceAll("^" + conf.getIRInamespace() + "(.+)$", conf.getPublicUrlPrefix() + "$1").replaceAll("([^:])//", "$1/");
		}

	}

	public static String guessColor(String colorPair, ResultBean r, ConfigurationBean conf) {
		if (conf.getColorPairMatcher() != null && conf.getColorPairMatcher().size() > 0) {
			List<TripleBean> m = r.getResources(r.getMainIRI()).get(r.getTypeProperty());
			for (String key : conf.getColorPairMatcher().keySet()) {
				for (TripleBean tripleBean : m) {
					if (tripleBean.getValue().equals(key)) {
						colorPair = conf.getColorPairMatcher().get(key);
						return colorPair;
					}
				}
			}
			return conf.getColorPairMatcher().get("http://lodview.it/conf#otherClasses");
		}
		return colorPair;
	}

	public static ResultBean guessClass(ResultBean r, ConfigurationBean conf, OntologyBean ontoBean) {
		if (conf.getMainOntologiesPrefixes().size() > 0) {

			LinkedHashMap<PropertyBean, List<TripleBean>> mainResource = r.getResources(r.getMainIRI());
			List<TripleBean> m = r.getResources(r.getMainIRI()).get(r.getTypeProperty());
			Model model = ontoBean.getModel();

			for (TripleBean tripleBean : m) {
				// TODO: find the lowest class
			}

		}
		return r;
	}

}
