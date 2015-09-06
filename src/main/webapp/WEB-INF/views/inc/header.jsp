<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@taglib uri="http://www.springframework.org/tags" prefix="sp"%><%@page session="true"%>
<link href="${conf.getStaticResourceURL()}style.css" rel="stylesheet" type="text/css" />
<script>
	document.write('<style type="text/css">');
	document.write('.c2{visibility:hidden}');
	document.write('</style>');
</script>
<meta http-equiv="x-ua-compatible" content="IE=Edge"/>
<script src="${conf.getStaticResourceURL()}vendor/jquery.min.js"></script>
<meta property="og:title" content="${results.getTitle()} &mdash; LodView, giving data a new shape">
<meta property="og:image" content="${conf.getStaticResourceURL()}img/lodview_sharer.png">
<link rel="image_src" href="${conf.getStaticResourceURL()}img/lodview_sharer.png">
<meta name="twitter:title" content="${results.getTitle()} &mdash; LodView, giving data a new shape">
<meta name="twitter:description" content="LodView is a powerful RDF viewer, IRI dereferencer and opensource SPARQL navigator">
<link rel="icon" type="image/png" href="${conf.getStaticResourceURL()}img/favicon.png">
<link href='//fonts.googleapis.com/css?family=Roboto:100,300,500&subset=latin-ext,latin,greek-ext,greek,cyrillic-ext,vietnamese,cyrillic' rel='stylesheet' type='text/css'>
<!-- managing maps  -->
<link rel="stylesheet" href="${conf.getStaticResourceURL()}vendor/leaflet/leaflet.css" />
<script src="${conf.getStaticResourceURL()}vendor/leaflet/leaflet.js"></script>
<link rel="canonical" href="${results.getMainIRI()}" >
<script src="${conf.getStaticResourceURL()}vendor/masonry.pkgd.min.js"></script>
<script src="${conf.getStaticResourceURL()}vendor/modernizr-custom.min.js"></script>
<c:set var="color1" value='${colorPair.replaceAll("-.+","") }' scope="page" />
<c:set var="color2" value='${colorPair.replaceAll(".+-","") }' scope="page" />
<style type="text/css">
hgroup, #linking a span,#audio .audio{
	background-color: ${color1
}

}
header div#abstract, #loadPanel, div#lodCloud .connected div#counterBlock.content
	{
	background-color: ${color2
}

}
#errorPage div#bnodes {
	color: ${color2
}

}
div#loadPanel span.ok img {
	background-color: ${color1
}
}
</style>
<script>
	var isRetina = window.devicePixelRatio > 1;
	var isChrome = /chrom(e|ium)/.test(navigator.userAgent.toLowerCase())
</script>
<c:set var="hasMap" scope="page" value='${results.getLatitude()!=null && !results.getLatitude().equals("") && results.getLongitude()!=null&&!results.getLongitude().equals("")}'></c:set>
<c:set var="hasLod" scope="page" value="${results.getLinking()!=null && results.getLinking().size()>0}"></c:set>
<c:set var="hasImages" scope="page" value="${results.getImages()!=null && results.getImages().size()>0}"></c:set>
<c:set var="hasVideos" scope="page" value="${results.getVideos()!=null && results.getVideos().size()>0}"></c:set>
<c:set var="hasAudios" scope="page" value="${results.getAudios()!=null && results.getAudios().size()>0}"></c:set>
<c:if test="${hasAudios }">	 
	<link rel="stylesheet" href="${conf.getStaticResourceURL()}vendor/jplayercircle/circle.skin/circle.player.css">
	<script type="text/javascript" src="${conf.getStaticResourceURL()}vendor/jplayercircle/js/jquery.transform.js"></script>
	<script type="text/javascript" src="${conf.getStaticResourceURL()}vendor/jplayercircle/js/jquery.grab.js"></script>
	<script type="text/javascript" src="${conf.getStaticResourceURL()}vendor/jplayercircle/js/jquery.jplayer.js"></script>
	<script type="text/javascript" src="${conf.getStaticResourceURL()}vendor/jplayercircle/js/mod.csstransforms.min.js"></script>
	<script type="text/javascript" src="${conf.getStaticResourceURL()}vendor/jplayercircle/js/circle.player.js"></script>
</c:if>	