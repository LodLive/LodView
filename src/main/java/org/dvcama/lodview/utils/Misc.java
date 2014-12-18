package org.dvcama.lodview.utils;

import java.io.UnsupportedEncodingException;

import org.dvcama.lodview.conf.ConfigurationBean;

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
}
