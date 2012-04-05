/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

/**
 * XWiki chat module.
 */
var XWikiChat = (function() {
	/**
	 * The BOSH endpoint to use for XMPP communication. This is the URI associated to the XMPPServerBoshServlet in the
	 * web.xml of the web application.
	 */
	var BOSH_ENDPOINT = XWiki.contextPath + "/bosh";

	/**
	 * The variable storing the current user name.
	 */
	var userName;

	/**
	 * This is a list of chat rooms to be joined automatically at startup.
	 * 
	 * TODO: This is temporary. Actually only the wiki chat room is always joined. Space and page chat rooms will be
	 * joined depending on which page and space the user is actually in.
	 */
	var rooms = [ {
		id : "wiki@chat.xwiki",
		label : "Wiki"
	}, {
		id : "space@chat.xwiki",
		label : "Space"
	}, {
		id : "page@chat.xwiki",
		label : "Page"
	} ];

	/**
	 * Log a message to console.
	 */
	function log(msg) {
		console.log(msg);
	}

	/**
	 * This function returns a "send message" to a given chat room, on a given connection function.
	 * 
	 * @returns A function that takes a message as argument, and sends it to the correct destination.
	 */
	function getGroupChatSendFunction(connection, room) {
		return function(message) {
			connection.muc.groupchat(room, message);
		};
	}

	/**
	 * This function returns a "send message" to a given user on a given connection function.
	 * 
	 * @returns A function that takes a message as argument, and sends it to the correct destination.
	 */
	function getChatSendFunction(connection, user) {
		return function(message) {
			var msg = $xmppmsg({
				from : connection.jid,
				to : user,
				type : 'chat'
			}).c('body', {
				xmlns : Strophe.NS.CLIENT
			}).t(message);
			connection.send(msg);
		};
	}

	/**
	 * The onMessage callback. This is called by Strophe when a message is received. It is responsible of parsing the
	 * XMPP message and to display it properly using UI functions.
	 */
	function onMessage(msg) {
		var from = msg.getAttribute('from');
		var type = msg.getAttribute('type');
		var elems = msg.getElementsByTagName('body');

		if (elems.length > 0) {
			var body = elems[0];
			var text = Strophe.getText(body);
			var chatTabId = Strophe.getBareJidFromJid(from);
			var fromLabel;

			if (type == "chat") {
				fromLabel = Strophe.getNodeFromJid(from);

				/* Create the chat tab if it doesn't exist */
				if (!XWikiChatUI.chatTabExists(chatTabId)) {
					XWikiChatUI.addChatTab(chatTabId, fromLabel, getChatSendFunction(connection, from));
				}

			} else if (type == "groupchat") {
				fromLabel = Strophe.getResourceFromJid(from);
			}

			XWikiChatUI.writeMessage(chatTabId, fromLabel, text, "font-weight: bold", null);
		}
		return true;
	}

	/**
	 * Connection callback.
	 */
	function onConnect(status) {
		if (status == Strophe.Status.CONNECTING) {
			log("Connecting...");
		} else if (status == Strophe.Status.CONNFAIL) {
			log("Connection failed.");
		} else if (status == Strophe.Status.DISCONNECTING) {
			log("Disconnecting...");
		} else if (status == Strophe.Status.DISCONNECTED) {
			log("Disconnected.");
		} else if (status == Strophe.Status.CONNECTED) {
			log("Connected.");

			/**
			 * Use the username coming from the connection.
			 */
			userName = Strophe.getNodeFromJid(connection.jid);

			/**
			 * Register the onMessage callback for linking message reception to the UI
			 */
			connection.addHandler(onMessage, null, "message", null, null, null);

			/**
			 * Automatically join the specified chatrooms and create the related UI.
			 */
			for ( var i = 0; i < rooms.length; i++) {
				console.log(rooms[i].id);
				connection.muc.join(rooms[i].id, userName, null, null, null, null);

				/*
				 * TODO: Maybe this can be handled in the onMessage callback, when a message is received a chat tab is
				 * opened automatically if it didn't exist previously.
				 */
				XWikiChatUI.addChatTab(rooms[i].id, rooms[i].label, getGroupChatSendFunction(connection, rooms[i].id));
			}
		}
	}

	/**
	 * Public functions.
	 */
	var api = {

		/**
		 * Initialize Strophe and connect to the XMPP server.
		 */
		connect : function() {
			connection = new Strophe.Connection(BOSH_ENDPOINT);
			/* Use a dummy username and password because the connection will be authenticated via cookies. */
			connection.connect("client@xwiki", "nopassword", onConnect);
		},

		/**
		 * Leave all the chatrooms and disconnect from the XMPP server.
		 */
		disconnect : function() {
			for ( var i = 0; i < rooms.length; i++) {
				connection.muc.leave(rooms[i].id, userName, null, null);
			}
			/*
			 * This is important, otherwise the stanza with the leave operation might not be sent. This would result in
			 * a lot of stale users populating the multi-user chat room.
			 */
			connection.flush();
			connection.disconnect('bye');
		}
	};

	return api;
})();

/**
 * Call connect/disconnect functions when the page il loaded/unloaded
 * 
 * TODO: Maybe the connection object might be shared across different pages, so that we reuse the same object during all
 * the navigation. But I don't know if this is possible.
 */
document.observe("xwiki:dom:loaded", XWikiChat.connect);
window.onbeforeunload = XWikiChat.disconnect;