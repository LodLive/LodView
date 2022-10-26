package it.gov.innovazione.lodviewng.builder;


import it.gov.innovazione.lodviewng.endpoint.SPARQLEndPoint;
import it.gov.innovazione.lodviewng.bean.OntologyBean;
import it.gov.innovazione.lodviewng.bean.ResultBean;
import it.gov.innovazione.lodviewng.bean.TripleBean;
import it.gov.innovazione.lodviewng.conf.ConfigurationBean;
import it.gov.innovazione.lodviewng.utils.Misc;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFWriterI;
import org.apache.jena.riot.Lang;
import org.springframework.context.MessageSource;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
public class ResourceBuilder {

    public static final String ERROR_NO_CONTENT_NEGOTIATION = "error.noContentNegotiation";
    public static final String SORRY_BUT_CONTENT_NEGOTIATION_IS_NOT_SUPPORTED_BY_THE_IRI = "sorry but content negotiation is not supported by the IRI";
    public static final String LITERAL = "literal";
    public static final String IRI = "iri";
    public static final String BNODE = "bnode";
    public static final String SHOW_XML_DECLARATION = "showXMLDeclaration";
    public static final String RELATIVE_URIS = "relativeURIs";
    private MessageSource messageSource;

    public ResultBean buildHtmlResource(String iri, Locale locale, ConfigurationBean conf, OntologyBean ontoBean) throws Exception {
        return buildHtmlResource(iri, locale, conf, ontoBean, false);
    }

    public ResultBean buildHtmlResource(String iri, Locale locale, ConfigurationBean conf, OntologyBean ontoBean, boolean localMode) throws Exception {
        ResultBean result = new ResultBean();
        List<String> videos = new ArrayList<>();
        List<String> audios = new ArrayList<>();
        List<String> images = new ArrayList<>();
        List<String> linking = new ArrayList<>();
        SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());
        result.setMainIRI(iri);

        String preferredLanguage = conf.getPreferredLanguage();
        if (preferredLanguage.equals("auto")) {
            preferredLanguage = locale.getLanguage();
        }
        List<TripleBean> triples;
        if (conf.getEndPointUrl() != null && conf.getEndPointUrl().equals("<>")) {
            localMode = true;
        }
        if (localMode) {
            /* looking for data via content negotiation */
            Model m = ModelFactory.createDefaultModel();
            try {
                m.read(iri);
            } catch (Exception e) {
                throw new Exception(messageSource.getMessage(ERROR_NO_CONTENT_NEGOTIATION, null, SORRY_BUT_CONTENT_NEGOTIATION_IS_NOT_SUPPORTED_BY_THE_IRI, locale));
            }
            triples = se.doLocalQuery(m, iri, conf.getDefaultQueries());
        } else {
            triples = se.doQuery(iri, conf.getDefaultQueries(), null);
        }
        boolean betterTitleMatch = false;
        boolean betterDescrMatch = false;
        for (TripleBean tripleBean : triples) {

            if (tripleBean.getIri() == null) {
                tripleBean.setIri(iri);
                tripleBean.setNsIri(Misc.toNsResource(tripleBean.getIri(), conf));
            }

            if (conf.getTitleProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getTitleProperties().contains(tripleBean.getProperty().getProperty())) {
                if (tripleBean.getIri().equals(iri) && !betterTitleMatch && (result.getTitle() == null || result.getTitle().trim().equals("") || (tripleBean.getLang() != null && (preferredLanguage.equals(tripleBean.getLang()) || tripleBean.getLang().equals("en"))))) {
                    result.setTitle(Misc.stripHTML(tripleBean.getValue()));
                    if (preferredLanguage.equals(tripleBean.getLang())) {
                        betterTitleMatch = true;
                    }
                }
            } else if (conf.getDescriptionProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getDescriptionProperties().contains(tripleBean.getProperty().getProperty())) {
                if (tripleBean.getIri().equals(iri) && !betterDescrMatch && (result.getDescriptionProperty() == null || (tripleBean.getLang() != null && (preferredLanguage.equals(tripleBean.getLang()) || tripleBean.getLang().equals("en"))))) {
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
            } else if (conf.getAudioProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getAudioProperties().contains(tripleBean.getProperty().getProperty())) {
                audios.add(tripleBean.getValue());
            } else if (conf.getVideoProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getVideoProperties().contains(tripleBean.getProperty().getProperty())) {
                videos.add(tripleBean.getValue());
            } else if (conf.getLinkingProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getLinkingProperties().contains(tripleBean.getProperty().getProperty())) {
                linking.add(tripleBean.getValue());
            } else if (conf.getTypeProperties().contains(tripleBean.getProperty().getNsProperty()) || conf.getTypeProperties().contains(tripleBean.getProperty().getProperty())) {
                result.setTypeProperty(tripleBean.getProperty());
            }

            if (tripleBean.getType().equals(IRI)) {
                tripleBean.setUrl(Misc.toBrowsableUrl(tripleBean.getValue(), conf));
                tripleBean.setNsValue(Misc.toNsResource(tripleBean.getValue(), conf));
                if (!tripleBean.getUrl().equals(tripleBean.getValue()) || tripleBean.getValue().startsWith(conf.getPublicUrlPrefix())) {
                    tripleBean.setLocal(true);
                }
                result.addResource(tripleBean, tripleBean.getIri());
            } else if (tripleBean.getType().equals(LITERAL)) {
                result.addLiteral(tripleBean, tripleBean.getIri());
            } else if (tripleBean.getType().equals(BNODE)) {
                result.addBnode(tripleBean, tripleBean.getIri());
            }
        }

        result.setImages(images);
        result.setLinking(linking);
        result.setVideos(videos);
        result.setAudios(audios);

        return result;
    }

