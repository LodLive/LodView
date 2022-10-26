package it.gov.innovazione.lodviewng.controllers;

import it.gov.innovazione.lodviewng.bean.OntologyBean;
import it.gov.innovazione.lodviewng.bean.ResultBean;
import it.gov.innovazione.lodviewng.bean.TripleBean;
import it.gov.innovazione.lodviewng.builder.ResourceBuilder;
import it.gov.innovazione.lodviewng.conf.ConfigurationBean;
import it.gov.innovazione.lodviewng.utils.Misc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.web.AcceptList;
import org.apache.jena.atlas.web.MediaType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping(value = "/")
@RequiredArgsConstructor
@Slf4j
public class ResourceController {

    final AcceptList offeringRDF = new AcceptList("text/turtle, application/turtle, " //
            + "application/x-turtle, application/rdf+xml, " //
            + "application/rdf+json, application/ld+json, " //
            + "text/plain, application/n-triples, text/trig, " //
            + "application/n-quads, application/x-trig, application/trig, " //
            + "text/n-quads, text/nquads, application/trix+xml, " //
            + "text/rdf+n3, application/n3, " //
            + "text/n3");
    final AcceptList offeringResources = new AcceptList("text/html, application/xhtml+xml");
    private final ConfigurationBean conf;
    private final OntologyBean ontoBean;
    private final MessageSource messageSource;

    @RequestMapping(value = {"{path:(?!staticResources).*$}", "{path:(?!staticResources).*$}/**"})
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

        log.info("IRIsuffix " + IRIsuffix);
        log.info("requestUrl " + requestUrl);

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
                IRItokens[i] = URLEncoder.encode(IRItokens[i], StandardCharsets.UTF_8);
            }
            IRI = StringUtils.join("/");
        } else if (conf.getForceIriEncoding().equals("decode")) {
            String[] IRItokens = IRI.split("/");
            for (int i = 0; i < IRItokens.length; i++) {
                IRItokens[i] = java.net.URLDecoder.decode(IRItokens[i], StandardCharsets.UTF_8);
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

        log.info("####################################################################");
        log.info("#################  looking for " + IRI + "  ################# ");

        String[] acceptedContent = req.getHeader("Accept").split(",");
        if (redirected) {
            acceptedContent = "text/html".split(",");
        }
        // log.trace("Accept " + req.getHeader("Accept"));

        AcceptList a = AcceptList.create(acceptedContent);
        // log.trace("-- AcceptList: " + a);
        // log.trace("-- OffertList: " + offeringRDF);

        MediaType matchItem = AcceptList.match(offeringRDF, a);
        Lang lang = matchItem != null ? RDFLanguages.contentTypeToLang(matchItem.getContentTypeStr()) : null;

        // override content negotiation
        if (!output.equals("")) {
            try {
                output = output.replaceAll("([a-zA-Z]) ([a-zA-Z])", "$1+$2");
                a = AcceptList.create(output.split(","));
                matchItem = AcceptList.match(offeringRDF, a);
                lang = RDFLanguages.contentTypeToLang(matchItem.getContentTypeStr());
            } catch (Exception e) {
                return new ErrorController(conf).error406(res, model, colorPair);
            }
            log.debug("override content type " + matchItem.getContentTypeStr());
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
                return resourceRaw(conf, model, IRI, conf.getEndPointUrl(), matchItem.getContentTypeStr());
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
            list.put("xml", "?output=" + URLEncoder.encode("application/rdf+xml", StandardCharsets.UTF_8) + queryString);
            list.put("ntriples", "?output=" + URLEncoder.encode("text/plain", StandardCharsets.UTF_8) + queryString);
            list.put("turtle", "?output=" + URLEncoder.encode("text/turtle", StandardCharsets.UTF_8) + queryString);
            list.put("json", "?output=" + URLEncoder.encode("application/rdf+json", StandardCharsets.UTF_8) + queryString);
            list.put("ld+json", "?output=" + URLEncoder.encode("application/ld+json", StandardCharsets.UTF_8) + queryString);
            rawdatalinks.put("rdf:", list);

        } else {
            Map<String, String> list = new LinkedHashMap<String, String>();
            list.put("xml", "?output=" + URLEncoder.encode("application/rdf+xml", StandardCharsets.UTF_8) + queryString);
            list.put("ntriples", "?output=" + URLEncoder.encode("text/plain", StandardCharsets.UTF_8) + queryString);
            list.put("turtle", "?output=" + URLEncoder.encode("text/turtle", StandardCharsets.UTF_8) + queryString);
            list.put("ld+json", "?output=" + URLEncoder.encode("application/ld+json", StandardCharsets.UTF_8) + queryString);
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

        rawdatalinks.values().stream()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .forEach(e ->
                        res.addHeader(
                                "Link", "<" + e.getValue() + ">;" +
                                        " rel=\"alternate\"; type=\"application/rdf+xml\";" +
                                        " title=\"Structured Descriptor Document (" + e.getKey() + ")\""));

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
        // logger.trace("ResourceController.resourceRaw()");
        contentType = contentType.replaceAll("([a-zA-Z]) ([a-zA-Z])", "$1+$2");
        Lang lang = RDFLanguages.contentTypeToLang(contentType);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", contentType + "; charset=" + conf.getContentEncoding());

            if (sparql != null && sparql.equals("<>")) {
                Model m = ModelFactory.createDefaultModel();
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
