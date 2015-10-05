package org.dvcama.lodview.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.ResultBean;
import org.dvcama.lodview.bean.TripleBean;
import org.dvcama.lodview.builder.ResourceBuilder;
import org.dvcama.lodview.conf.ConfigurationBean;
import org.dvcama.lodview.utils.Misc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UrlPathHelper;

import com.hp.hpl.jena.rdf.model.ModelFactory;

@Controller
@RequestMapping(value = "/")
public class ResourceController {
	@Autowired
	private MessageSource messageSource;

	@Autowired
	ConfigurationBean conf;

	@Autowired
	OntologyBean ontoBean;

	final AcceptList offeringRDF = new AcceptList("text/turtle, application/turtle, " //
			+ "application/x-turtle, application/rdf+xml, " //
			+ "application/rdf+json, application/ld+json, " //
			+ "text/plain, application/n-triples, text/trig, " //
			+ "application/n-quads, application/x-trig, application/trig, " //
			+ "text/n-quads, text/nquads, application/trix+xml, " //
			+ "text/rdf+n3, application/n3, " //
			+ "text/n3");

	final AcceptList offeringResources = new AcceptList("text/html, application/xhtml+xml");

	public ResourceController() {

	}

	public ResourceController(MessageSource messageSource, OntologyBean ontoBean) {
		this.messageSource = messageSource;
		this.ontoBean = ontoBean;
	}

