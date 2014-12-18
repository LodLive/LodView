package org.dvcama.lodview.controllers;

import org.dvcama.lodview.conf.ConfigurationBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SPARQLController {
	@Autowired
	ConfigurationBean conf;

	@RequestMapping(value = { "/sparql", "/SPARQL" })
	public String sparql() {

		/* TODO: make this configurable allowing "proxy" features */

		return "redirect:" + conf.getEndPointUrl();
	}
}
