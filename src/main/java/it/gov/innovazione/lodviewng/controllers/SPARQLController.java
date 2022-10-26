package it.gov.innovazione.lodviewng.controllers;

import it.gov.innovazione.lodviewng.conf.ConfigurationBean;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
public class SPARQLController {
    private final ConfigurationBean conf;

    @RequestMapping(value = {"/sparql", "/SPARQL"})
    public String sparql() {
        return "redirect:" + conf.getEndPointUrl();
    }
}
