var totImage = 0;
var familytree = {
	"mainIRI" : null,
	"data" : {},
	"c" : null,
	"mainOffset" : null,
	"template" : null,
	"imgStatus" : "foto",
	"init" : function(setmainIRI, name, image) {
		this.c = $('#familytree-content');
		this.mainIRI = setmainIRI;
		this.sons = familytree.c.find('#sons');
		this.mainOffset = familytree.c.offset().top;
		$('.header').html('<div>' + name + '&rsquo;s family</div>');
		this.aSwitch();
		familytree.load(image);
		document.location = '#family:' + this.mainIRI;
	},
	"aSwitch" : function() {
		var familyswitch = 'foto';
		if (localStorage && localStorage.getItem('family-switch')) {
			familyswitch = localStorage.getItem('family-switch');
			this.imgStatus = familyswitch;
		}

		var imgSwitch = $('<div class="family-switch sp ' + this.imgStatus + '" ></div>');
		$('.header').find('div').append(imgSwitch);

		imgSwitch.click(function() {
			if ($(this).hasClass("foto")) {
				$(this).removeClass("foto");
				$(this).addClass("testo");
				familytree.imgStatus = "testo";
				familytree.c.find('img').fadeOut('fast');
				if (localStorage) {
					localStorage.setItem('family-switch', 'testo');
				}
			} else {
				$(this).removeClass("testo");
				$(this).addClass("foto");
				familytree.imgStatus = "foto";
				familytree.c.find('img').fadeIn('fast');
				if (localStorage) {
					localStorage.setItem('family-switch', 'foto');
				}
			}
		});
	},
	"load" : function(image) {
		totImage = 0;
		$('#familyload').children('div').empty();
		if (image) {
			image.css({
				display : 'block'
			});
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
			familytree.c.css({
				display : 'block',
				opacity : 0
			});
			familytree.c.find('strong').each(function() {
				familytree.betterTitle($(this));
			});
			familytree.c.fadeTo('fast', '1');
		});
	},
	betterImage : function(image, docount) {
		image.load(function() {
			if (!image.attr("style")) {
				var w = $(this).width();
				var h = $(this).height();

				try {
					w = $(this).naturalWidth();
					h = $(this).naturalHeight();
				} catch (e) {
				}

				if (h > w) {
					h = Math.round(h / w * 100);
					w = 100;
				} else if (w > h) {
					w = Math.round(w / h * 100);
					h = 100;
				} else {
					w = 100;
					h = 100;
				}
				$(this).css({
					width : w ? w : 'auto',
					height : h ? h : 'auto',
					marginLeft : -(Math.abs(100 - w) / 2) + "px",
					marginTop : -(Math.min(Math.abs(100 - h) / 2, 0)) + "px"
				});
				$(this).fadeTo('fast', '1');
				if (docount) {
					totImage--;
				}
			}
			if (familytree.imgStatus == 'testo') {
				$(this).hide();
			}
			if (docount && totImage == 0) {
				familytree.open();
			}
		});
		image.error(function() {
			$(this).attr("src", conf.StaticResourceURL + 'familytree/avatar-neutro.png');
			$(this).load();
		})
	},
	betterTitle : function(title) {
		var h = title.height();
		title.css({
			marginTop : (50 - (h / 2)) + "px"
		});
	},
	betterPage : function() {
		totImage = familytree.c.find('img').length;

		familytree.c.find('img').each(function() {
			$(this).attr('src', $(this).attr("data-src"));
			familytree.betterImage($(this), true);
		});

		setTimeout(function() {
			// familytree.c.find('img').load();
		}, 2000);

		var left = $('.mainIRI').offset().left;

		var totG = $('#fgp').find('.e').length;
		var totP = $('#parents').find('.e').length;
		var totGP = $('#parents').find('.e').length;
		var totS = $('#spouse').find('.e').length;
		var totSO = $('#sons').find('.e').length;
		var totGS = $('#grandsons').find('.e').length;
		var totSI = $('#sibl').find('.e').length;
		if (totSI == 0) {
			$('#sibl').append('<div class="pair"><div class="e"></div></div>')
		}
		if (totG == 0) {
			$('#fgp').append('<div class="pair"><div class="e"></div></div>')
		}
		if ($('#mgp').find('.e').length == 0) {
			$('#mgp').append('<div class="pair"><div class="e"></div></div>')
		}
		

		var center = totG * 150;

		if (totP > 2) {
			center = center < ((150 * (totP + 1) / 2) + 75) ? (150 * (totP + 1) / 2) + 75 : center;
		}
		if (totS > 1) {
			center = center < (150 * totS + 75) ? (150 * totS + 75) : center;
		}
		if (totG * 150 < center) {
			$('#fgp').css({
				paddingLeft : center - (totG * 150 > 300 ? totG * 150 : 300)
			});
		}
		if (center < 300) {
			center = 300
		}
		$('#parents').find('h3').css({
			width : (totP * 150) + 'px',
			padding : 0,
			textAlign : 'center'
		});
		$('#parents').css({
			paddingLeft : center - ((totP + 1) * 150 / 2) + 75
		});
		if (center > (150 * totS)) {
			$('#spouse').css({
				width : center - 75
			});
		}
		if (totSO > 0 && center > (totSO * 150 / 2)) {
			$('#sons').css({
				paddingLeft : center - (totSO * 150 / 2)
			});
		} else if (center > (totSO * 150 / 2)) {
			$('#sons').css({
				paddingLeft : center - 75
			});
		}
		if (totGS > 0 && center > (totGS * 150 / 2)) {
			$('#grandsons').css({
				paddingLeft : center - (totGS * 150 / 2)
			});
		} else if (center > (totGS * 150 / 2)) {
			$('#grandsons').css({
				paddingLeft : center - 75
			});
		}

		$('#sons').find('h3').css({
			width : 150,
			paddingLeft : center - parseInt($('#sons').css('padding-left').replace(/px/, ''), 10) - 75,
			textAlign : 'center'
		});
		$('#grandsons ').find('h3').css({
			width : 150,
			paddingLeft : center - parseInt($('#grandsons').css('padding-left').replace(/px/, ''), 10) - 75,
			textAlign : 'center'
		});

		var max = Math.max(totG, totP, totP, totGP, totS, totSO, totGS, totSI + 2);
		$('.familyarea').each(function() {
			$(this).css({
				width : Math.max(max * 150 - parseInt($(this).css('padding-left').replace(/px/, ''), 10), window.innerWidth)
			});
		});
		familytree.c.wrap('<div class="dragcontainer" style="overflow:hidden"></div>')
		familytree.c.draggable();
	},
	buildPair : function(person, writeSpouse, wrapSpouse) {
		var pair = $('<div class="pair"></div>');
		var pkey = encodeURI(person.replace(/[^\d\w]/g, '').toLowerCase());
		if ($('[data-test=' + pkey + ']').length == 0) {
			var hb = null;
			if (!person.indexOf("http") == 0) {
				hb = $('<div class="e"><div class="hb" data-test="' + pkey + '" ><strong>' + person + '</strong><img src="' + conf.StaticResourceURL + 'familytree/avatar-neutro.png"></div></div>');
				pair.append(hb);
			} else {
				hb = $('<div class="e"><div class="hb" data-iri="' + person + '" data-test="' + pkey + '" data-family="' + (JSON.stringify(familytree.data[person]) + '').replace(/"/g, '') + '"><strong>' + (familytree.data.s[person].title ? familytree.data.s[person].title : person.replace(/.+\/([^/]+)/g, '$1')) + '</strong>' + (familytree.data.s[person].image ? '<img data-src="' + familytree.data.s[person].image + '">' : '<img src="' + conf.StaticResourceURL + 'familytree/avatar-neutro.png">') + '</div></div>');
				if (person == familytree.mainIRI) {
					hb.addClass('mainIRI');
				}
				if (writeSpouse) {
					var sp;
					if (familytree.data[person] && familytree.data[person].spouse) {
						$.each(familytree.data[person].spouse, function(k, v) {
							var vkey = encodeURI(v.replace(/[^\d\w]/g, '').toLowerCase());
							if ($('[data-test=' + vkey + ']').length == 0) {
								if (v.indexOf("http") == -1) {
									sp = $('<div class="e"><div  data-test="' + vkey + '" class="wf" ><strong>' + v + '</strong><img src="' + conf.StaticResourceURL + 'familytree/avatar-neutro.png"></div></div>');
									if (wrapSpouse) {
										wrapSpouse.append(sp);
										pair.prepend(wrapSpouse);
									} else {
										pair.prepend(sp);
									}
								} else if (familytree.data.s[v]) {
									sp = $('<div class="e"><div data-iri="' + v + '" class="wf" data-test="' + vkey + '" data-family="' + (JSON.stringify(familytree.data[v]) + '').replace(/"/g, '') + '"><strong>' + (familytree.data.s[v].title ? familytree.data.s[v].title : v.replace(/.+\/([^/]+)/g, '$1')) + '</strong>' + (familytree.data.s[v].image ? '<img data-src="' + familytree.data.s[v].image + '">' : '<img src="' + conf.StaticResourceURL + 'familytree/avatar-neutro.png">') + '</div></div>');
									if (wrapSpouse) {
										wrapSpouse.append(sp);
										pair.prepend(wrapSpouse);
									} else {
										pair.prepend(sp);
									}
								}
							}
						});
					} else if (wrapSpouse) {
						wrapSpouse.append('<div class="e"></div>');
						pair.prepend(wrapSpouse);
					}
				}
				pair.append(hb);
				pair.find('[data-iri]').css({
					cursor : 'pointer'
				});
				pair.find('[data-iri]').click(function() {
					var iri = $(this);
					familytree.c.fadeOut('fast', function() {
						$('#familytree-container').remove();
						var familytreeLayer = familytree.template.clone();
						var ele = $('.familytree-container');
						ele.prepend(familytreeLayer);
						lodview.zoomHelper(familytreeLayer, ele, true)
						familytreeLayer.fadeIn(300, function() {
							familytree.init(iri.attr("data-iri"), iri.text(), iri.find('img:first').clone());
						});
					});
				});

				pair.find('[data-test]').on('mouseenter', function() {
					if (familytree.imgStatus == 'testo') {
						$(this).find('img').fadeIn('fast');
					} else {
						$(this).find('img').fadeOut();
					}
				});

				pair.find('[data-test]').on('mouseleave', function() {
					if (familytree.imgStatus == 'foto') {
						$(this).find('img').fadeIn('fast');
					} else {
						$(this).find('img').fadeOut();
					}
				});
			}
		} else {
			return "";
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