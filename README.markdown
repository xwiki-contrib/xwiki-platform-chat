Introduction
============

This project contains an integration of an XMPP based chat in XWiki (see http://www.youtube.com/embed/0Gwtpu3iVwo)

Installation
------------

* Build the toplevel project using `mvn install`

* Download the XWiki 3.5 Jetty distribution from [http://enterprise.xwiki.org/xwiki/bin/view/Main/Download](http://enterprise.xwiki.org/xwiki/bin/view/Main/Download) and unpack it.

* Copy the `xwiki-chat-platform-server/target/xwiki-chat-platform-server.jar` in the `webapps/xwiki/WEB-INF/lib` of your XWiki 3.5 Jetty installation.

* Add the following blocks to the `webapps/xwiki/WEB-INF/web.xml` in the appropriate places:

	`<listener>`<br/>
	`	<listener-class>org.xwiki.chat.server.XMPPServerContextListener</listener-class>`<br/>
	`</listener>`<br/>
	`<filter-mapping>`<br/>
	`	<filter-name>XWikiContextInitializationFilter</filter-name>`<br/>
	`	<servlet-name>BoshServlet</servlet-name>`<br/>
	`	<dispatcher>REQUEST</dispatcher>`<br/>
	`	<dispatcher>INCLUDE</dispatcher>`<br/>
	`	<dispatcher>FORWARD</dispatcher>`<br/>
	`</filter-mapping>`<br/>
	`<servlet>`<br/>
	`	<servlet-name>BoshServlet</servlet-name>`<br/>
	`	<servlet-class>org.xwiki.chat.server.XMPPServerBoshServlet</servlet-class>`<br/>
	`</servlet>`<br/>
	`<servlet-mapping>`<br/>
	`<servlet-name>BoshServlet</servlet-name>`<br/>
	`	<url-pattern>/bosh</url-pattern>`<br/>
	`</servlet-mapping>`<br/>

* Run XWiki with `start_xwiki.sh` and import the XAR `xwiki-chat-platform-ui/target/xwiki-chat-platform-ui.xar`. To go fast you can only import the page `XWiki.XWikiChatSkin`

* Use the skin `XWiki.XWikiChatSkin` to display a page with the chat activated (use the `?skin=XWiki.XWikiChatSkin` query string in the UI)

Enjoy.


