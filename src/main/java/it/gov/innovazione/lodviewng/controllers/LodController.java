package it.gov.innovazione.lodviewng.controllers;

import it.gov.innovazione.lodviewng.bean.ResultBean;
import it.gov.innovazione.lodviewng.bean.TripleBean;
import it.gov.innovazione.lodviewng.builder.ResourceBuilder;
import it.gov.innovazione.lodviewng.conf.ConfigurationBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@Slf4j
public class LodController {

    public static final String LOD_CONTROLLER_RESOURCE_LOAD = "				LodController.resource() - load - ";
    private final ConfigurationBean confLinked;
    private final MessageSource messageSource;


    @ResponseBody
    @RequestMapping(value = {"/linkedResource", "/lodview/linkedResource"}, produces = "application/xml;charset=UTF-8")
    public String resource(HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "IRI") String IRI) throws Exception {

        if (confLinked.getSkipDomains().contains(IRI.replaceAll("http[s]*://([^/]+)/.*", "$1"))) {
            return "<root error=\"true\" about=\"" + StringEscapeUtils.escapeXml11(IRI) + "\"><title>" + //
                    StringEscapeUtils.escapeXml11(messageSource.getMessage("error.skipedDomain", null, "skiping this URI", locale)) + //
                    "</title><msg><![CDATA[skiping this URI, probably offline]]></msg></root>";
        }
        try {
            log.info(LOD_CONTROLLER_RESOURCE_LOAD + IRI);

            ResultBean results = new ResourceBuilder(messageSource).buildHtmlResource(IRI, locale, confLinked, null, true);

            StringBuilder result = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root about=\"" + StringEscapeUtils.escapeXml11(IRI) + "\">");

            result.append("<title><![CDATA[" + StringEscapeUtils.escapeHtml4(results.getTitle()) + "]]></title>");

            String lang = locale.getLanguage().toLowerCase();
            String descr = "";
            List<TripleBean> descrProperties = results.getLiterals(IRI).get(results.getDescriptionProperty());
            if (descrProperties != null) {
                for (TripleBean tripleBean : descrProperties) {
                    if (lang.equals(tripleBean.getLang())) {
                        descr = tripleBean.getValue();
                        lang = tripleBean.getLang();
                        break;
                    } else if (tripleBean.getLang().equals("en")) {
                        lang = tripleBean.getLang();
                        descr = tripleBean.getValue();
                    } else if (descr.equals("")) {
                        descr = tripleBean.getValue();
                        lang = tripleBean.getLang();
                    }
                }
            }

            result.append("<description lang=\"" + lang + "\"><![CDATA[" + StringEscapeUtils.escapeHtml4(descr) + "]]></description>");

            for (String img : results.getImages()) {
                result.append("<img src=\"" + StringEscapeUtils.escapeXml11(img) + "\"/>");
            }
            for (String link : results.getLinking()) {
                result.append("<link href=\"" + StringEscapeUtils.escapeXml11(link) + "\"/>");
            }
            result.append("<links tot=\"" + results.getLinking().size() + "\"/>");
            result.append("<longitude><![CDATA[" + results.getLongitude() + "]]></longitude>");
            result.append("<latitude><![CDATA[" + results.getLatitude() + "]]></latitude>");

            result.append("</root>");
            return result.toString();

        } catch (Exception e) {
            // e.printStackTrace();
            log.error(IRI + " unable to retrieve data " + e.getMessage());
            return "<root error=\"true\" about=\"" + StringEscapeUtils.escapeXml11(IRI) + "\"><title>" + //
                    messageSource.getMessage("error.linkedResourceUnavailable", null, "unable to retrieve data", locale) + //
                    "</title><msg><![CDATA[" + e.getMessage() + "]]></msg></root>";
        }
    }
}
