package it.innovaway.lodviewng.lodview.bean;

import lombok.Data;

@Data
public class PropertyBean {
    private String nsProperty;
    private String property;
    private String propertyUrl;
    private String label = "";
    private String comment = "";
}
