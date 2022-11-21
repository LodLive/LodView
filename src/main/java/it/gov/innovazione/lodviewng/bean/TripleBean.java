package it.gov.innovazione.lodviewng.bean;

import lombok.Data;

@Data
public class TripleBean {
    private PropertyBean property;
    private String nsValue;
    private String type;
    private String iri;
    private String nsIri;
    private String value;
    private String dataType;
    private String nsDataType;
    private String lang;
    private String url;
    private boolean isLocal = false;
}