	@RequestMapping(value = { "{path:(?!staticResources).*$}", "{path:(?!staticResources).*$}/**" })
	public Object resourceController(ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "output", defaultValue = "") String output, @CookieValue(value = "colorPair", defaultValue = "") String colorPair) throws UnsupportedEncodingException {
		if (colorPair.equals("")) {
			colorPair = conf.getRandomColorPair();
			Cookie c = new Cookie("colorPair", colorPair);
			c.setPath("/");
			res.addCookie(c);
		}
		return resource(conf, model, req, res, locale, output, "", colorPair);
	}

	public Object resource(ConfigurationBean conf, ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, String output, String forceIRI, String colorPair) throws UnsupportedEncodingException {

		model.addAttribute("conf", conf);

		String IRIsuffix = new UrlPathHelper().getLookupPathForRequest(req).replaceAll("/lodview/", "/");
		String requestUrl = req.getRequestURI();

		System.out.println("IRIsuffix " + IRIsuffix);
		System.out.println("requestUrl " + requestUrl);

		model.addAttribute("path", new UrlPathHelper().getContextPath(req).replaceAll("/lodview/", "/"));
		model.addAttribute("locale", locale.getLanguage());

		boolean redirect = false;
		boolean redirected = false;
		boolean avoidRedirection = false;

		// managing redirections
		if (!conf.getHttpRedirectExcludeList().equals("")) {
			String[] excList = conf.getHttpRedirectExcludeList().split(",");
			for (String exclude : excList) {
				if (requestUrl.matches(exclude)) {
					avoidRedirection = true;
					break;
				}
			}
		}
		if (!avoidRedirection && !conf.getHttpRedirectSuffix().equals("")) {
			// 303 dereferencing mode
			if (IRIsuffix.matches(".+" + conf.getHttpRedirectSuffix() + "$")) {
				// after redirect
				IRIsuffix = IRIsuffix.replaceAll(conf.getHttpRedirectSuffix() + "$", "");

				redirected = true;
			} else {
				// before redirect
				redirect = true;
			}
		} else if (!avoidRedirection && !conf.getHttpRedirectPrefix().equals("")) {
			// 303 dereferencing mode
			if (IRIsuffix.matches("^" + conf.getHttpRedirectPrefix() + ".+")) {
				// after redirect
				IRIsuffix = IRIsuffix.replaceAll("^" + conf.getHttpRedirectPrefix(), "");
				if (conf.getRedirectionStrategy().equals("pubby")) {
					IRIsuffix = "/resource/" + IRIsuffix;
					IRIsuffix = IRIsuffix.replaceAll("//", "/");
				}
				redirected = true;
			} else {
				// before redirect
				redirect = true;
			}
		}

		IRIsuffix = IRIsuffix.replaceAll("^/", "");

		String IRIprefix = conf.getIRInamespace().replaceAll("/$", "");
		String IRI = IRIprefix + "/" + IRIsuffix.replaceAll(" ", "%20");

		if (forceIRI != null && !forceIRI.equals("")) {
			IRI = forceIRI;
		}

		if (conf.getForceIriEncoding().equals("encode")) {
			String[] IRItokens = IRI.split("/");
			for (int i = 0; i < IRItokens.length; i++) {
				IRItokens[i] = java.net.URLEncoder.encode(IRItokens[i], "UTF-8");
			}
			IRI = StringUtils.join("/");
		} else if (conf.getForceIriEncoding().equals("decode")) {
			String[] IRItokens = IRI.split("/");
			for (int i = 0; i < IRItokens.length; i++) {
				IRItokens[i] = java.net.URLDecoder.decode(IRItokens[i], "UTF-8");
			}
			IRI = StringUtils.join("/");
		}

		if (conf.getRedirectionStrategy().equals("pubby")) {
			/*
			 * http://dbpedia.org/data/Barack_Obama.ntriples
			 * http://dbpedia.org/data/Barack_Obama.n3
			 * http://dbpedia.org/data/Barack_Obama.json
			 * http://dbpedia.org/data/Barack_Obama.rdf
			 */
			if (requestUrl.matches(".+\\.(ntriples|n3|json|rdf)")) {
				String outputType = "";
				String newUrl = requestUrl.replaceFirst("/data/", "/resource/").replaceAll("\\.(ntriples|n3|json|rdf)$", "");
				RedirectView r = new RedirectView();
				r.setExposeModelAttributes(false);
				if (requestUrl.endsWith(".ntriples")) {
					outputType = "text/plain";
				} else if (requestUrl.endsWith(".n3")) {
					outputType = "text/turtle";
				} else if (requestUrl.endsWith(".json")) {
					outputType = "application/rdf+json";
				} else if (requestUrl.endsWith(".rdf")) {
					outputType = "application/rdf+xml";
				}
				r.setUrl(newUrl + "?" + (req.getQueryString() != null ? req.getQueryString() + "&" : "") + "output=" + outputType);
				return r;
			}
		}

		System.out.println("####################################################################");
		System.out.println("#################  looking for " + IRI + "  ################# ");

		String[] acceptedContent = req.getHeader("Accept").split(",");
		if (redirected) {
			acceptedContent = "text/html".split(",");
		}
		// System.out.println("Accept " + req.getHeader("Accept"));

		AcceptList a = AcceptList.create(acceptedContent);
		// System.out.println("-- AcceptList: " + a);
		// System.out.println("-- OffertList: " + offeringRDF);

		MediaType matchItem = AcceptList.match(offeringRDF, a);
		Lang lang = matchItem != null ? RDFLanguages.contentTypeToLang(matchItem.getContentType()) : null;

		// override content negotiation
		if (!output.equals("")) {
			try {
				output = output.replaceAll("([a-zA-Z]) ([a-zA-Z])", "$1+$2");
				a = AcceptList.create(output.split(","));
				matchItem = AcceptList.match(offeringRDF, a);
				lang = RDFLanguages.contentTypeToLang(matchItem.getContentType());
			} catch (Exception e) {
				return new ErrorController(conf).error406(res, model, colorPair);
			}
			System.out.println("override content type " + matchItem.getContentType());
		}

		try {
			if (lang == null) {
				matchItem = AcceptList.match(offeringResources, a);
				// probably you are asking for an HTML page
				if (matchItem != null) {
					if (redirect && !redirected) {
						return redirect(req, IRIsuffix);
					} else {
						return htmlResource(model, IRI, colorPair, locale, req, res);
					}
				} else {
					return new ErrorController(conf).error406(res, model, colorPair);
				}
			} else {
				return resourceRaw(conf, model, IRI, conf.getEndPointUrl(), matchItem.getContentType());
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && e.getMessage().startsWith("404")) {
				return new ErrorController(conf).error404(res, model, e.getMessage(), colorPair, IRI, conf.getEndPointUrl());
			} else {
				return new ErrorController(conf).error500(res, model, e.getMessage(), colorPair, IRI, conf.getEndPointUrl());
			}
		}

	}

	private String htmlResource(ModelMap model, String IRI, String colorPair, Locale locale, HttpServletRequest req, HttpServletResponse res) throws Exception {
		model.addAttribute("contextPath", new UrlPathHelper().getContextPath(req));
		ResultBean r = new ResourceBuilder(messageSource).buildHtmlResource(IRI, locale, conf, ontoBean);
		model.addAttribute("colorPair", Misc.guessColor(colorPair, r, conf));
		model.addAttribute("results", Misc.guessClass(r, conf, ontoBean));
		model.addAttribute("ontoBean", ontoBean);

		addDataLinks(IRI, model, req, locale);
		addLodliveLink(locale, model, IRI);
		enrichResponse(model, r, req, res);
		return "resource";
	}

	private void addDataLinks(String IRI, ModelMap model, HttpServletRequest req, Locale locale) throws UnsupportedEncodingException {

		Map<String, Map<String, String>> rawdatalinks = new LinkedHashMap<String, Map<String, String>>();
		String queryString = (req.getQueryString() != null ? "&amp;" + req.getQueryString().replaceAll("&", "&amp;") : "");

		if (conf.getRedirectionStrategy().equals("pubby")) {

			Map<String, String> list = new LinkedHashMap<String, String>();
			list.put("xml", "?output=" + URLEncoder.encode("application/rdf+xml", "UTF-8") + queryString);
			list.put("ntriples", "?output=" + URLEncoder.encode("text/plain", "UTF-8") + queryString);
			list.put("turtle", "?output=" + URLEncoder.encode("text/turtle", "UTF-8") + queryString);
			list.put("json", "?output=" + URLEncoder.encode("application/rdf+json", "UTF-8") + queryString);
			list.put("ld+json", "?output=" + URLEncoder.encode("application/ld+json", "UTF-8") + queryString);
			rawdatalinks.put("rdf:", list);

		} else {
			Map<String, String> list = new LinkedHashMap<String, String>();
			list.put("xml", "?output=" + URLEncoder.encode("application/rdf+xml", "UTF-8") + queryString);
			list.put("ntriples", "?output=" + URLEncoder.encode("text/plain", "UTF-8") + queryString);
			list.put("turtle", "?output=" + URLEncoder.encode("text/turtle", "UTF-8") + queryString);
			list.put("ld+json", "?output=" + URLEncoder.encode("application/ld+json", "UTF-8") + queryString);
			rawdatalinks.put(messageSource.getMessage("footer.viewAs", null, "view as", locale), list);
		}

		if (conf.getEndPointType().equals("virtuoso")) {
			{
				Map<String, String> list = new LinkedHashMap<String, String>();
				list.put("atom", conf.getEndPointUrl() + "?output=application%2Fatom%2Bxml&amp;query=DESCRIBE+%3C" + IRI + "%3E");
				list.put("json", conf.getEndPointUrl() + "?output=application%2Fodata%2Bjson&amp;query=DESCRIBE+%3C" + IRI + "%3E");
				rawdatalinks.put("odata:", list);
			}
			{
				Map<String, String> list = new LinkedHashMap<String, String>();
				list.put("html", conf.getEndPointUrl() + "?output=text%2Fhtml&amp;query=DESCRIBE+%3C" + IRI + "%3E");
				list.put("json", conf.getEndPointUrl() + "?output=application%2Fmicrodata%2Bjson&amp;query=DESCRIBE+%3C" + IRI + "%3E");
				rawdatalinks.put("microdata:", list);
			}
			{
				Map<String, String> list = new LinkedHashMap<String, String>();
				list.put("csv", conf.getEndPointUrl() + "?output=text%2Fcsv&amp;query=DESCRIBE+%3C" + IRI + "%3E");
				list.put("cxml", conf.getEndPointUrl() + "?output=format=text%2Fcxml&amp;query=DESCRIBE+%3C" + IRI + "%3E");
				rawdatalinks.put("rawdata:", list);
			}
		}
		model.addAttribute("rawdatalinks", rawdatalinks);

	}

	private RedirectView redirect(HttpServletRequest req, String IRIsuffix) throws UnsupportedEncodingException {

		RedirectView r = new RedirectView();
		// preventing redirect of model attributes
		r.setExposeModelAttributes(false);
		r.setContentType("text/html");
		r.setHttp10Compatible(false);
		if (!conf.getHttpRedirectPrefix().equals("")) {
			// prefix mode
			String redirectUrl = conf.getHttpRedirectPrefix().replaceAll("^/", "");
			if (conf.getRedirectionStrategy().equals("pubby")) {
				r.setUrl(conf.getPublicUrlPrefix() + redirectUrl + IRIsuffix.replaceAll("^resource/", "") + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
			} else {
				r.setUrl(conf.getPublicUrlPrefix().replaceAll(IRIsuffix + "$", "") + redirectUrl + IRIsuffix + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
			}
		} else {
			// suffix mode
			String redirectUrl = conf.getHttpRedirectSuffix();
			// String[] redirectUrlArray = redirectUrl.split("/");
			// redirectUrl = "";
			// for (String string : redirectUrlArray) {
			// redirectUrl += URLEncoder.encode(string, "UTF-8") + "/";
			// }
			// redirectUrl = redirectUrl.replaceAll("/$", "");
			r.setUrl(req.getRequestURL() + redirectUrl + (req.getQueryString() != null ? "?" + req.getQueryString() : ""));
		}

		return r;

	}

	private void addLodliveLink(Locale locale, ModelMap model, String IRI) {
		if (locale.getLanguage().equals("it")) {
			model.addAttribute("lodliveUrl", "http://lodlive.it?" + IRI.replaceAll("#", "%23"));
		} else if (locale.getLanguage().equals("fr")) {
			model.addAttribute("lodliveUrl", "http://fr.lodlive.it?" + IRI.replaceAll("#", "%23"));
		} else if (locale.getLanguage().equals("gl")) {
			model.addAttribute("lodliveUrl", "http://gl.lodlive.it?" + IRI.replaceAll("#", "%23"));
		} else {
			model.addAttribute("lodliveUrl", "http://en.lodlive.it?" + IRI.replaceAll("#", "%23"));
		}
	}

	private void enrichResponse(ModelMap model, ResultBean r, HttpServletRequest req, HttpServletResponse res) {

		String publicUrl = r.getMainIRI();
		res.addHeader("Link", "<" + publicUrl + ">; rel=\"about\"");

		@SuppressWarnings("unchecked")
		Map<String, Map<String, String>> rawdatalinks = (LinkedHashMap<String, Map<String, String>>) model.get("rawdatalinks");
		for (String k : rawdatalinks.keySet()) {
			for (String k1 : rawdatalinks.get(k).keySet()) {
				res.addHeader("Link", "<" + rawdatalinks.get(k).get(k1) + ">; rel=\"alternate\"; type=\"application/rdf+xml\"; title=\"Structured Descriptor Document (" + k1 + ")\"");
			}
		}
		try {
			for (TripleBean t : r.getResources(r.getMainIRI()).get(r.getTypeProperty())) {
				res.addHeader("Link", "<" + t.getProperty().getProperty() + ">; rel=\"type\"");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@RequestMapping(value = "/rawdata")
	public ResponseEntity<String> resourceRawController(ModelMap model, @RequestParam(value = "IRI") String IRI, @RequestParam(value = "sparql") String sparql, @RequestParam(value = "contentType", defaultValue = "application/rdf+xml") String contentType) {
		return resourceRaw(conf, model, IRI, sparql, contentType);
	}

	public ResponseEntity<String> resourceRaw(ConfigurationBean conf, ModelMap model, @RequestParam(value = "IRI") String IRI, @RequestParam(value = "sparql") String sparql, @RequestParam(value = "contentType", defaultValue = "application/rdf+xml") String contentType) {
		// System.out.println("ResourceController.resourceRaw()");
		contentType = contentType.replaceAll("([a-zA-Z]) ([a-zA-Z])", "$1+$2");
		Lang lang = RDFLanguages.contentTypeToLang(contentType);
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Content-Type", contentType + "; charset=" + conf.getContentEncoding());

			if (sparql != null && sparql.equals("<>")) {
				com.hp.hpl.jena.rdf.model.Model m = ModelFactory.createDefaultModel();
				try {
					m.read(IRI);
				} catch (Exception e) {
					throw new Exception(messageSource.getMessage("error.noContentNegotiation", null, "sorry but content negotiation is not supported by the IRI", Locale.ENGLISH));
				}
				return new ResponseEntity<String>(new ResourceBuilder(messageSource).buildRDFResource(IRI, m, lang, conf), headers, HttpStatus.OK);
			} else {
				return new ResponseEntity<String>(new ResourceBuilder(messageSource).buildRDFResource(IRI, sparql, lang, conf), headers, HttpStatus.OK);
			}

		} catch (Exception e) {
			if (e.getMessage() != null && e.getMessage().startsWith("404")) {
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
			} else {
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}

	}
}
