<%@page session="true"%><%@taglib uri="http://www.springframework.org/tags" prefix="sp"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><html version="XHTML+RDFa 1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.w3.org/1999/xhtml http://www.w3.org/MarkUp/SCHEMA/xhtml-rdfa-2.xsd" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:xsd="http://www.w3.org/2001/XMLSchema#" xmlns:cc="http://creativecommons.org/ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:foaf="http://xmlns.com/foaf/0.1/">
<head data-color="${colorPair}" profile="http://www.w3.org/1999/xhtml/vocab">
<title>${results.getTitle()}&mdash;LodView</title>
<jsp:include page="../inc/header.jsp"></jsp:include>
<style>
body {
	white-space: nowrap;
	background: #fff;
	box-sizing: border-box;
	-moz-box-sizing: border-box;
	-webkit-box-sizing: border-box;
}

#pedigree-content {
	position: absolute;
	text-align: center;
	width: 100%;
	top: 50%;
	margin-top: -50px;
}

div.relContainer {
	position: absolute
}

div.relContainer div.pair {
  position: relative;
  top: -200px;
  left: 60px;
}

div.pair {
	display: inline-block;
	width: 220px;
	height: 100px;
	margin-right: 60px;
	display: inline-block;
}

div.pair div.wf-connector {
	float: left;
	width: 16px;
	height: 1px;
	border-top: 1px solid;
	margin-top: 49px;
}

div.pair div.hb {
	white-space: normal;
	float: right;
	width: 100px;
	height: 100px;
	border: 1px solid;
	-webkit-border-radius: 50px;
	-moz-border-radius: 50px;
	border-radius: 50px;
	overflow: hidden;
	-moz-border-radius: 50px;
	border-radius: 50px;
}

div.pair div.wf {
	white-space: normal;
	float: left;
	width: 100px;
	height: 100px;
	border: 1px solid;
	-webkit-border-radius: 50px;
	-moz-border-radius: 50px;
	border-radius: 50px;
	overflow: hidden;
}

div.pair div.mainIRI {
	background: #ddd;
}

div.pair div strong {
	display: block;
	margin-top: 40px;
	margin-right: 5px;
	margin-left: 5px;
	overflow: hidden;
	text-align: center;
	height: 60px;
	font-weight: normal;
	font-size: 10px;
	line-height: 10px;
}
</style>
</head>
<body class="">
	<div id="pedigree-content"></div>
	<script>
		$(function() {
			pedigree.init();
		});

		var pedigree = {
			"mainIRI" : "${param.IRI}",
			"data" : {},
			"c" : null,
			"init" : function() {
				$.ajax({
					url : "${conf.getPublicUrlPrefix()}pedigree/data",
					data : {
						"IRI" : pedigree.mainIRI,
						"sparql" : "${conf.getEndPointUrl()}",
						"prefix" : "${conf.getIRInamespace()}"
					},
					method : 'POST',
					success : function(data) {
						console.info(data);
						$('body').find('strong').text('success!');
						data.s[pedigree.mainIRI] = {
							//<c:forEach var="data" items="${results.getLiterals(param.IRI)}">
							//<c:forEach items='${data.getValue()}' var="p"> 
							"title" : "${p.getValue()}",
							"url" : '${p.getProperty().getPropertyUrl()}',
							"nsIri" : '${p.getProperty().getNsProperty()}'
						//</c:forEach>
						//</c:forEach>
						}
						pedigree.data = data;
						pedigree.process();
					},
					error : function(data) {
						$('body').find('strong').text('error!');
					},
					beforeSend : function() {
						$('body').append('<strong>loading...</strong>');
					}
				});
				this.c = $('#pedigree-content');
			},
			"process" : function() {
				if (!this.data[this.mainIRI]) {
					$('body').html('<strong>albero non generabile</strong>')
				} else {
					var bro = this.data[this.mainIRI].bro;
					var broTemp = [];
					if (!bro) {
						bro = [];
						broTemp.push(pedigree.mainIRI);
					} else {
						$.each(bro, function(k, v) {
							if (k == Math.round(bro.length / 2 - 1)) {
								broTemp.push(pedigree.mainIRI);
							}
							broTemp.push(v);
						});
					}

					bro = broTemp;

					$.each(bro, function(k, v) {
						var pair = pedigree.buildPair(v);
						pedigree.c.append(pair)
					});

					if (this.data[this.mainIRI].parents) {
						// find the father
						this.buildFather(this.data[this.mainIRI].parents[0], $('.mainIRI').closest('.pair'));
					}

				}
			},
			buildFather : function(person, son) {
				console.info('parent ' + person + ' ok ' + son.attr("data-iri"))
				var pair = this.buildPair(person);
				var cont = $('<div class="relContainer"></div>');
				cont.append(pair);
				son.prepend().append(cont);
				/* if (this.data[person].parents) {
					this.buildFather(this.data[person].parents[0],pair)
				} */
			},
			buildPair : function(person) {
				console.info('building ' + person)
				var pair = $('<div class="pair"></div>');
				var hb = $('<div class="hb" data-iri="'+person+'"><strong>' + pedigree.data.s[person].title + '</strong></div>');
				if (person == pedigree.mainIRI) {
					hb.addClass('mainIRI');
				}
				if (pedigree.data[person]) {
					//TODO: more then one spouse and string spouse
					var sp;
					if (pedigree.data[person].spouse && pedigree.data.s[pedigree.data[person].spouse[0]]) {
						sp = $('<div class="wf" data-iri="'+pedigree.data[person].spouse[0]+'"><strong>' + pedigree.data.s[pedigree.data[person].spouse[0]].title + '</strong></div>');
						pair.append(sp);
						pair.append('<div class="wf-connector"></div>')
					}
				}
				pair.append(hb);
				pair.find('[data-iri]').click(function() {
					document.location = '?IRI=' + $(this).attr("data-iri");
				});

				return pair;
			}
		}
	</script>
</body>
</html>