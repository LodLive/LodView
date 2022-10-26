package it.gov.innovazione.lodviewng.controllers;

import it.gov.innovazione.lodviewng.conf.ConfigurationBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StaticController {


    private final ConfigurationBean conf;

    private final MessageSource messageSource;

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
        log.debug("home controller");
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

    @RequestMapping(value = {"/lodviewcolor", "/**/lodviewcolor"})
    public ResponseEntity<String> lodviewcolor(Model model, HttpServletRequest req, HttpServletResponse res, Locale locale, @RequestParam(value = "colorPair") String colorPair) {
        Cookie c = new Cookie("colorPair", colorPair);
        c.setPath("/");
        res.addCookie(c);
        return new ResponseEntity<>(HttpStatus.OK);
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
