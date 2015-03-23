# LodView
LodView is a Java web application based on Spring and Jena, it's a tool able to offer a W3C standard compliant IRI dereferenciation. LodView, in conjunction with a SPARQL endpoint, allows you to publish RDF data according to all defined standards for Linked Open Data.  
LodView is easy to configure and deploy for any developer and it dramatically improves the enduser’s experience in accessing HTML based representations of RDF resources.

## Why develop it (for free)
We believe that the dereferencing layer has to be independent from the SPARQL endpoint implementation so, during these last years, we preferred using Pubby to other software for publishing our data. Probably you don't know this but you already know Pubby, it is used to publish dbpedia data and a lot of other Linked Open Data out there, sadly it is pretty old and its development appears to have stopped, so we created LodView taking inspiration from some of the features in it we really appreciated. 
LodView shares Pubby philosophy; the configuration approach (an RDF file) and the basic technologies (we also use Apache Jena) are very similar, both interfaces even look somewhat alike, but we have made some important improvements according to the RDF 1.1 standard. We have added new features and changed the ones we didn’t like (such as the 303 redirection for HTML representation). 
While developing LodLive we realized that there was real need for a great interface to spread linked data principles even more effectively, so we spent time designing an interface for LodView that would be easy to use and beautiful to experience. LodView is free to use for all and we hope that the LOD community may appreciate and enjoy our brand new piece of work. 
LodView is an open source software, you may download it and use for it your own data publication but it is also a web service (http://lodview.it) useful to browse any resource published using a SPARQL endpoint or published according to the rules of the web of data (aka content negotiation and RDF).

## Demo
You can find some examples on http://lodview.it 

## Who is using it
list of known users https://github.com/dvcama/LodView/wiki/LodView-users

## Installing instruction
See the wiki page https://github.com/dvcama/LodView/wiki

## Some interesting features
##### Content negotiation and serialization

LodView allows you to publish RDF data offering a lot of different serializations. It handles content negotiation requests with or without 303 redirections: at the same IRI you can fully access different versions of the resource ( HTML, turtle, n-triples, json, json-ld, and many more) or you can set a suffix useful to redirect some requests to an HTML representation of the resource (eg. http://yourIRI.html). You can also override content negotiation features adding the  'output' parameter to the URL and specifying which serialization format you want to access. (eg. http://yourIRI?output=application/ld-json)

##### Internationalization and content language
Available languages: english, italian and slovak.  
LodView interface uses a very few words (labels and system messages) all managed with a language configuration file. It's able to use the client locale to manage not only the interface language but also the RDF literal values for a full i18n experience. You also have the possibility to override your default locale using 'locale' parameter.  (eg. http://yourIRI?locale=fr) 
We will appreciate any help in translating LodView, please commit your translation using GitHub or send us the translation file (https://github.com/dvcama/LodView/blob/master/src/main/resources/messages_en.properties)  
Special thanks to Jana Ivanová for the Slovak translation. 

##### Information about used properties
LodView provides info about every property found in the resource, just hover on the property to get a "info tooltip" that shows label and comment from the referring ontology (according to the locale of the client).  
See also: https://github.com/dvcama/LodView/wiki/how-to%3A-populate-info-tooltip

##### Object properties
In order to help humans understand the meaning of a resource, LodView shows the label of every object property it has found according to the locale of the client.

##### Blank nodes managing
We don't like blank nodes, but they are used sometimes so we  have managed them as an actual   part of the resource; they contain information that doesn’t belong to any other resource so LodView shows them in the main resource page nesting their values in sub-boxes which saves the user  further clicks  and contextualizes data more effectively.

##### Inverse relations managing 
Inverse relations are an interesting descriptive part of a resource. Very often inverse relations are as valuable and informative as direct relations, and sometimes there are too many of them  to be shown all in one go on the HTML page. LodView collects the inverse resources showing them in collapsed boxes; it also provides information about the used inverse properties and the total count of elements that share them without transferring all the data at once but using light on-demand Ajax calls instead.

##### Colors and user experience
Lodview is beautiful and colorful: you can let it randomize the colors of the interface or set your own colors or even bind classes to specific colors to make all "peoples" orange and all "organizations" green.

##### Resource widget
LodView provides (and will provide) various widgets for displaying multimedia contents, thesaurus hierarchies, external connected resources and geographical information.

##### Image widget
Easy access to the image referenced in the resource.

##### Linked LOD resources widget
To make the context of the viewed resourced even more understandable and to prove the power of the linked data cloud, all connected resources (eg. sameAs) are automatically shown with their title, an image or a map, and an abstract.
    
##### Map representation widget 
Geographical information is shown as points on a map (thanks to OSM project).
**TODO**: manage shape representation.

##### Hierarchy widget
**TODO**:  develop a widget which is able to represent hierarchical relations (eg. thesauri, family trees or taxonomies) 

##### Multimedia widget
Developing a multimedia player for viewing video or listening  to music is obviously an easy thing to implement, we hope that LodView will spread wide and that someone will ask us to do it.

##### SPARQL URL handler
Because we want the SPARQL endpoint URL to be easy to deduce from a resource's IRI, LodView manages calls to http://data.yourdomain.com/sparql redirecting clients to the SPARQL endpoint real URL or proxying it for an even easier access (proxy feature is still under development). This particular feature simplifies server configuration and supports client's discovery capabilities.

##### LodLive integration
**TODO**: We want to integrate LodLive in the interface to avoid LodLive having  to be open in a new page. The integration will provide an easy graph navigation model, very useful to better understand resource context and to move on to other resources without using standard hyperlinks.

* * *
contact information: info@lodlive.it
