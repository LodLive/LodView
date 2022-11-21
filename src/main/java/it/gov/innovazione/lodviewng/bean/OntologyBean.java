package it.gov.innovazione.lodviewng.bean;


import it.gov.innovazione.lodviewng.utils.ResourceClassPathLoader;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;

@Component
@Slf4j
public class OntologyBean {

    private static final String DEFAULT_VALUE = "";
    @Getter
    private final Model model = ModelFactory.createDefaultModel();
    @Value("${lode.ontoDir}")
    private String ontoDir;

    @PostConstruct
    void init() {
        List<File> ontologies = ResourceClassPathLoader.toFiles(ontoDir);

        if (ontologies.isEmpty()) {
            log.debug("no ontologies found in directory " + ontoDir);
            return;
        }

        ontologies.forEach(file -> {
            if (!file.isDirectory()) {
                try {
                    log.info("Parsing ontology " + file.getName());
                    log.debug("loading " + file.getCanonicalPath());
                    FileManager.get().readModel(model, file.getAbsolutePath());
                    log.debug("read successfully!");
                } catch (Exception e) {
                    log.error("error loading " + e.getMessage());
                }
            }
        });
    }

    public String getValue(String what, String preferredLanguage, String iri) {
        Resource resource = model.createResource(iri);
        return getSingleValue(preferredLanguage, resource, "http://www.w3.org/2000/01/rdf-schema#" + what);
    }

    public String getEscapedValue(String what, String preferredLanguage, String iri) {
        return StringEscapeUtils.escapeHtml4(getValue(what, preferredLanguage, iri));
    }

    private String getSingleValue(String preferredLanguage, Resource iri, String prop) {
        NodeIterator iter = model.listObjectsOfProperty(iri, model.createProperty(prop));
        String result = DEFAULT_VALUE;
        boolean betterTitleMatch = false;
        while (iter.hasNext()) {
            RDFNode node = iter.nextNode();
            Literal l = node.asLiteral();
            if (!betterTitleMatch && (result.equals(DEFAULT_VALUE) || l.getLanguage().equals("en") || l.getLanguage().equals(preferredLanguage))) {
                if (preferredLanguage.equals(l.getLanguage())) {
                    betterTitleMatch = true;
                }
                result = l.getLexicalForm();
            }

        }
        return result;
    }

}
