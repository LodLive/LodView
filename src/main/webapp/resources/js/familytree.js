var familytree = {
	"mainIRI" : null,
	"data" : {},
	"c" : null,
	"mainOffset" : null,
	"init" : function(mainIri) {
		this.c = $('#familytree-content');
		this.mainIri = mainIri;
		this.sons = $('#familytree-content').find('#sons');
		this.mainOffset = $('#familytree-content').offset().top;
		if (localStorage && localStorage.getItem(familytree.mainIRI)) {
			familytree.data = (JSON.parse(localStorage.getItem(familytree.mainIRI)));
			familytree.process();
		} else {
			$.ajax({
				url : conf.PublicUrlPrefix+"familytree/data",
				data : {
					"IRI" : familytree.mainIRI,
					"sparql" : conf.EndPointUrl,
					"prefix" : conf.IRInamespace
				},
				cache : true,
				method : 'GET',
				success : function(data) {
					console.info(data);
					$('body').find('strong').text('success!');
					data.s[familytree.mainIRI] = {
						// <c:forEach var="data"
						// items="${results.getLiterals(param.IRI)}">
						// <c:forEach items='${data.getValue()}' var="p">
//						"title" : "${p.getValue()}",
//						"url" : '${p.getProperty().getPropertyUrl()}',
//						"nsIri" : '${p.getProperty().getNsProperty()}'
					// </c:forEach>
					// </c:forEach>
					}
					if (localStorage) {
						localStorage.setItem(familytree.mainIRI, JSON.stringify(data));
					}
					familytree.data = data;
					familytree.process();
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
				broTemp.push(familytree.mainIRI);
			} else {
				$.each(bro, function(k, v) {
					if (k == 0) {
						broTemp.push(familytree.mainIRI);
					}
					broTemp.push(v);
				});
			}

			bro = broTemp;
			$.each(bro, function(k, v) {
				var pair = familytree.buildPair(v, k == 0, $('<div id="spouse"><h3>SPOUSE</h3></div>'));
				if (k == 0) {
					familytree.c.find('#mainr').prepend(pair)
				} else {
					familytree.c.find('#sibl').children('*:last').before(pair)
				}

			});
			var sons = this.data[this.mainIRI].sons;
			if (sons) {
				$.each(sons, function(k, v) {
					var pair = familytree.buildPair(v, false);
					if (pair.text()) {
						familytree.c.children('#sons').append(pair);
					}
					if (familytree.data[v]) {
						var grandsons = familytree.data[v].sons;
						if (grandsons) {
							$.each(grandsons, function(ak, av) {
								var apair = familytree.buildPair(av, false);
								if (apair.text()) {
									familytree.c.children('#grandsons').append(apair)
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
					var pair = familytree.buildPair(v, true);
					familytree.c.children('#parents').append(pair);
					if (familytree.data[v]) {
						var grampa = familytree.data[v].parents;
						if (grampa) {
							$.each(grampa, function(ak, av) {
								var apair = familytree.buildPair(av, true);
								if (swit) {
									familytree.c.find('#fgp').append(apair)
								} else {
									familytree.c.find('#mgp').append(apair)
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
			width : ((tot + 1) * 150) + 'px'
		}, 'fast');
		$('#sons').animate({
			paddingLeft : (left + 75 - (tot * 75)) + 'px'
		}, 'fast', function() {
			tot = $('#grandsons').find('.pair').length;
			$('#grandsons').find('h3').animate({
				width : ((tot + 1) * 150) + 'px'
			}, 'fast');
			$('#grandsons').animate({
				paddingLeft : (left + 75 - (tot * 75)) + 'px'
			}, 'fast', function() {
				tot = $('#parents').find('.pair').length;
				$('#parents').find('h3').animate({
					width : ((tot + 1) * 150) + 'px'
				}, 'fast');
				$('#parents').animate({
					paddingLeft : (left + 75 - (tot * 75)) + 'px'
				}, 'fast');
			});
		});
	},
	buildPair : function(person, writeSpouse, wrapSpouse) {
		console.info('building ' + person)
		var pair = $('<div class="pair"></div>');
		if ($('[data-iri="' + person + '"]').length == 0) {
			var hb = $('<div class="e"><div class="hb" data-iri="' + person + '" data-family="' + (JSON.stringify(familytree.data[person]) + '').replace(/"/g, '') + '"><strong>' + familytree.data.s[person].title + '</strong>' + (familytree.data.s[person].image ? '<img src="' + familytree.data.s[person].image + '">' : '') + '</div></div>');
			if (person == familytree.mainIRI) {
				hb.addClass('mainIRI');
			}
			if (writeSpouse && familytree.data[person]) {
				// TODO: more then one spouse and string spouse
				var sp;
				if (familytree.data[person].spouse && familytree.data.s[familytree.data[person].spouse[0]]) {
					$.each(familytree.data[person].spouse, function(k, v) {
						sp = $('<div class="e"><div  class="wf" data-iri="' + v + '" data-family="' + (JSON.stringify(familytree.data[v]) + '').replace(/"/g, '') + '"><strong>' + familytree.data.s[v].title + '</strong>' + (familytree.data.s[v].image ? '<img src="' + familytree.data.s[v].image + '">' : '') + '</div></div>');
						if (wrapSpouse) {
							wrapSpouse.append(sp);
							pair.prepend(wrapSpouse);
						} else {
							pair.prepend(sp);
						}

					});
					// simple pair, with no child
					// pair.append('<div class="wf-connector"></div>')
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
		$.each(familytree.data, function(k, v) {
			if (k == iri && v.parents) {
				yes = true;
				return false;
			}
		});
		return yes;
	},
	isParent : function(iri) {
		var yes = false;
		$.each(familytree.data, function(k, v) {
			if (v.parents && $.inArray(iri, v.parents) != -1) {
				yes = true;
				return false;
			}
		});
		return yes;
	},
	isBrother : function(iri) {
		var yes = false;
		$.each(familytree.data, function(k, v) {
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