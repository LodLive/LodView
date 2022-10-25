package it.innovaway.lodviewng.lodview.endpoint;


import it.innovaway.lodviewng.lodview.bean.OntologyBean;
import it.innovaway.lodviewng.lodview.bean.PropertyBean;
import it.innovaway.lodviewng.lodview.bean.TripleBean;
import it.innovaway.lodviewng.lodview.conf.ConfigurationBean;
import it.innovaway.lodviewng.lodview.utils.Misc;
import org.apache.jena.graph.Node;
import org.apache.jena.http.auth.AuthEnv;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.exec.http.QueryExecutionHTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class SPARQLEndPoint {

    private static final Logger logger = LoggerFactory.getLogger(SPARQLEndPoint.class);
    protected ConfigurationBean conf;
    OntologyBean ontoBean;
    String locale = "en";

    public SPARQLEndPoint(ConfigurationBean conf, OntologyBean ontoBean, String locale) {
        this.locale = locale;
        this.ontoBean = ontoBean;
        this.conf = conf;
        // TODO Auto-generated constructor stub
    }

    public List<TripleBean> doQuery(String IRI, String aProperty, int start, List<String> queries, String filter, String overrideProperty) throws Exception {
        // logger.trace("executing query on " + conf.getEndPointUrl());
        List<TripleBean> results = new ArrayList<TripleBean>();
        if (conf.getAuthPassword() != null && !conf.getAuthPassword().equals("")) {
            AuthEnv.get().registerUsernamePassword(URI.create(conf.getEndPointUrl()), conf.getAuthUsername(), conf.getAuthPassword());
        }
        for (String query : queries) {
            // logger.trace("-- " + parseQuery(query, IRI, aProperty,
            // start, filter));
            QueryExecution qe = QueryExecutionHTTP.create()
                    .endpoint(conf.getEndPointUrl())
                    .query(parseQuery(query, IRI, aProperty, start, filter))
                    .build();
            //QueryExecutionFactory.sparqlService(conf.getEndPointUrl(), parseQuery(query, IRI, aProperty, start, filter), auth);
            results = moreThenOneQuery(qe, results, 0, overrideProperty);
            qe.close();
        }

        if (results.size() == 0) {
            if (IRI != null) {
                boolean hasInverses = false;
                for (String query : conf.getDefaultInversesTest()) {
                    // logger.trace("query!!! " + parseQuery(query, IRI,
                    // aProperty, start, filter));
                    QueryExecution qe = QueryExecutionHTTP.create()
                            .endpoint(conf.getEndPointUrl())
                            .query(parseQuery(query, IRI, aProperty, start, filter))
                            .build();
                    if (!hasInverses) {
                        hasInverses = qe.execAsk();
                    }
                    qe.close();
                }
                if (!hasInverses) {
                    throw new Exception("404 - not found");
                }
            }
        }
        return results;
    }

    private List<TripleBean> moreThenOneQuery(QueryExecution qe, List<TripleBean> results, int retry, String overrideProperty) throws Exception {

        try {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                TripleBean rb = new TripleBean();
                QuerySolution qs = rs.next();
                String property = "";
                if (overrideProperty != null) {
                    property = overrideProperty;
                } else if (qs.get("p") != null) {
                    property = qs.get("p").asNode().toString();
                }

                try {
                    if (qs.get("s") != null && !qs.get("s").asNode().toString().startsWith("http://")) { // probably
                        // a
                        // bn
                        rb.setIri(qs.get("s").asNode().toString());
                        rb.setNsIri("_:" + rb.getIri());
                    } else if (qs.get("s") != null && qs.get("s").asNode().toString().startsWith("http://")) {
                        rb.setIri(qs.get("s").asNode().toString());
                        rb.setNsIri(Misc.toNsResource(rb.getIri(), conf));
                        rb.setUrl(Misc.toBrowsableUrl(rb.getIri(), conf));
                    }

                    PropertyBean p = new PropertyBean();
                    p.setNsProperty(Misc.toNsResource(property, conf));
                    p.setProperty(property);
                    if (ontoBean != null) {
                        p.setLabel(ontoBean.getEscapedValue("label", locale, property));
                        p.setComment(ontoBean.getEscapedValue("comment", locale, property));
                    }
                    p.setPropertyUrl(Misc.toBrowsableUrl(property, conf));
                    rb.setProperty(p);
                    if (qs.get("o") != null) {
                        Node object = qs.get("o").asNode();
                        if (object.isURI()) {
                            rb.setType("iri");
                            rb.setValue(object.toString(false));
                        } else if (object.isLiteral()) {
                            rb.setType("literal");
                            rb.setDataType(object.getLiteralDatatypeURI());
                            rb.setNsDataType(Misc.toNsResource(object.getLiteralDatatypeURI(), conf));
                            rb.setLang(object.getLiteralLanguage());
                            rb.setValue(object.getLiteralLexicalForm());
                        } else if (object.isBlank()) {
                            rb.setType("bnode");
                            rb.setValue(object.toString(false));
                        }
                    } else {
                        rb.setType("literal");
                        rb.setValue("");
                    }
                    results.add(rb);
                } catch (Exception e) {
                    logger.error("error? " + e.getMessage());
                    // e.printStackTrace();
                }
            }
        } catch (Exception ez) {
            if (retry < 3) {
                retry++;
                // logger.trace("query failed (" + ez.getMessage() +
                // "), I'm giving another chance (" + retry + "/3)");
                return moreThenOneQuery(qe, results, retry, overrideProperty);
            }
            ez.printStackTrace();
            throw new Exception("connection refused");
        }

        return results;
    }

    public List<TripleBean> doQuery(String IRI, List<String> queries, String overrideProperty) throws Exception {
        return doQuery(IRI, null, -1, queries, null, overrideProperty);
    }

    public List<TripleBean> doLocalQuery(Model m, String IRI, List<String> queries, String about) throws Exception {
        return doLocalQuery(m, IRI, null, -1, queries, about);
    }

    public List<TripleBean> doLocalQuery(Model model, String IRI, List<String> queries) throws Exception {
        return doLocalQuery(model, IRI, null, -1, queries, null);
    }

    public List<TripleBean> doLocalQuery(Model model, String IRI, String localProperty, int start, List<String> queries, String overrideProperty) throws Exception {
        // logger.trace("executing query on model based on " + IRI);
        List<TripleBean> results = new ArrayList<TripleBean>();

        for (String query : queries) {
            QueryExecution qe = QueryExecutionFactory.create(parseQuery(query, IRI, localProperty, start, null), model);
            try {
                ResultSet rs = qe.execSelect();
                while (rs.hasNext()) {
                    TripleBean rb = new TripleBean();
                    QuerySolution qs = rs.next();
                    String property = "";
                    if (overrideProperty != null) {
                        property = overrideProperty;
                    } else if (qs.get("p") != null) {
                        property = qs.get("p").asNode().toString();
                    }
                    try {
                        if (qs.get("s") != null && !qs.get("s").asNode().toString().startsWith("http://")) { // probably
                            // blanknode
                            rb.setIri(qs.get("s").asNode().toString());
                            rb.setNsIri("_:" + rb.getIri());
                        } else if (qs.get("s") != null && qs.get("s").asNode().toString().startsWith("http://")) {
                            rb.setIri(qs.get("s").asNode().toString());
                            rb.setNsIri(Misc.toNsResource(rb.getIri(), conf));
                            rb.setUrl(Misc.toBrowsableUrl(rb.getIri(), conf));
                        }
                        PropertyBean p = new PropertyBean();
                        p.setNsProperty(Misc.toNsResource(property, conf));
                        p.setProperty(property);
                        p.setPropertyUrl(Misc.toBrowsableUrl(property, conf));
                        if (ontoBean != null) {
                            p.setLabel(ontoBean.getEscapedValue("label", locale, property));
                            p.setComment(ontoBean.getEscapedValue("comment", locale, property));
                        }
                        rb.setProperty(p);
                        if (qs.get("o") != null) {
                            Node object = qs.get("o").asNode();
                            if (object.isURI()) {
                                rb.setType("iri");
                                rb.setValue(object.toString(false));
                            } else if (object.isLiteral()) {
                                rb.setType("literal");
                                rb.setDataType(object.getLiteralDatatypeURI());
                                rb.setNsDataType(Misc.toNsResource(object.getLiteralDatatypeURI(), conf));
                                rb.setLang(object.getLiteralLanguage());
                                rb.setValue(object.getLiteralLexicalForm());
                            } else if (object.isBlank()) {
                                rb.setType("bnode");
                                rb.setValue(object.toString(false));
                            }
                        } else {
                            rb.setType("literal");
                            rb.setValue("");
                        }
                        results.add(rb);
                    } catch (Exception e) {
                        logger.error("error? " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                throw new Exception("500 - " + e.getMessage());
            } finally {
                qe.close();
            }
        }

        if (results.size() == 0) {
            throw new Exception("404 - not found");
        }
        return results;
    }

    public Model extractData(Model result, String IRI, String sparql, List<String> queries) throws Exception {

        // logger.trace("executing query on " + sparql);
        Resource subject = result.createResource(IRI);
        for (String query : queries) {
            QueryExecution qe = QueryExecutionFactory.sparqlService(sparql, parseQuery(query, IRI, null, -1, null));
            try {
                ResultSet rs = qe.execSelect();

                List<Statement> sl = new ArrayList<Statement>();
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    RDFNode subject2 = qs.get("s");
                    RDFNode property = qs.get("p");
                    RDFNode object = qs.get("o");
                    Property property1 = result.createProperty(property.asNode().toString());
                    result.add(result.createStatement(subject2 != null ? subject2.asResource() : subject, property1, object));
                }
                result.add(sl);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("error in query execution: " + e.getMessage());
            } finally {
                qe.close();
            }
        }
        return result;
    }

    public Model extractLocalData(Model result, String IRI, Model m, List<String> queries) throws Exception {

        // logger.trace("executing query on IRI");
        Resource subject = result.createResource(IRI);
        for (String query : queries) {
            QueryExecution qe = QueryExecutionFactory.create(parseQuery(query, IRI, null, -1, null), m);
            try {
                ResultSet rs = qe.execSelect();
                List<Statement> sl = new ArrayList<Statement>();
                while (rs.hasNext()) {
                    QuerySolution qs = rs.next();
                    RDFNode subject2 = qs.get("s");
                    RDFNode property = qs.get("p");
                    RDFNode object = qs.get("o");
                    Property property1 = result.createProperty(property.asNode().toString());
                    result.add(result.createStatement(subject2 != null ? subject2.asResource() : subject, property1, object));
                }
                result.add(sl);
            } catch (Exception e) {
                e.printStackTrace();
                throw new Exception("error in query execution: " + e.getMessage());
            } finally {
                qe.close();
            }
        }
        return result;
    }

    public String parseQuery(String query, String IRI, String property, int start, String filter) {
        if (IRI != null) {
            /* managing issues depending on "$" in some IRIs */
            query = query.replaceAll("\\$\\{IRI\\}", IRI.replaceAll("\\$", "%24")).replaceAll("%24", "\\$");
        }
        if (property != null) {
            query = query.replaceAll("\\$\\{PROPERTY\\}", property);
        }
        if (filter != null) {
            query = query.replaceAll("\\$\\{FILTERPROPERTY\\}", filter);
        }
        if (query.indexOf("STARTFROM") > 0) {
            query = query.replaceAll("\\$\\{STARTFROM\\}", "" + start);
        } else if (start > 0) {
            query = query.replaceAll("LIMIT (.+)$", "OFFSET " + start + " LIMIT $1");
        }
        return query;
    }

    public String testEndpoint(ConfigurationBean conf) {
        logger.info("testing connection on " + conf.getEndPointUrl());
        QueryExecution qe = QueryExecutionFactory.sparqlService(conf.getEndPointUrl(), "select ?s {?s ?p ?o} LIMIT 1");
        String msg = "";
        try {
            ResultSet rs = qe.execSelect();
            if (rs.hasNext()) {
                logger.info("is online");
                msg = "online";
            } else {
                logger.info("is offline");
                msg = "offline";
            }
        } catch (Exception e) {
            logger.info("is offline " + e.getMessage());
            msg = "offline " + e.getMessage();
        } finally {
            qe.close();
        }
        return msg;
    }

}
