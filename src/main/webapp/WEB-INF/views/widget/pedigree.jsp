<%@page session="true"%><%@taglib uri="http://www.springframework.org/tags" prefix="sp"%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><html version="XHTML+RDFa 1.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.w3.org/1999/xhtml http://www.w3.org/MarkUp/SCHEMA/xhtml-rdfa-2.xsd" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#" xmlns:xsd="http://www.w3.org/2001/XMLSchema#" xmlns:cc="http://creativecommons.org/ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:foaf="http://xmlns.com/foaf/0.1/">
<head data-color="${colorPair}" profile="http://www.w3.org/1999/xhtml/vocab">
<title>${results.getTitle()}&mdash;LodView</title>
<%@include file="../inc/header.jsp"%>
<style>
body {
	white-space: nowrap;
	background: #fff;
	box-sizing: border-box;
	-moz-box-sizing: border-box;
	-webkit-box-sizing: border-box;
}

#pedigree-content {
	text-align: left;
}

div.pair {
	display: inline-block;
}

.hb, .wf {
	background-color: #e2e2e2;
	width: 100px;
	height: 100px;
	-webkit-border-radius: 50px;
	-moz-border-radius: 50px;
	border-radius: 50px;
	overflow: hidden;
	-moz-border-radius: 50px;
	border-radius: 50px;
	white-space: normal;
	color: #6d6d6d;
	font-weight: 700;
	font-size: 12px;
	line-height: 14px;
}

div.pair div.hb {
	float: right;
}

div.pair div.wf {
	float: left;
}

.e {
	width: 100px;
	height: 100px;
	padding: 0 25px;
	display: inline-block;
}

h3 {
	line-height: 25px;
	padding: 0 25px;
}

.mainIRI .hb, .mainIRI .wf {
	color: #fff;
	background: ${color1
}

;
}
.mainIRI {
	height: 150px;
	background-color: #fff;
}
div.pair img{
	position: relative;
	top: 50%;
	transform: translateY(-50%);
	width:100px;
	height:auto;
}
div.pair div strong {
	display: none;
	margin-right: 5px;
	margin-left: 5px;
	overflow: hidden;
	position: relative;
	top: 50%;
	transform: translateY(-50%);
	text-align: center;
}

.sel {
	background: #cdcdcd
}

#grampas {
	height: 150px;
	background: #878787;
}

#sons {
	height: 150px;
	background: #dadada;
}

#grandsons {
	padding-left: 75px;
	height: 150px;
	background: #ededed;
	height: 150px;
}

#bros {
	height: 150px;
	background: #c6c6c6;
}

#sibl, #mainr {
	display: inline-block;
}

#spouse {
	padding-left: 75px;
	text-align: right;
	background: #b2b2b2;
	display: inline-block;
	height: 150px;
	float: left;
	min-width: 150px;
}

#sibl {
	position: relative;
	top: -25px;
}

.mainIRI>div {
	margin-top: 25px
}

#fgp {
	min-width: 300px;
	float: left;
	height: 150px;
	background-color: #706f6f;
	text-align: right;
}

#fgp h3 {
	text-align: right;
}

#mgp {
	float: left;
}

#parents {
	height: 150px;
	padding-left: 150px;
	background: #9d9d9c;
}

#parents h3 {
	padding: 0;
	width:450px;
	margin-left:-75px;
	text-align: center;
}

#sons {
	height: 150px;
	padding-left: 75px;
	background: #dadada;
}

