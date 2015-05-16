package org.dvcama.lodview.controllers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jena.atlas.json.io.parser.JSONParser;
import org.dvcama.lodview.bean.OntologyBean;
import org.dvcama.lodview.bean.ResultBean;
import org.dvcama.lodview.builder.ResourceBuilder;
import org.dvcama.lodview.conf.ConfigurationBean;
import org.dvcama.lodview.utils.Misc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.UrlPathHelper;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping(value = "/pedigree")
public class PedigreeController {
	@Autowired
	private MessageSource messageSource;

	@Autowired
	ConfigurationBean conf;

	@Autowired
	OntologyBean ontoBean;

	@RequestMapping(value = "/page")
	public Object pedigreePageController(ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "IRI") String IRI, @RequestParam(value = "output", defaultValue = "") String output, @CookieValue(value = "colorPair", defaultValue = "") String colorPair) throws UnsupportedEncodingException {
		if (colorPair.equals("")) {
			colorPair = conf.getRandomColorPair();
			Cookie c = new Cookie("colorPair", colorPair);
			c.setPath("/");
			res.addCookie(c);
		}
		return pedigreePage(conf, model, req, res, locale, output, IRI, colorPair);
	}

	private Object pedigreePage(ConfigurationBean conf, ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, String output, String IRI, String colorPair) {

		model.addAttribute("conf", conf);
		model.addAttribute("Misc", new Misc());

		System.out.println("####################################################################");
		System.out.println("#################  looking for " + IRI + " in pedigree ################# ");

		try {
			model.addAttribute("contextPath", new UrlPathHelper().getContextPath(req));
			model.addAttribute("ontoBean", ontoBean);
			ResourceBuilder builder = new ResourceBuilder(messageSource);
			ResultBean results = builder.buildPartialHtmlResource(IRI, IRI.split("@@@@@@"), locale, conf, ontoBean, conf.getTitleProperties());
			model.put("results", results);

			return "widget/pedigree";
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && e.getMessage().startsWith("404")) {
				return new ErrorController(conf).error404(res, model, e.getMessage(), colorPair, IRI, conf.getEndPointUrl());
			} else {
				return new ErrorController(conf).error500(res, model, e.getMessage(), colorPair, IRI, conf.getEndPointUrl());
			}
		}

	}

	@ResponseBody
	@RequestMapping(value = "/data", produces = "application/json;charset=UTF-8")
	public Object pedigreeDataController(ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "IRI") String IRI, @RequestParam(value = "output", defaultValue = "") String output, @CookieValue(value = "colorPair", defaultValue = "") String colorPair) throws UnsupportedEncodingException {
		if (colorPair.equals("")) {
			colorPair = conf.getRandomColorPair();
			Cookie c = new Cookie("colorPair", colorPair);
			c.setPath("/");
			res.addCookie(c);
		}
		return pedigreeData(conf, model, req, res, locale, output, IRI, colorPair);
	}

	public Object pedigreeData( ConfigurationBean conf, ModelMap model, HttpServletRequest req, HttpServletResponse res, Locale locale, String output, String IRI, String colorPair) throws UnsupportedEncodingException {
		model.addAttribute("conf", conf);
		model.addAttribute("Misc", new Misc());

		System.out.println("####################################################################");
		System.out.println("#################  looking for " + IRI + " in pedigree byg.portal ################# ");

		try {
			model.addAttribute("contextPath", new UrlPathHelper().getContextPath(req));
			model.addAttribute("ontoBean", ontoBean);
			ResourceBuilder builder = new ResourceBuilder(messageSource);
			Map<Object, Object> result = builder.buildPedegreeData(IRI, conf, ontoBean, locale);
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(result);
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null && e.getMessage().startsWith("404")) {
				return new ErrorController(conf).error404(res, model, e.getMessage(), colorPair, IRI, conf.getEndPointUrl());
			} else {
				return new ErrorController(conf).error500(res, model, e.getMessage(), colorPair, IRI, conf.getEndPointUrl());
			}
		}

	}

}