    public String buildRDFResource(String iri, String sparql, Lang lang, ConfigurationBean conf) throws Exception {
        String result;
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(conf.getPrefixes());

        SPARQLEndPoint se = new SPARQLEndPoint(conf, null, null);
        model = se.extractData(model, iri, sparql, conf.getDefaultRawDataQueries());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RDFWriterI rdfWriter = model.getWriter(lang.getName());
        rdfWriter.setProperty(SHOW_XML_DECLARATION, "true");
        rdfWriter.setProperty(RELATIVE_URIS, "");

        rdfWriter.write(model, baos, conf.getIRInamespace());

        result = baos.toString(StandardCharsets.UTF_8);

        return result;
    }

    public String buildRDFResource(String iri, Model m, Lang lang, ConfigurationBean conf) throws Exception {
        String result;
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefixes(conf.getPrefixes());

        SPARQLEndPoint se = new SPARQLEndPoint(conf, null, null);
        model = se.extractLocalData(model, iri, m, conf.getDefaultRawDataQueries());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RDFWriterI rdfWriter = model.getWriter(lang.getName());
        rdfWriter.setProperty(SHOW_XML_DECLARATION, "true");
        rdfWriter.setProperty(RELATIVE_URIS, "");

        rdfWriter.write(model, baos, conf.getIRInamespace());
        rdfWriter.setProperty(SHOW_XML_DECLARATION, "true");
        rdfWriter.setProperty(RELATIVE_URIS, "");

        result = baos.toString(StandardCharsets.UTF_8);

        return result;
    }

    public ResultBean buildPartialHtmlResource(String iri, String[] abouts, Locale locale, ConfigurationBean conf, OntologyBean ontoBean, List<String> filterProperties) {

        SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());
        ResultBean result = new ResultBean();
        List<TripleBean> literals = new ArrayList<>();

        String preferredLanguage = conf.getPreferredLanguage();
        if (preferredLanguage.equals("auto")) {
            preferredLanguage = locale.getLanguage();
        }

        List<TripleBean> triples = new ArrayList<>();

        /*
         * FIXME: make more distinct queries to avoid length limits, eg
         * http://dati.camera.it/ocd/assemblea.rdf/a16
         */

        StringBuilder filter = new StringBuilder();
        for (String titleProperty : filterProperties) {
            if (titleProperty.toLowerCase().startsWith("http:")) {
                filter.append("(?filterProperty = <").append(titleProperty).append(">)");
            } else {
                filter.append("(?filterProperty = ").append(titleProperty).append(")");
            }
            filter.append(" || ");
        }

