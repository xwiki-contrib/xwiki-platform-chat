Introduction
============

This project contains an integration of an XMPP based chat in XWiki

<iframe width="560" height="315" src="http://www.youtube.com/embed/0Gwtpu3iVwo" frameborder="0" allowfullscreen></iframe>

Installation
------------

* Build the toplevel project using `mvn install`

* Download the XWiki 3.5 Jetty distribution from [http://enterprise.xwiki.org/xwiki/bin/view/Main/Download](http://enterprise.xwiki.org/xwiki/bin/view/Main/Download) and unpack it.

* Copy the `xwiki-chat-platform-server/target/xwiki-chat-platform-server.jar` in the `webapps/xwiki/WEB-INF/lib` of your XWiki 3.5 Jetty installation.

* Add the following lines to the `webapps/xwiki/WEB-INF/web.xml`:

	<listener>
		<listener-class>org.xwiki.chat.server.XMPPServerContextListener</listener-class>`
	</listener>
	<servlet>
		<servlet-name>BoshServlet</servlet-name>
		<servlet-class>org.xwiki.chat.server.XMPPServerBoshServlet</servlet-class>
	</servlet>
	<servlet-mapping>
		<servlet-name>BoshServlet</servlet-name>
		<url-pattern>/bosh</url-pattern>
	</servlet-mapping>

* Run XWiki with `start_xwiki.sh` and import the XAR `xwiki-chat-platform-ui/target/xwiki-chat-platform-ui.xar`. To go fast you can only import the page `XWiki.XWikiChatSkin`

* Use the skin `XWiki.XWikiChatSkin` to display a page with the chat activated (use the `?skin=XWiki.XWikiChatSkin` query string in the UI)

Enjoy.


