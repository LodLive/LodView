package org.dvcama.lodview.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.PropertyBean;
import org.dvcama.lodview.bean.ResultBean;
import org.dvcama.lodview.bean.TripleBean;
import org.dvcama.lodview.conf.ConfigurationBean;
import org.dvcama.lodview.conf.ConfigurationBean.ColorStrategy;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Misc {

	public static String toNsResource(String iri, ConfigurationBean conf) {
		if (iri != null && !iri.equals("")) {
			// if (iri.startsWith(conf.getIRInamespace())) {
			// return conf.getNsURIPrefix(conf.getIRInamespace()) + ":" +
			// iri.replaceAll(conf.getIRInamespace() + "(.+)$", "$1");
			// } else {
			// }
			return conf.getNsURIPrefix(iri.replaceAll("[^/#]+$", "")) + ":" + iri.replaceAll(".+[/|#]([^/#]+)$", "$1");

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
		switch(conf.getColorStrategy())
		{
		case CLASS: {
			try {
				List<TripleBean> m = r.getResources(r.getMainIRI()).get(r.getTypeProperty());
				for (TripleBean tripleBean : m)
				{
					colorPair = conf.getColorPairMatcher().get(tripleBean.getValue());			
					if(colorPair!=null) return colorPair;
				}
			} catch (Exception e) {
			}
			return conf.getColorPairMatcher().get("http://lodview.it/conf#otherClasses");			
		}
		case PREFIX: {
			for (String prefix : conf.getColorPairMatcher().keySet()) {
				if(r.getMainIRI().startsWith(prefix))
				{
					return conf.getColorPairMatcher().get(prefix);
				}
			}
		}
		default: break;
		}
		return colorPair;
	}

	public static ResultBean guessClass(ResultBean r, ConfigurationBean conf, OntologyBean ontoBean) {
		try {
			if (conf.getMainOntologiesPrefixes().size() > 0) {
				String mainIri = r.getMainIRI();
				Map<PropertyBean, List<TripleBean>> mainResource = r.getResources(mainIri);
				TreeMap<Integer, List<TripleBean>> resultOrderedMap = new TreeMap<Integer, List<TripleBean>>();

				List<TripleBean> m = new ArrayList<TripleBean>(mainResource.get(r.getTypeProperty()));
				Model model = ontoBean.getModel();
				for (TripleBean tripleBean : m) {
					int dept = 0;
					if (startsWithAtLeastOne(tripleBean.getValue(), conf.getMainOntologiesPrefixes())) {
						dept = countFathers(tripleBean.getValue(), 0, model);
					}
					List<TripleBean> l = null;
					if (resultOrderedMap.get(dept) != null) {
						l = resultOrderedMap.get(dept);
					} else {
						l = new ArrayList<TripleBean>();
					}
					l.add(tripleBean);
					resultOrderedMap.put(dept, l);
					// removing types
					r.removeResource(tripleBean, mainIri);
				}
				if (resultOrderedMap.size() > 0) {
					for (Integer dept : resultOrderedMap.descendingKeySet()) {
						List<TripleBean> l = resultOrderedMap.get(dept);
						for (TripleBean tripleBean : l) {
							// adding ordered types
							r.addResource(tripleBean, mainIri);
						}
					}
				}
			}
			return r;

		} catch (Exception e) {
			// e.printStackTrace();
		}
		return r;
	}

	private static Integer countFathers(String value, int i, Model model) {
		NodeIterator iter = model.listObjectsOfProperty(model.createResource(value), model.createProperty("http://www.w3.org/2000/01/rdf-schema#subClassOf"));
		while (iter.hasNext()) {
			RDFNode node = iter.next();
			return countFathers(node.toString(), ++i, model);
		}
		return i;
	}

	private static boolean startsWithAtLeastOne(String value, List<String> startsWithList) {
		for (String string : startsWithList) {
			if (value.startsWith(string)) {
				return true;
			}
		}
		return false;
	}

	public static String stripHTML(String value) {

		value = value.replaceAll("</?\\w[^>]*>", "");

		return value;
	}

}