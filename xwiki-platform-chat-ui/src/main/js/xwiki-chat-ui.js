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
 * XWiki chat UI module.
 */
var XWikiChatUI = (function() {

	/**
	 * An array containing all the currently opened chat tab elements.
	 */
	var chatTabs = [];

	/**
	 * The HTML template for a chat tab.
	 */
	var chatTabTemplate = new Template(
			"<div class='chat_tab'>"
					+ "  <div id='#{chatTabId}_chat_messages' class='chat_messages not_displayed'>"
					+ "    <div id='#{chatTabId}_chat_messages_area' class='chat_messages_area'></div>"
					+ "    <div class='chat_messages_input'><input type='text' id='#{chatTabId}_chat_message_input' style='width: 100%'/></div>"
					+ "  </div>"
					+ "  <div id='#{chatTabId}_chat_tab_title' class='chat_tab_title'><span onclick='XWikiChatUI.toggleMessagesWindow(\"#{chatTabId}\")'>#{chatTabTitle}</span></div>"
					+ "</div>");

	/**
	 * This function returns an input event handler to be linked to the input text area of a chat tab. This handler will
	 * send the content of the text area using the specified send function when the ENTER key is pressed.
	 * 
	 */
	function getChatMessageInputEventHandler(id, sendFunction) {
		return function(event) {
			var key = event.keyCode || event.which;
			if (key == 13) {
				var input = event.srcElement;
				var text = input.value;
				sendFunction(text);
				input.clear();
			}
		};
	}

	/**
	 * Public functions.
	 */
	var api = {

		/**
		 * Check if a chat tab exists.
		 * 
		 * @param id
		 *            The id of the chat tab.
		 */
		chatTabExists : function(id) {
			return id in chatTabs;
		},

		/**
		 * Create a chat tab.
		 * 
		 * @param id
		 *            The id of the chat tab.
		 * @param title
		 *            The title of the chat tab.
		 * @param sendFunction
		 *            The function that should be used to send messages typed in this chat tab.
		 */
		addChatTab : function(id, title, sendFunction) {
			var chatToolbar = $("chat_toolbar");

			if (chatToolbar != null) {
				$("chat_toolbar").insert(chatTabTemplate.evaluate({
					chatTabId : id,
					chatTabTitle : title
				}));

				$(id + "_chat_message_input").observe('keypress', getChatMessageInputEventHandler(id, sendFunction));

				chatTabs[id] = $(id + "_chat_messages");
			} else {
				console.log("Chat toolbar not found");
			}

			return id;
		},

		/**
		 * Show/hide the message window for a given chat tab.
		 * 
		 * @param id
		 *            The id of the chat tab.
		 */
		toggleMessagesWindow : function(id) {
			var chatTabTitle = $(id + "_chat_tab_title");
			var messageWindow = $(id + "_chat_messages");

			if (chatTabTitle != null && messageWindow != null) {
				for (k in chatTabs) {
					if (chatTabs.hasOwnProperty(k) && k != id) {
						chatTabs[k].addClassName('not_displayed');
					}
				}
				messageWindow.toggleClassName('not_displayed');

				/* Remove the chat tab title hightlight when the message window is displayed. */
				if (!messageWindow.hasClassName("not_displayed")) {
					chatTabTitle.removeClassName("chat_tab_title_highlight");
				}
			} else {
				console.log("Messages window not found.");
			}
		},

		/**
		 * Write a message to the message window associated to a chat tab.
		 * 
		 * @param id
		 *            The id of the chat tab.
		 * @param from
		 *            A string representing the sender.
		 * @param msg
		 *            The message.
		 * @param fromStyle
		 *            The CSS style to be used for the sender (can be null).
		 * @param msgStyle
		 *            The CSS style to be used for the message (can be null).
		 */
		writeMessage : function(id, from, msg, fromStyle, msgStyle) {
			var messageWindow = $(id + "_chat_messages");
			var chatTabTitle = $(id + "_chat_tab_title");
			var messagesArea = $(id + "_chat_messages_area");

			if (messageWindow != null && chatTabTitle != null && messagesArea != null) {
				var html = "<p>";
				if (fromStyle != null) {
					html += "<span style='" + fromStyle + "'>";
				}
				html += from;
				if (fromStyle != null) {
					html += "</span>";
				}

				html += " : ";

				if (msgStyle != null) {
					html += "<span style='" + msgStyle + "'>";
				}
				html += msg;
				if (msgStyle != null) {
					html += "</span>";
				}

				messagesArea.insert(html);

				/* If the message goes to a chat tab with a hidded message window, then highlight the chat tab title. */
				if (messageWindow.hasClassName("not_displayed")) {
					chatTabTitle.addClassName("chat_tab_title_highlight");
				}
			} else {
				console.log("Messages area not found. Cannot write message '" + msg + "'");
			}
		}
	};

	return api;
})();