        for (String about : abouts) {
            List<String> sparqlQueries = new ArrayList<>();
            sparqlQueries.add(("select distinct ?o  " + "{ <" + about + "> ?filterProperty ?o. FILTER (" + filter + "))}  ").replaceAll("\\|\\| \\)", ""));
            try {

                if (conf.getEndPointUrl().equals("<>")) {
                    /* looking for data via content negotiation */
                    Model m = ModelFactory.createDefaultModel();
                    try {
                        m.read(about);
                    } catch (Exception e) {
                        throw new Exception(messageSource.getMessage(ERROR_NO_CONTENT_NEGOTIATION, null, SORRY_BUT_CONTENT_NEGOTIATION_IS_NOT_SUPPORTED_BY_THE_IRI, locale));
                    }
                    triples.addAll(se.doLocalQuery(m, about, sparqlQueries, about));
                } else {
                    triples.addAll(se.doQuery(null, sparqlQueries, about));
                }

            } catch (Exception e) {
            }
        }

        Map<String, List<TripleBean>> l = new HashMap<>();

        for (TripleBean tripleBean : triples) {
            if (tripleBean.getType().equals(LITERAL)) {
                List<TripleBean> al = l.get(tripleBean.getProperty().getProperty());
                if (al == null) {
                    al = new ArrayList<>();
                }
                al.add(tripleBean);
                l.put(tripleBean.getProperty().getProperty(), al);
            }
        }

        for (Map.Entry<String, List<TripleBean>> about : l.entrySet()) {
            List<TripleBean> al = about.getValue();
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
        result.setLiterals(iri, literals);
        return result;
    }

    public ResultBean buildHtmlInverseResource(String iri, String property, int start, Locale locale, ConfigurationBean conf, OntologyBean ontoBean) throws Exception {
        ResultBean result = new ResultBean();

        SPARQLEndPoint se = new SPARQLEndPoint(conf, ontoBean, locale.getLanguage());
        String preferredLanguage = conf.getPreferredLanguage();
        if (preferredLanguage.equals("auto")) {
            preferredLanguage = locale.getLanguage();
        }
        if (property == null) {
            /* counting */
            List<TripleBean> resources = new ArrayList<>();
            List<TripleBean> triples;

            if (conf.getEndPointUrl().equals("<>")) {
                /* looking for data via content negotiation */
                Model m = ModelFactory.createDefaultModel();
                try {
                    m.read(iri);
                } catch (Exception e) {
                    throw new Exception(messageSource.getMessage(ERROR_NO_CONTENT_NEGOTIATION, null, SORRY_BUT_CONTENT_NEGOTIATION_IS_NOT_SUPPORTED_BY_THE_IRI, locale));
                }
                triples = se.doLocalQuery(m, iri, conf.getDefaultInversesCountQueries());
            } else {
                triples = se.doQuery(iri, conf.getDefaultInversesCountQueries(), null);
            }

            for (TripleBean tripleBean : triples) {
                if (tripleBean.getType().equals(LITERAL)) {
                    resources.add(tripleBean);
                }
            }

            result.setResources(iri, resources);

        } else {
            /* listing */
            List<TripleBean> resources = new ArrayList<>();
            List<TripleBean> triples;

            if (conf.getEndPointUrl().equals("<>")) {
                /* looking for data via content negotiation */
                Model m = ModelFactory.createDefaultModel();
                try {
                    m.read(iri);
                } catch (Exception e) {
                    throw new Exception(messageSource.getMessage(ERROR_NO_CONTENT_NEGOTIATION, null, SORRY_BUT_CONTENT_NEGOTIATION_IS_NOT_SUPPORTED_BY_THE_IRI, locale));
                }
                triples = se.doLocalQuery(m, iri, property, start, conf.getDefaultInversesQueries(), null);
            } else {
                triples = se.doQuery(iri, property, start, conf.getDefaultInversesQueries(), null, null);
            }

            Map<String, TripleBean> controlList = new HashMap<>();
            for (TripleBean tripleBean : triples) {
                if (tripleBean.getType().equals(LITERAL)) {
                    if (controlList.get(tripleBean.getProperty().getProperty()) == null || preferredLanguage.equals(tripleBean.getLang())) {
                        controlList.put(tripleBean.getProperty().getProperty(), tripleBean);
                    }
                }
            }

            controlList.forEach((ignored, tripleBean) -> resources.add(tripleBean));
            result.setResources(iri, resources);

        }

        return result;
    }

    public ResultBean buildHtmlInverseResource(String iri, Locale locale, ConfigurationBean conf, OntologyBean ontoBean) throws Exception {
        return buildHtmlInverseResource(iri, null, -1, locale, conf, ontoBean);
    }
}
