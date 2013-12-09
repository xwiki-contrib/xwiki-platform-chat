/**
 * Candy Plugin for WebRTC Video Conferencing
 *
 * Author: Ludovic Dubost, XWiki SAS <ludovic@xwiki.com>
 * This program is distributed under the terms of the MIT license.
 * Please see the LICENSE.CANDYWEBRTC file for details.
 *
 * Based on Candy Jingle plugin by
 * Author: Michael Weibel <michael.weibel@gmail.com>
 * Copyright: 2011-2012 Michael Weibel <michael.weibel@gmail.com>
 * This program is distributed under the terms of the MIT license.
 * Please see the LICENSE.CANDYJINGLE file for details.
 */
var CandyShop = (function(self) { return self; }(CandyShop || {}));

CandyShop.WebRTC = (function(self, Candy, $) {
		this.connection = null;
		this.peerconnection  = null;
                this.turnDone = true;
		this.pcConfig;
		this.pcConstraints = {"optional": [{"DtlsSrtpKeyAgreement": true}]};
                this.localStream;
                this.remoteStream;
                this.streamReady = false;
		this.media_constraints = {
mandatory: {
'OfferToReceiveAudio': true,
'OfferToReceiveVideo': true
}
// MozDontOfferDataChannel: true when this is firefox
};

this._displayError = function(reason, e) {
console.log("Error " + reason);
console.log(e);
console.error(reason, e);
Candy.View.Pane.Chat.Modal.show($.i18n._('webrtcReason-' + reason), true);
};

this._onCalling = function(jid) {
	Candy.View.Pane.Chat.Modal.hide();
	$('#chat-tabs li[data-roomjid="' + jid + '"] .label .webrtc').removeClass('webrtc-calling');
	$('#webrtc-videos').css('display', '');			
};

this._onTerminate = function(jid) {
	_endCall();

	Candy.View.Pane.Room.getPane(Candy.View.getCurrent().roomJid, '.message-pane').removeClass('webrtc');
	$('#chat-tabs li[data-roomjid="' + jid + '"] .label .webrtc').remove();
	$('#webrtc-videos').remove();
};

this._applyTranslations = function() {
	Candy.View.Translation.en.labelCallConfirm = '"%s" wants a video call with you, accept?';
	Candy.View.Translation.en.labelYes = 'Yes';
	Candy.View.Translation.en.labelNo = 'No';
	Candy.View.Translation.en.calling = 'Establishing video call...';
	Candy.View.Translation.en.hangup = 'Hangup';
	Candy.View.Translation.en.callTerminated = 'Recipient terminated the call.';
	Candy.View.Translation.en['webrtcReason-service-unavailable'] = 'Recipient does not support video.';
	Candy.View.Translation.en['webrtcReason-resource-constraint'] = 'Recipient is already in a call.';
};

this._applyEventHooks = function() {
	Candy.View.Event.Room.onHide = function(args) {
		$('#webrtc-videos').toggle();
	};
};

this._Template = {
callConfirm: '<strong>{{_label}}</strong>'
		     + '<p><button class="button" id="webrtc-call-confirm-yes">{{_labelYes}}</button> '
		     + '<button class="button" id="webrtc-call-confirm-no">{{_labelNo}}</button></p>'
};

this._onRinging = function(jid, cb) {
	$('#webrtc-videos').append('<video width="230" height="150" id="webrtc-remoteView" autoplay="autoplay"></video>');

	this._startCall(jid, Candy.Core.getRoom("wiki@chat.xwiki").getUser().getJid(), true);

	Candy.View.Pane.Room.getPane(Candy.View.getCurrent().roomJid, '.message-pane').addClass('webrtc');
	$('#chat-tabs li[data-roomjid="' + jid + '"] .label')
		.append('<a class="webrtc webrtc-calling" title="' + $.i18n._('hangup') + '"></a>');
	$('#chat-tabs li[data-roomjid="' + jid + '"] .label a.webrtc')
		.click(function() {
				_onTerminate(jid);
				});

	$('#webrtc-videos').addClass('active');
};

this._sendMessage = function(jid, message) {
	console.log("Sending message to " + jid + ": " + message);
	// we use the jabber channel to send the message 
	Candy.Core.Action.Jabber.Room.Message(jid, message, "webrtc");
        Candy.Core.getConnection().flush();
};

this.addIceCandidate = function(iceCandidate) {
    if (!this.streamReady) {
        var that = this;
        console.log("Delaying adding iceCandidate");
        window.setTimeout(function() {
                   that.addIceCandidate.bind(that)(iceCandidate);
                }, 1000);
    } else {
        console.log("Real adding iceCandidate");
        this.peerconnection.addIceCandidate(iceCandidate);
    }
}

this._receiveMessage = function(fromjid, message) {

	var that = this; 
	if (message.sdp && message.sdp.type==="offer") {
		console.log("Offer handling");
		if (!this.peerconnection) {
			this._startCall(fromjid, Candy.Core.getUser().getJid(), false, message);
		}
	} else if (message.sdp && message.sdp.type==="answer") {
		console.log("Answer handling");
		this.peerconnection.setRemoteDescription(new RTCSessionDescription(message.sdp), function () {
				console.log("Answer handling success");
				}, function(e) { _displayError('setRemoteDesc failed', e) });
	} else {
		console.log("Candidate handling");
		// we received a candidate, let's add it
                this.addIceCandidate(new RTCIceCandidate(message.candidate)); 
	}
};

this.mergeConstraints = function(cons1, cons2) {
	var merged = cons1;
	for (var name in cons2.mandatory) {
		merged.mandatory[name] = cons2.mandatory[name];
	}
	merged.optional.concat(cons2.optional);
	return merged;
};

this._sendSDP = function(localSDP) {
	console.log("Sending SDP to " + this.peerconnection.peerjid);
	var that = this;
	this.peerconnection.setLocalDescription(localSDP, function () {
                        console.log("setLocalDesc success");
			}, function(e) {
                             _displayError('setLocalDesc failed', e) 
                        });
	this._sendMessage(that.peerconnection.peerjid, JSON.stringify({
					'sdp': this.peerconnection.localDescription
					}));
};

this._startCall = function(jid, myjid, initiator, message) {
	console.log("starting call");
        jQuery("#webrtcvideo").show();
	this.peerconnection = new RTCPeerConnection(this.pcConfig, this.pcConstraints);
	this.peerconnection.peerjid = jid;
	var that = this;
	// send any ice candidates to the other peer
	this.peerconnection.onicecandidate = function (evt) {
                console.log("on ICE Candidate");
		if (evt.candidate) {
			window.setTimeout(function() {
                                        console.log("Sending ICE Candidate");
					_sendMessage(that.peerconnection.peerjid, JSON.stringify({
							'candidate': evt.candidate
							}));
					}, 2000);
		}
	};
	this.peerconnection.onrenegotiationneeded = function(evt) {
		console.log("Renegociation is needed");
		console.log(evt);
	}
	this.peerconnection.onaddstream = function(evt) {
		console.log("Got remote stream");
		console.log(evt);
		document.getElementById("remoteVideo").src = window.URL.createObjectURL(evt.stream);  
		that.remoteStream = evt.stream;
                console.log("Got remote stream end");
        };
        this.peerconnection.onsignalingstatechange = function(evt) {
                console.log("New signaling state");
                console.log("Gatthering " + that.peerconnection.iceGatheringState + " Sig: " + that.peerconnection.signalingState + " ICE: " + that.peerconnection.iceConnectionState);
        };
        this.peerconnection.oniceconnectionstatechange = function(evt) {
                console.log("New ice connection state");
                console.log("Gatthering " + that.peerconnection.iceGatheringState + " Sig: " + that.peerconnection.signalingState + " ICE: " + that.peerconnection.iceConnectionState);
        };

        // get a local stream, show it in a self-view and add it to be sent
        getUserMedia({
                     'audio': true,
                     'video': true
                    }, function (stream) {
                         document.getElementById("localVideo").src = window.URL.createObjectURL(stream);
                         that.localStream = stream;
                         console.log("Adding local stream");
                         that.peerconnection.addStream(stream);
 
                         if (initiator) { 
                           console.log("Create offer");
                           that.peerconnection.createOffer(that._sendSDP.bind(that), function(s) { _displayError('createOffer failed', e) }, this.media_constraints);
                           that.streamReady = true;
                         } else {
                           console.log("Sending answer");
                           that.peerconnection.setRemoteDescription(new RTCSessionDescription(message.sdp), function () {
                           console.log("setRemoteDesc success");
                           that.peerconnection.createAnswer(that._sendSDP.bind(that), function(e) { _displayError('createAnswer failed', e) },  this.media_constraints);
                             }, function(e) { _displayError('setRemoteDesc failed', e) });
                           that.streamReady = true;
                         }
                       }, function(e) { _displayError("Error getting user media ", e) });
                };
                
        this._endCall = function(jid) {
                   console.log("ending call");
                };

        this.onMessage = function(msg) {
               var msg = $(msg),
		   fromJid = msg.attr('from'),
	  	   type = msg.attr('type'),
	  	   toJid = msg.attr('to');
                   var content =  msg.children("body").text();
                   if (type=="webrtc") {
                     // this is a webrtc message
                     console.log("WebRTC message from " + fromJid + ": " + msg.children("body").text());
                     var sdp = JSON.parse(msg.children("body").text());
                     this._receiveMessage(Candy.Util.unescapeJid(fromJid), sdp);
                   }
                   return true;
        };

this.requestTurn = function() {
 var turnUrl = 'https://computeengineondemand.appspot.com/turn?username=74651302&key=4080218913';

  console.log("Checking turn");
  // Skipping TURN Http request for Firefox version <=22.
  // Firefox does not support TURN for version <=22.
  if (webrtcDetectedBrowser === 'firefox' && webrtcDetectedVersion <=22) {
    this.turnDone = true;
    return;
  }

  for (var i = 0, len = this.pcConfig.iceServers.length; i < len; i++) {
    if (this.pcConfig.iceServers[i].url.substr(0, 5) === 'turn:') {
      this.turnDone = true;
      return;
    }
  }

  var currentDomain = document.domain;
  if (currentDomain.search('localhost') === -1 &&
      currentDomain.search('webrtc.xwiki.com') === -1) {
    // Not authorized domain. Try with default STUN instead.
    this.turnDone = true;
    return;
  }

  // No TURN server. Get one from computeengineondemand.appspot.com.
  xmlhttp = new XMLHttpRequest();
  xmlhttp.onreadystatechange = this.onTurnResult.bind(this);
  xmlhttp.open('GET', turnUrl, true);
  xmlhttp.send();
}

this.onTurnResult = function() {
  console.log("turn result");
  if (xmlhttp.readyState !== 4)
    return;

  if (xmlhttp.status === 200) {
    var turnServer = JSON.parse(xmlhttp.responseText);
    for (i = 0; i < turnServer.uris.length; i++) {
      // Create a turnUri using the polyfill (adapter.js).
      var iceServer = createIceServer(turnServer.uris[i],
                                      turnServer.username,
                                      turnServer.password);
      if (iceServer !== null) {
        this.pcConfig.iceServers.push(iceServer);
      }
    }
  } else {
    console.log('Request for TURN server failed.');
  }
  // If TURN request failed, continue the call with default STUN.
  this.turnDone = true;
}

	this.init = function(pcConfig) {
                this.pcConfig = pcConfig;
		this._applyTranslations();
		this._applyEventHooks();
                this.requestTurn();

		// Candy.Core.Event.addObserver(Candy.Core.Event.KEYS.CHAT, _ChatObserver);
		connection = Candy.Core.getConnection();

                var that = this;
		Candy.View.Event.Roster.onContextMenu = function(args) {
		    return {
		        'webrtc': {
		            requiredPermission: function(user, me) {
		                return me.getNick() !== user.getNick()  
							&& !Candy.Core.getUser().isInPrivacyList('ignore', user.getJid());
		            },
		            'class': 'webrtc',
		            'label': 'Video call',
		            'callback': function(e, roomJid, user) {
						var rosterElem = $('#user-' + Candy.Util.jidToId(roomJid) + '-' + Candy.Util.jidToId(user.getJid()));
						Candy.View.Pane.PrivateRoom.open(rosterElem.attr('data-jid'), rosterElem.attr('data-nick'), true, false);

                                                console.log("launching call");

						that._onRinging(user.getJid(), function() {
							Candy.View.Pane.Chat.Modal.show($.i18n._('calling'), false, true);

							var supported = _call.initSession(user.getJid(), 'audioVideo', 'both', function() {
								_onCalling(user.getJid());
							});
							if (!supported) {
								_displayError('service-unavailable');
								_onTerminate(user.getJid());
							}	
						});
		            }
		        }
		    }
		};
	};

	return this;
}(CandyShop.WebRTC || {}, Candy, jQuery));
