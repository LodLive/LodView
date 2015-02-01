package org.dvcama.lodview.controllers;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.dvcama.lodview.conf.ConfigurationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ErrorController {
	@Autowired
	ConfigurationBean conf;

	public ErrorController(ConfigurationBean conf) {
		this.conf = conf;
	}

	public ErrorController() {
	}

	/* TODO: change the handler to send "error" param to the client */
	@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE, reason = "unhandled encoding")
	@RequestMapping(value = "/406")
	public String error406(HttpServletResponse res, ModelMap model, @CookieValue(value = "colorPair") String colorPair) {
		System.out.println("not acceptable");
		model.addAttribute("statusCode", "406");
		model.addAttribute("conf", conf);
		colors(colorPair, res, model);
		res.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
		return "error";
	}

	@RequestMapping(value = "/404")
	public String error404(HttpServletResponse res, ModelMap model, @RequestParam(value = "error", defaultValue = "") String error, @CookieValue(value = "colorPair", defaultValue = "") String colorPair, @RequestParam(value = "IRI", defaultValue = "") String IRI, @RequestParam(value = "endpoint", defaultValue = "") String endpoint) {
		System.out.println("not found " + error + " -- " + IRI + " -- " + endpoint);
		/* spring bug? */
		model.addAttribute("IRI", IRI);
		model.addAttribute("endpoint", endpoint);
		model.addAttribute("error", error);
		model.addAttribute("conf", conf);
		colors(colorPair, res, model);
		model.addAttribute("statusCode", "404");
		res.setStatus(HttpServletResponse.SC_NOT_FOUND);
		return "error";
	}

	@RequestMapping(value = "/400")
	public String error400(HttpServletResponse res, ModelMap model, @RequestParam(value = "IRI", defaultValue = "") String IRI, @CookieValue(value = "colorPair", defaultValue = "") String colorPair) {
		System.out.println("error on " + IRI);
		/* spring bug? */
		model.addAttribute("IRI", IRI.replaceAll("(http://.+),http://.+", "$1"));
		model.addAttribute("conf", conf);
		colors(colorPair, res, model);
		model.addAttribute("statusCode", "400");
		res.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
		return "error";
	}

	@RequestMapping(value = { "/500", "/error" })
	public String error500(HttpServletResponse res, ModelMap model, @RequestParam(value = "error", defaultValue = "") String error, @CookieValue(value = "colorPair", defaultValue = "") String colorPair, @RequestParam(value = "IRI", defaultValue = "") String IRI, @RequestParam(value = "endpoint", defaultValue = "") String endpoint) {
		System.out.println("error on " + error + " -- " + IRI + " -- " + endpoint);
		/* spring bug? */
		model.addAttribute("IRI", IRI);
		model.addAttribute("endpoint", endpoint);
		model.addAttribute("error", error);
		model.addAttribute("conf", conf);
		colors(colorPair, res, model);
		model.addAttribute("statusCode", "500");
		res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		return "error";
	}

	private void colors(String colorPair, HttpServletResponse res, ModelMap model) {
		if (colorPair.equals("")) {
			colorPair = conf.getRandomColorPair();
			Cookie c = new Cookie("colorPair", colorPair);
			c.setPath("/");
			res.addCookie(c);
		}
		if (conf != null && conf.getColorPairMatcher() != null && conf.getColorPairMatcher().size() > 0) {
			model.addAttribute("colorPair", conf.getColorPairMatcher().get("http://lodview.it/conf#otherClasses"));
		} else {
			model.addAttribute("colorPair", colorPair);
		}

	}
}
