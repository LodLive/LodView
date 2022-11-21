package it.gov.innovazione.lodviewng.controllers;

import it.gov.innovazione.lodviewng.builder.ResourceBuilder;
import it.gov.innovazione.lodviewng.bean.OntologyBean;
import it.gov.innovazione.lodviewng.bean.PropertyBean;
import it.gov.innovazione.lodviewng.bean.ResultBean;
import it.gov.innovazione.lodviewng.bean.TripleBean;
import it.gov.innovazione.lodviewng.conf.ConfigurationBean;
import it.gov.innovazione.lodviewng.utils.Misc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;
import static org.apache.commons.text.StringEscapeUtils.escapeXml11;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LinkedResourcesController {

    public static final String ERROR_LINKED_RESOURCE_UNAVAILABLE = "error.linkedResourceUnavailable";
    public static final String UNABLE_TO_RETRIEVE_DATA = "unable to retrieve data";
    public static final String ROOT = "</root>";
    public static final String RESOURCE_ABOUT = "<resource about=\"";
    public static final String NSABOUT = "\" nsabout=\"";
    private final ConfigurationBean conf;
    private final OntologyBean ontoBean;
    private final MessageSource messageSource;

    @ResponseBody
    @RequestMapping(value = "/linkedResourceTitles", produces = "application/xml;charset=UTF-8")
    public String resourceTitles(ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "IRI") String IRI, @RequestParam(value = "abouts[]") String[] abouts) throws Exception {
        return resourceTitles(model, conf, req, res, locale, IRI, abouts);
    }

    public String resourceTitles(ModelMap model, ConfigurationBean conf, HttpServletRequest req, HttpServletResponse res, Locale locale, String IRI, String[] abouts) throws Exception {
        StringBuilder result = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>");
        try {
            ResultBean results = new ResourceBuilder(messageSource).buildPartialHtmlResource(IRI, abouts, locale, conf, null, conf.getTitleProperties());
            Map<PropertyBean, List<TripleBean>> literals = results.getLiterals(IRI);
            if (literals == null || literals.size() == 0) {
                return ("<root error=\"true\"><title></title><msg>" + messageSource.getMessage("error.noLiteral", null, "no literal values where found", locale) + "</msg></root>");
            }
            for (Map.Entry<PropertyBean, List<TripleBean>> literal : literals.entrySet()) {
                for (TripleBean tripleBean : literal.getValue()) {
                    result.append(RESOURCE_ABOUT)
                            .append(escapeXml11(tripleBean.getProperty().getProperty()))
                            .append(NSABOUT)
                            .append(escapeXml11(tripleBean.getProperty().getNsProperty()))
                            .append("\"><title><![CDATA[")
                            .append(escapeHtml4(Misc.stripHTML(tripleBean.getValue())))
                            .append("]]></title></resource>\n");
                }
            }
            result.append(ROOT);
            return result.toString();
        } catch (Exception e) {
            // 404?
            return ("<root error=\"true\"><title>" + messageSource.getMessage(ERROR_LINKED_RESOURCE_UNAVAILABLE, null, UNABLE_TO_RETRIEVE_DATA, locale) + "</title><msg>" + e.getMessage() + "</msg></root>");
        }
    }

    @ResponseBody
    @RequestMapping(value = "/linkedResourceInverses", produces = "application/xml;charset=UTF-8")
    public String resourceInversesController(ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "IRI") String IRI, @RequestParam(value = "property", defaultValue = "") String property, @RequestParam(value = "start", defaultValue = "-1") int start) throws Exception {
        return resourceInverses(model, conf, ontoBean, req, res, locale, IRI, property, start);
    }

    public String resourceInverses(ModelMap model, ConfigurationBean conf, OntologyBean ontoBean, HttpServletRequest req, HttpServletResponse res, Locale locale, String IRI, String property, int start) throws Exception {
        StringBuilder result = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>");
        if (property.equals("")) {
            try {
                ResultBean results = new ResourceBuilder(messageSource).buildHtmlInverseResource(IRI, locale, conf, ontoBean);
                Map<PropertyBean, List<TripleBean>> resources = results.getResources(IRI);
                if (resources != null) {
                    for (Map.Entry<PropertyBean, List<TripleBean>> resource : resources.entrySet()) {
                        for (TripleBean tripleBean : resource.getValue()) {
                            if (tripleBean.getProperty().getProperty() == null || tripleBean.getProperty().getProperty().equals("")) {
                                throw new Exception("no content");
                            }
                            result.append(RESOURCE_ABOUT).append(escapeXml11(tripleBean.getProperty().getProperty()))
                                    .append(NSABOUT).append(escapeXml11(tripleBean.getProperty().getNsProperty()))
                                    .append("\" propertyurl=\"").append(escapeXml11(tripleBean.getProperty().getPropertyUrl()))
                                    .append("\" propertylabel=\"").append(escapeXml11(Misc.stripHTML(tripleBean.getProperty().getLabel())))
                                    .append("\" propertycomment=\"").append(escapeXml11(tripleBean.getProperty().getComment()))
                                    .append("\" ><count><![CDATA[").append(escapeHtml4(tripleBean.getValue()))
                                    .append("]]></count></resource>\n");
                        }
                    }
                }

                result.append(ROOT);
                return result.toString();

            } catch (Exception e) {
                return ("<root  error=\"true\" ><title>" + messageSource.getMessage(ERROR_LINKED_RESOURCE_UNAVAILABLE, null, UNABLE_TO_RETRIEVE_DATA, locale) + "</title><msg>" + e.getMessage() + "</msg></root>");
            }
        } else {

            /* retrieving inverse relations */
            try {
                ResultBean results = new ResourceBuilder(messageSource).buildHtmlInverseResource(IRI, property, start, locale, conf, null);
                Map<PropertyBean, List<TripleBean>> resources = results.getResources(IRI);
                for (Map.Entry<PropertyBean, List<TripleBean>> resource : resources.entrySet()) {
                    for (TripleBean tripleBean : resource.getValue()) {
                        result.append(RESOURCE_ABOUT).append(escapeXml11(tripleBean.getProperty().getProperty()))
                                .append(NSABOUT).append(escapeXml11(tripleBean.getProperty().getNsProperty()))
                                .append("\" propertyurl=\"").append(escapeXml11(tripleBean.getProperty().getPropertyUrl()))
                                .append("\" ></resource>\n");
                    }
                }

                result.append(ROOT);
                return result.toString();

            } catch (Exception e) {
                e.printStackTrace();
                return ("<root  error=\"true\" ><title>" + messageSource.getMessage(ERROR_LINKED_RESOURCE_UNAVAILABLE, null, UNABLE_TO_RETRIEVE_DATA, locale) + "</title><msg><![CDATA[" + e.getMessage() + "]]></msg></root>");
            }
        }
    }
}
