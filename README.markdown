Introduction
============

This project contains an integration of an XMPP based chat in XWiki (see http://www.youtube.com/embed/0Gwtpu3iVwo)

Installation
------------

* Build the toplevel project using `mvn install`

* At the end of the build you will have a ready-to-use Jetty distribution built in `xwiki-chat-platform-distribution-jetty-hsqldb/target`.

* Unpack the zip file and start the XWiki Chat prototype with `start_xwiki.sh`

* Open a browser and go to `http://localhost:8080/xwiki`

Enjoy.

Limitations
-----------

* Currently the web application works only with Jetty

* The UI works fine in Chrome (and possibly other WebKit based browsers)
