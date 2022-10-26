package it.gov.innovazione.lodviewng.conf;


import it.gov.innovazione.lodviewng.utils.ResourceClassPathLoader;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@Data
@RequiredArgsConstructor
public class ConfigurationBean implements ServletContextAware, Cloneable {

    private static final Random RAND = new Random();
    protected final String confFile;
    protected Model confModel;
    protected ServletContext context;
    private String endPointType;
    private String redirectionStrategy;
    private String forceIriEncoding;
    private String httpRedirectExcludeList;
    private String homeUrl;
    private String license;
    private String httpRedirectSuffix;
    private String httpRedirectPrefix;
    private String endPointUrl;
    private String IRInamespace;
    private String contentEncoding;
    private String staticResourceURL;
    private String preferredLanguage;
    private String publicUrlPrefix;
    private String publicUrlSuffix = "";
    private String authUsername;
    private String authPassword;
    private String defaultInverseBehaviour = "collapse";
    private ColorStrategy colorStrategy = ColorStrategy.RANDOM;
    private List<String> defaultQueries;
    private List<String> defaultRawDataQueries;
    private List<String> defaultInversesQueries;
    private List<String> defaultInversesTest;
    private List<String> defaultInversesCountQueries;
    private List<String> typeProperties;
    private List<String> audioProperties;
    private List<String> imageProperties;
    private List<String> videoProperties;
    private List<String> linkingProperties;
    private List<String> titleProperties;
    private List<String> descriptionProperties;
    private List<String> longitudeProperties;
    private List<String> latitudeProperties;
    private List<String> colorPair;
    private List<String> skipDomains;
    private List<String> mainOntologiesPrefixes;
    private Map<String, String> colorPairMatcher;

    public ColorStrategy getColorStrategy() {
        return colorStrategy;
    }

    public void populateBean() throws Exception {
        log.debug("Initializing configuration " + confFile);

        confModel = RDFDataMgr.loadModel(ResourceClassPathLoader.toFile("conf/" + confFile).getAbsolutePath());

        endPointUrl = getSingleConfValue("endpoint");
        endPointType = getSingleConfValue("endpointType", "");
        authPassword = getSingleConfValue("authPassword");
        authUsername = getSingleConfValue("authUsername");
        forceIriEncoding = getSingleConfValue("forceIriEncoding", "auto");
        redirectionStrategy = getSingleConfValue("redirectionStrategy", "");

        IRInamespace = getSingleConfValue("IRInamespace", "<not provided>");

        httpRedirectSuffix = getSingleConfValue("httpRedirectSuffix", "");
        httpRedirectPrefix = getSingleConfValue("httpRedirectPrefix", "");
        httpRedirectExcludeList = getSingleConfValue("httpRedirectExcludeList", "");

        publicUrlPrefix = getSingleConfValue("publicUrlPrefix", "");
        publicUrlPrefix = publicUrlPrefix.replaceAll("^(.+/)?auto$", context.getContextPath() + "/");

        contentEncoding = getSingleConfValue("contentEncoding");
        staticResourceURL = getSingleConfValue("staticResourceURL", "");
        homeUrl = getSingleConfValue("homeUrl", "/");
        staticResourceURL = staticResourceURL.replaceAll("^(.+/)?auto$", context.getContextPath() + "/staticResources/");

        preferredLanguage = getSingleConfValue("preferredLanguage");

        typeProperties = getMultiConfValue("typeProperties");
        titleProperties = getMultiConfValue("titleProperties");
        descriptionProperties = getMultiConfValue("descriptionProperties");
        imageProperties = getMultiConfValue("imageProperties");
        audioProperties = getMultiConfValue("audioProperties");
        videoProperties = getMultiConfValue("videoProperties");
        linkingProperties = getMultiConfValue("linkingProperties");
        longitudeProperties = getMultiConfValue("longitudeProperties");
        latitudeProperties = getMultiConfValue("latitudeProperties");

        defaultQueries = getMultiConfValue("defaultQueries");
        defaultRawDataQueries = getMultiConfValue("defaultRawDataQueries");

        defaultInversesQueries = getMultiConfValue("defaultInversesQueries");
        defaultInversesTest = getMultiConfValue("defaultInversesTest");
        defaultInversesCountQueries = getMultiConfValue("defaultInversesCountQueries");

        defaultInverseBehaviour = getSingleConfValue("defaultInverseBehaviour", defaultInverseBehaviour);
        mainOntologiesPrefixes = getMultiConfValue("mainOntologiesPrefixes");

        license = getSingleConfValue("license", "");

        colorPair = getMultiConfValue("colorPair");

        if (colorPair.size() == 1 && colorPair.get(0).startsWith("http://")) {
            String colorPairSuffix = colorPair.get(0).replace("http://lodview.it/conf#", "");
            if ("byClass".equals(colorPairSuffix)) {
                colorStrategy = ColorStrategy.CLASS;
            } else if ("byPrefix".equals(colorPairSuffix)) {
                colorStrategy = ColorStrategy.PREFIX;
            }
            colorPairMatcher = populateColorPairMatcher();
        }

        skipDomains = getMultiConfValue("skipDomains");
    }

    private Map<String, String> populateColorPairMatcher() {
        Map<String, String> result = new HashMap<>();
        ResIterator iter = confModel.listSubjectsWithProperty(confModel.createProperty(confModel.getNsPrefixURI("conf"), "hasColorPair"));
        while (iter.hasNext()) {
            Resource res = iter.next();
            NodeIterator values = confModel.listObjectsOfProperty(res, confModel.createProperty(confModel.getNsPrefixURI("conf"), "hasColorPair"));
            if (values.hasNext()) {
                result.put(res.toString(), values.next().toString());
            }
        }
        return result;
    }

    private String getSingleConfValue(String prop) {
        return getSingleConfValue(prop, null);
    }

    private String getSingleConfValue(String prop, String defaultValue) {
        String value = System.getenv("LodView" + prop);
        if (value != null) {
            return value;
        }
        NodeIterator iter = confModel.listObjectsOfProperty(confModel.createProperty(confModel.getNsPrefixURI("conf"), prop));
        if (iter.hasNext()) {
            return iter.next().toString();
        }
        return defaultValue;
    }

    private List<String> getMultiConfValue(String prop) {
        List<String> result = new ArrayList<>();
        NodeIterator iter = confModel.listObjectsOfProperty(confModel.createProperty(confModel.getNsPrefixURI("conf"), prop));
        while (iter.hasNext()) {
            RDFNode node = iter.next();
            result.add(node.toString());
        }
        return result;
    }

    @Override
    public void setServletContext(ServletContext arg0) {
        this.context = arg0;
        try {
            populateBean();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Map<String, String> getPrefixes() {
        return confModel.getNsPrefixMap();
    }

    public String getNsPrefixURI(String prefix) {
        return confModel.getNsPrefixURI(prefix);
    }

    public String getNsURIPrefix(String iri) {
        return confModel.getNsURIPrefix(iri);
    }

    public String getRandomColorPair() {
        if (colorStrategy != ColorStrategy.RANDOM) {
            return "#914848-#7d3e3e";
        }
        int randomNum = RAND.nextInt(colorPair.size());
        return colorPair.get(randomNum);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Something impossible just happened");
        }
    }

    public String getForceIriEncoding() {
        return forceIriEncoding;
    }

    public String getEndPointType() {
        return endPointType;
    }

    public enum ColorStrategy {RANDOM, CLASS, PREFIX}

}
