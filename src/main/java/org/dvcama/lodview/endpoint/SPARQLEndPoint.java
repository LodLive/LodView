package org.dvcama.lodview.endpoint;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.PropertyBean;
import org.dvcama.lodview.bean.TripleBean;
import org.dvcama.lodview.conf.ConfigurationBean;
import org.dvcama.lodview.utils.Misc;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

public class SPARQLEndPoint {

	OntologyBean ontoBean;
	String locale = "en";
	protected ConfigurationBean conf;

	public SPARQLEndPoint(ConfigurationBean conf, OntologyBean ontoBean, String locale) {
		this.locale = locale;
		this.ontoBean = ontoBean;
		this.conf = conf;
		// TODO Auto-generated constructor stub
	}

	public List<TripleBean> doQuery(String IRI, String aProperty, int start, List<String> queries, String filter, String overrideProperty) throws Exception {
		// System.out.println("executing query on " + conf.getEndPointUrl());
		List<TripleBean> results = new ArrayList<TripleBean>();
		HttpAuthenticator auth = null;
		if (conf.getAuthPassword() != null && !conf.getAuthPassword().equals("")) {
			auth = new SimpleAuthenticator(conf.getAuthUsername(), conf.getAuthPassword().toCharArray());
		}
		for (String query : queries) {
			// System.out.println("-- " + parseQuery(query, IRI, aProperty,
			// start, filter));
			QueryExecution qe = QueryExecutionFactory.sparqlService(conf.getEndPointUrl(), parseQuery(query, IRI, aProperty, start, filter), auth);
			results = moreThenOneQuery(qe, results, 0, overrideProperty);
			qe.close();
		}

		if (results.size() == 0) {
			if (IRI != null) {
				boolean hasInverses = false;
				for (String query : conf.getDefaultInversesTest()) {
					// System.out.println("query!!! " + parseQuery(query, IRI,
					// aProperty, start, filter));
					QueryExecution qe = QueryExecutionFactory.sparqlService(conf.getEndPointUrl(), parseQuery(query, IRI, aProperty, start, filter), auth);
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
						rb.setIRI(qs.get("s").asNode().toString());
						rb.setNsIRI("_:" + rb.getIRI());
					} else if (qs.get("s") != null && qs.get("s").asNode().toString().startsWith("http://")) {
						rb.setIRI(qs.get("s").asNode().toString());
						rb.setNsIRI(Misc.toNsResource(rb.getIRI(), conf));
						rb.setUrl(Misc.toBrowsableUrl(rb.getIRI(), conf));
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
					System.err.println("error? " + e.getMessage());
					// e.printStackTrace();
				}
			}
		} catch (Exception ez) {
			if (retry < 3) {
				retry++;
				// System.out.println("query failed (" + ez.getMessage() +
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
		// System.out.println("executing query on model based on " + IRI);
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
							rb.setIRI(qs.get("s").asNode().toString());
							rb.setNsIRI("_:" + rb.getIRI());
						} else if (qs.get("s") != null && qs.get("s").asNode().toString().startsWith("http://")) {
							rb.setIRI(qs.get("s").asNode().toString());
							rb.setNsIRI(Misc.toNsResource(rb.getIRI(), conf));
							rb.setUrl(Misc.toBrowsableUrl(rb.getIRI(), conf));
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
						System.err.println("error? " + e.getMessage());
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

		// System.out.println("executing query on " + sparql);
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
					result.add(result.createStatement(subject2 != null ? subject2.asResource() : subject, property.as(Property.class), object));
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

		// System.out.println("executing query on IRI");
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
					result.add(result.createStatement(subject2 != null ? subject2.asResource() : subject, property.as(Property.class), object));
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
		System.out.println("testing connection on " + conf.getEndPointUrl());
		QueryExecution qe = QueryExecutionFactory.sparqlService(conf.getEndPointUrl(), "select ?s {?s ?p ?o} LIMIT 1");
		String msg = "";
		try {
			ResultSet rs = qe.execSelect();
			if (rs.hasNext()) {
				System.out.println("is online");
				msg = "online";
			} else {
				System.out.println("is offline");
				msg = "offline";
			}
		} catch (Exception e) {
			System.out.println("is offline " + e.getMessage());
			msg = "offline " + e.getMessage();
		} finally {
			qe.close();
		}
		return msg;
	}

}
