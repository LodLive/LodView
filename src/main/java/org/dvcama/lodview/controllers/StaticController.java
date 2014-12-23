package org.dvcama.lodview.controllers;

import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dvcama.lodview.conf.ConfigurationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UrlPathHelper;

@Controller
public class StaticController {
	@Autowired
	ConfigurationBean conf;

	@Autowired
	private MessageSource messageSource;

	public StaticController() {
		// TODO Auto-generated constructor stub
	}

	public StaticController(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@RequestMapping(value = "/")
	public String home(HttpServletRequest req, HttpServletResponse res, Model model, Locale locale, @CookieValue(value = "colorPair", defaultValue = "") String colorPair) {
		colorPair = conf.getRandomColorPair();
		Cookie c = new Cookie("colorPair", colorPair);
		c.setPath("/");
		res.addCookie(c);
		model.addAttribute("colorPair", colorPair);
		model.addAttribute("conf", conf);
		model.addAttribute("locale", locale.getLanguage());
		model.addAttribute("path", new UrlPathHelper().getContextPath(req).replaceAll("/lodview/", "/"));
		System.out.println("home controller");
		return "home";
	}

	@RequestMapping(value = "/lodviewmenu")
	public String lodviewmenu(Model model, HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "IRI") String IRI, @CookieValue(value = "colorPair", defaultValue = "") String colorPair) {
		if (colorPair.equals("")) {
			colorPair = conf.getRandomColorPair();
			Cookie c = new Cookie("colorPair", colorPair);
			c.setPath("/");
			res.addCookie(c);
		}
		return lodviewmenu(req, res, model, locale, IRI, conf, colorPair);
	}

	@RequestMapping(value = { "/lodviewcolor", "/**/lodviewcolor" })
	public ResponseEntity<String> lodviewcolor(Model model, HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "colorPair") String colorPair) {
		Cookie c = new Cookie("colorPair", colorPair);
		c.setPath("/");
		res.addCookie(c);
		return new ResponseEntity<String>(HttpStatus.OK);
	}

	public String lodviewmenu(HttpServletRequest req, HttpServletResponse res, Model model, Locale locale, @RequestParam(value = "IRI", defaultValue = "") String IRI, ConfigurationBean conf, String colorPair) {
		model.addAttribute("conf", conf);
		model.addAttribute("locale", locale.getLanguage());
		model.addAttribute("IRI", IRI);
		model.addAttribute("colorPair", colorPair);
		model.addAttribute("path", new UrlPathHelper().getContextPath(req).replaceAll("/lodview/", "/"));
		return "menu";
	}

}
