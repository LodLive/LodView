var totImage = 0;
var familytree = {
	"mainIRI" : null,
	"data" : {},
	"c" : null,
	"mainOffset" : null,
	"template" : null,
	"init" : function(setmainIRI, image) {
		this.c = $('#familytree-content');
		this.mainIRI = setmainIRI;
		this.sons = $('#familytree-content').find('#sons');
		this.mainOffset = $('#familytree-content').offset().top;
		$('.header').html('<div>' + $('h1').text() + '&rsquo;s family</div>');
		familytree.load(image);
	},
	"load" : function(image) {
		totImage = 0;
		$('#familyload').children('div').empty();
		if (image) {
			$('#familyload').children('div').append(image);
			this.betterImage(image);
			image.load();
		}
		$('#familyload').fadeIn('fast', function() {
			if (localStorage && localStorage.getItem(familytree.mainIRI)) {
				familytree.data = (JSON.parse(localStorage.getItem(familytree.mainIRI)));
				familytree.process();
			} else {
				$.ajax({
					url : conf.PublicUrlPrefix + "familytree/data",
					data : {
						"IRI" : familytree.mainIRI,
						"sparql" : conf.EndPointUrl,
						"prefix" : conf.IRInamespace
					},
					cache : true,
					method : 'GET',
					success : function(data) {
						console.info(data);
						// $('body').find('strong').text('success!');

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

					}
				});
			}
		});
	},
	"process" : function() {
		if (!this.data[this.mainIRI]) {
			alert('<strong>albero non generabile</strong>')
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
				var pair = familytree.buildPair(v, k == 0, $('<div id="spouse" class="half"><h3>SPOUSE</h3></div>'));
				if (k == 0) {
					familytree.c.find('#mainr').prepend(pair)
				} else {
					familytree.c.find('#sibl').append(pair)
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
	open : function() {
		$('#familyload').fadeOut('fast', function() {
			$('#familytree-content').fadeIn('fast');
		});
	},
	betterImage : function(image, docount) {
		image.load(function() {
			if (docount) {
				totImage--;
			}

			var w = $(this).width();
			var h = $(this).height();
			$(this).css({
				height : 100,
				width : 'auto'
			});
			w = $(this).width();
			h = $(this).height();
			if (w < 100) {
				$(this).css({
					width : 100,
					height : 'auto'
				});
			}
			if (h < 100) {
				$(this).css({
					height : 100,
					width : 'auto'
				});
			}
			w = $(this).width();
			h = $(this).height();
			$(this).css({
				marginLeft : -(Math.abs(100 - w) / 2) + "px",
				marginTop : -(Math.abs(100 - h) / 2) + "px"
			});
			if (docount && totImage < 2) {
				familytree.open();
			}
		});
		image.error(function() {
			// todo: error image
			$(this).remove();
			if (docount) {
				totImage--;
			}
		})
	},
	betterPage : function() {
		totImage = $('#familytree-content').find('img').length;

		$('#familytree-content').find('img').each(function() {
			familytree.betterImage($(this), true);
		});

		setTimeout(function() {
			$('#familytree-content').find('img').load();
			totImage = 0;
		}, 3000);

		var left = $('.mainIRI').offset().left;
		var tot = $('#sons').find('.pair').length;
		$('#sons').find('h3').css({
			width : ((tot + 1) * 150) + 'px'
		});
		$('#sons').css({
			paddingLeft : (left + 75 - (tot * 75)) + 'px'
		});
		tot = $('#sibl').find('.pair').length;
		if (tot == 0) {
			$('#sibl').append('<div class="pair"></div>');
		}
		tot = $('#spouse').find('.e').length;
		if (tot == 0) {
			$('#spouse').append('<div class="e"></div>');
		}
		tot = $('#grandsons').find('.pair').length;
		$('#grandsons').find('h3').css({
			width : ((tot + 1) * 150) + 'px'
		});
		$('#grandsons').css({
			paddingLeft : (left + 75 - (tot * 75)) + 'px'
		});
		tot = $('#parents').find('.pair').length;
		$('#parents').find('h3').css({
			width : ((tot + 1) * 150) + 'px'
		});
		$('#parents').animate({
			paddingLeft : (left + 75 - (tot * 75)) + 'px'
		});
	},
	buildPair : function(person, writeSpouse, wrapSpouse) {
		console.info('building ' + person)
		var pair = $('<div class="pair"></div>');
		if ($('[data-iri="' + person + '"]').length == 0) {
			var hb = null;

			if (!person.indexOf("http") == 0) {
				hb = $('<div class="e"><div class="hb" data-iri="' + person + '" ><strong>' + person + '</strong></div></div>');
				pair.append(hb);
			} else {
				hb = $('<div class="e"><div class="hb" data-iri="' + person + '" data-family="' + (JSON.stringify(familytree.data[person]) + '').replace(/"/g, '') + '">' + (familytree.data.s[person].image ? '<img src="' + familytree.data.s[person].image + '">' : '') + '<strong>' + familytree.data.s[person].title + '</strong></div></div>');

				if (person == familytree.mainIRI) {
					hb.addClass('mainIRI');
				}
				if (writeSpouse) {
					console.info("write spouse")
					// TODO: more then one spouse and string spouse
					var sp;
					if (familytree.data[person] && familytree.data[person].spouse) {
						$.each(familytree.data[person].spouse, function(k, v) {
							console.info("has spouse " + v)
							if (v.indexOf("http") == -1) {
								sp = $('<div class="e"><div  data-iri="' + v + '" class="wf" ><strong>' + v + '</strong></div></div>');
								if (wrapSpouse) {
									wrapSpouse.append(sp);
									pair.prepend(wrapSpouse);
								} else {
									pair.prepend(sp);
								}
							} else if (familytree.data.s[v]) {
								sp = $('<div class="e"><div  class="wf" data-iri="' + v + '" data-family="' + (JSON.stringify(familytree.data[v]) + '').replace(/"/g, '') + '">' + (familytree.data.s[v].image ? '<img src="' + familytree.data.s[v].image + '">' : '') + '<strong>' + familytree.data.s[v].title + '</strong></div></div>');
								if (wrapSpouse) {
									wrapSpouse.append(sp);
									pair.prepend(wrapSpouse);
								} else {
									pair.prepend(sp);
								}
							}
						});
						// simple pair, with no child
						// pair.append('<div class="wf-connector"></div>')
					} else if (wrapSpouse) {
						pair.prepend(wrapSpouse);
					}
				}
				pair.append(hb);
				pair.find('[data-iri]').css({
					cursor : 'pointer'
				});
				pair.find('[data-iri]').click(function() {
					var iri = $(this);	
					$('#familytree-content').fadeOut('fast', function() {
						var familytreeLayer = familytree.template.clone();
						var ele = $('.familytree-container');
						ele.prepend(familytreeLayer);
						lodview.zoomHelper(familytreeLayer, ele, true)
						/* add loading */
						familytreeLayer.fadeIn(300, function() {
							familytree.init(iri.attr("data-iri"), iri.find('img:first').clone());
						});
					});
				});
			}
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