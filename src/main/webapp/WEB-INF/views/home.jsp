<%@page session="true"%><%@taglib uri="http://www.springframework.org/tags"
prefix="sp"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html
  version="XHTML+RDFa 1.1"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://www.w3.org/1999/xhtml http://www.w3.org/MarkUp/SCHEMA/xhtml-rdfa-2.xsd"
  xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
  xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
  xmlns:cc="http://creativecommons.org/ns#"
  xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:foaf="http://xmlns.com/foaf/0.1/"
>
  <head data-color="${colorPair}" profile="http://www.w3.org/1999/xhtml/vocab">
    <title>${results.getTitle()}&mdash;LodView</title>
    <jsp:include page="inc/header.jsp"></jsp:include>
  </head>
  <body id="top">
    <article>
      <div id="logoBanner">
        <div id="logo">
          <!-- placeholder for logo -->
        </div>
      </div>
      <header>
        <hgroup>
          <h1>
            <span>Polifonia LodView</span>
          </h1>
          <h2></h2>
        </hgroup>
        <div id="abstract">
          <div class="value">
            LodeView is the powerful tool that allows you to explore the
            entities and individuals within the Polifonia Knowledge Graph.
          </div>
        </div>
      </header>

      <aside class="empty"></aside>

      <div id="directs">
        <div class="value">
          To get started, simply copy and paste an entity URI from the Polifonia
          KG into your browser. By doing so, you will gain access to a wealth of
          information, insights, and resources about that specific entity.
          Whether you're interested in composers, musical works, historical
          periods, or genres, LodeView will guide you to discover the
          interconnectedness and significance of each entity.
        </div>
      </div>

      <div id="inverses" class="empty"></div>
      <jsp:include page="inc/custom_footer.jsp"></jsp:include>
    </article>
    <jsp:include page="inc/footer.jsp"></jsp:include>
  </body>
</html>