#sons h3, #grandsons h3 {
	padding: 0;
	width:450px;
	margin-left:-75px;
	text-align: center;
}
</style>
</head>
<body class="">
	<div id="pedigree-content">
		<div id="grampas">
			<div id="fgp">
				<h3>GRANDPARENTS</h3>
			</div>
			<div id="mgp">
				<h3>GRANDPARENTS</h3>
			</div>
		</div>
		<div id="parents">
			<h3>PARENTS</h3>
		</div>
		<div id="bros">
			<div id="mainr"></div>
			<div id="sibl">
				<h3>SIBLINGS</h3>
				<div class="pair"><div class="e"></div></div>
			</div>
		</div>
		<div id="sons">
			<h3>CHILDRENS</h3>
		</div>
		<div id="grandsons">
			<h3>GRANDCHILDRENS</h3>
		</div>
	</div>
	<script>
		$(function() {
			pedigree.init();
		});

		var pedigree = {
			"mainIRI" : "${param.IRI}",
			"data" : {},
			"c" : null,
			"mainOffset" : null,
			"init" : function() {
				this.c = $('#pedigree-content');
				this.sons = $('#pedigree-content').find('#sons');
				this.mainOffset = $('#pedigree-content').offset().top;
				if (localStorage && localStorage.getItem('${param.IRI}')) {
					pedigree.data = (JSON.parse(localStorage.getItem('${param.IRI}')));
					pedigree.process();
				} else {
					$.ajax({
						url : "${conf.getPublicUrlPrefix()}pedigree/data",
						data : {
							"IRI" : pedigree.mainIRI,
							"sparql" : "${conf.getEndPointUrl()}",
							"prefix" : "${conf.getIRInamespace()}"
						},
						cache : true,
						method : 'GET',
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
							if (localStorage) {
								localStorage.setItem('${param.IRI}', JSON.stringify(data));
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
				}
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
							if (k == 0) {
								broTemp.push(pedigree.mainIRI);
							}
							broTemp.push(v);
						});
					}

					bro = broTemp;
					$.each(bro, function(k, v) {
						var pair = pedigree.buildPair(v, k == 0, $('<div id="spouse"><h3>SPOUSE</h3></div>'));
						if (k == 0) {
							pedigree.c.find('#mainr').prepend(pair)
						} else {
							pedigree.c.find('#sibl').children('*:last').before(pair)
						}

					});
					var sons = this.data[this.mainIRI].sons;
					if (sons) {
						$.each(sons, function(k, v) {
							var pair = pedigree.buildPair(v, false);
							if(pair.text()){
								pedigree.c.children('#sons').append(pair);
							}
							if (pedigree.data[v]) {
								var grandsons = pedigree.data[v].sons;
								if (grandsons) {
									$.each(grandsons, function(ak, av) {
										var apair = pedigree.buildPair(av, false);
										if(apair.text()){
											pedigree.c.children('#grandsons').append(apair)
										}
									});
								}
							}
						});
					}

					var parents = this.data[this.mainIRI].parents;
					if (parents) {
						var swit = false;
						$.each(parents, function(k, v) {
							var pair = pedigree.buildPair(v, true);
							pedigree.c.children('#parents').append(pair);
							if (pedigree.data[v]) {
								var grampa = pedigree.data[v].parents;
								if (grampa) {
									$.each(grampa, function(ak, av) {
										var apair = pedigree.buildPair(av, true);
										if (swit) {
											pedigree.c.find('#fgp').append(apair)
										} else {
											pedigree.c.find('#mgp').append(apair)
										}
										swit = true;
									});
								}
							}
						});
					}

					this.betterPage();

				}
			},
			betterPage : function() {
				var left = $('.mainIRI').offset().left;
				var tot = $('#sons').find('.pair').length;
				$('#sons').find('h3').animate({
					width : ((tot+1)*150) + 'px'
				}, 'fast');
				$('#sons').animate({
					paddingLeft : (left + 75 -  (tot*75)) + 'px'
				},'fast',function(){
					tot = $('#grandsons').find('.pair').length;
					$('#grandsons').find('h3').animate({
						width : ((tot+1)*150) + 'px'
					}, 'fast');
					$('#grandsons').animate({
						paddingLeft : (left +  75 -(tot*75)) + 'px'
					},'fast',function(){
						tot = $('#parents').find('.pair').length;
						$('#parents').find('h3').animate({
							width : ((tot+1)*150) + 'px'
						}, 'fast');
						$('#parents').animate({
							paddingLeft : (left + 75 - (tot*75)) + 'px'
						},'fast');
					});
				});
			},
			buildPair : function(person, writeSpouse, wrapSpouse) {
				console.info('building ' + person)
				var pair = $('<div class="pair"></div>');
				if ($('[data-iri="' + person + '"]').length == 0) {
					var hb = $('<div class="e"><div class="hb" data-iri="' + person + '" data-family="' + (JSON.stringify(pedigree.data[person]) + '').replace(/"/g, '') + '"><strong>' + pedigree.data.s[person].title + '</strong>'+(pedigree.data.s[person].image?'<img src="'+ pedigree.data.s[person].image+'">':'')+'</div></div>');
					if (person == pedigree.mainIRI) {
						hb.addClass('mainIRI');
					}
					if (writeSpouse && pedigree.data[person]) {
						//TODO: more then one spouse and string spouse
						var sp;
						if (pedigree.data[person].spouse && pedigree.data.s[pedigree.data[person].spouse[0]]) {
							$.each(pedigree.data[person].spouse, function(k, v) {
								sp = $('<div class="e"><div  class="wf" data-iri="' + v + '" data-family="' + (JSON.stringify(pedigree.data[v]) + '').replace(/"/g, '') + '"><strong>' + pedigree.data.s[v].title + '</strong>'+(pedigree.data.s[v].image?'<img src="'+ pedigree.data.s[v].image+'">':'')+'</div></div>');
								if (wrapSpouse) {
									wrapSpouse.append(sp);
									pair.prepend(wrapSpouse);
								} else {
									pair.prepend(sp);
								}

							});
							// simple pair, with no child
							//pair.append('<div class="wf-connector"></div>')
						} else if (wrapSpouse) {
							pair.prepend(wrapSpouse);
						}
					}
					pair.append(hb);
					pair.find('[data-iri]').click(function() {
						document.location = '?IRI=' + $(this).attr("data-iri");
					});
				}
				return pair;
			},
			hasParent : function(iri) {
				var yes = false;
				$.each(pedigree.data, function(k, v) {
					if (k == iri && v.parents) {
						yes = true;
						return false;
					}
				});
				return yes;
			},
			isParent : function(iri) {
				var yes = false;
				$.each(pedigree.data, function(k, v) {
					if (v.parents && $.inArray(iri, v.parents) != -1) {
						yes = true;
						return false;
					}
				});
				return yes;
			},
			isBrother : function(iri) {
				var yes = false;
				$.each(pedigree.data, function(k, v) {
					if (v.bro && $.inArray(iri, v.bro) != -1) {
						yes = true;
						return false;
					}
				});
				return yes;
			}
		}

		Array.prototype.max = function() {
			return Math.max.apply(null, this);
		};

		Array.prototype.min = function() {
			return Math.min.apply(null, this);
		};
	</script>
</body>
</html>