package it.gov.innovazione.lodviewng.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ResultBean {

    private String title;
    private String latitude;
    private String longitude;
    private String mainIRI;
    private PropertyBean descriptionProperty;
    private PropertyBean typeProperty;
    private List<String> images;
    private List<String> linking;
    private List<String> videos;
    private List<String> audios;
    private Map<String, Map<PropertyBean, List<TripleBean>>> literals = new HashMap<>();
    private Map<String, Map<PropertyBean, List<TripleBean>>> resources = new HashMap<>();
    private Map<String, Map<PropertyBean, List<TripleBean>>> bnodes = new HashMap<>();

    private void addEle(String iri, TripleBean tripleBean, Map<String, Map<PropertyBean, List<TripleBean>>> ele) {
        if (ele.get(iri) == null || ele.get(iri).get(tripleBean.getProperty()) == null) {
            Map<PropertyBean, List<TripleBean>> a = ele.get(iri);
            if (a == null) {
                a = new LinkedHashMap<>();
            }
            List<TripleBean> b = new ArrayList<>();
            b.add(tripleBean);
            a.put(tripleBean.getProperty(), b);
            ele.put(iri, a);
        } else {
            Map<PropertyBean, List<TripleBean>> a = ele.get(iri);
            List<TripleBean> b = a.get(tripleBean.getProperty());
            b.add(tripleBean);
            a.put(tripleBean.getProperty(), b);
            ele.put(iri, a);
        }
    }

    private void removeEle(String iri, TripleBean tripleBean, Map<String, Map<PropertyBean, List<TripleBean>>> ele) {
        if (!(ele.get(iri) == null || ele.get(iri).get(tripleBean.getProperty()) == null)) {
            Map<PropertyBean, List<TripleBean>> a = ele.get(iri);
            List<TripleBean> b = a.get(tripleBean.getProperty());
            b.remove(tripleBean);
            a.put(tripleBean.getProperty(), b);
            ele.put(iri, a);
        }
    }

    public void setLiterals(String iri, List<TripleBean> localLiterals) {
        for (TripleBean tripleBean : localLiterals) {
            addEle(iri, tripleBean, literals);
        }
    }

    public void setBnodes(String iri, List<TripleBean> localBnodes) {
        for (TripleBean tripleBean : localBnodes) {
            addEle(iri, tripleBean, bnodes);
        }
    }

    public void setResources(String iri, List<TripleBean> localResources) {
        for (TripleBean tripleBean : localResources) {
            addEle(iri, tripleBean, resources);
        }
    }

    public Map<PropertyBean, List<TripleBean>> getResources(String iri) {
        return resources.get(iri);
    }

    public Map<PropertyBean, List<TripleBean>> getLiterals(String iri) {
        return literals.get(iri);
    }

    public Map<PropertyBean, List<TripleBean>> getBnodes(String IRI) {
        return bnodes.get(IRI);
    }

    public void addBnode(TripleBean tripleBean, String iri) {
        addEle(iri, tripleBean, bnodes);
    }

    public void addLiteral(TripleBean tripleBean, String iri) {
        addEle(iri, tripleBean, literals);
    }

    public void addResource(TripleBean tripleBean, String iri) {
        addEle(iri, tripleBean, resources);
    }

    public void removeBnode(TripleBean tripleBean, String iri) {

        removeEle(iri, tripleBean, bnodes);
    }

    public void removeLiteral(TripleBean tripleBean, String iri) {
        removeEle(iri, tripleBean, literals);

    }

    public void removeResource(TripleBean tripleBean, String iri) {
        removeEle(iri, tripleBean, resources);
    }

}
