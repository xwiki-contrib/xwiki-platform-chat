Introduction
============

This module contains the XMPP chat server. It uses [Vysper](http://mina.apache.org/vysper/)

Authentication
--------------

Authentication is done using the XWiki authentication service. When a BOSH connection is established, the system tries to authenticate it by using the cookies in the request (if they are present). This means that BOSH connections initiated from an XWiki page by a logged in user, will be authenticated and the initiating entity will be the XWiki user name.

For standard connections (or BOSH connections without XWiki authentication cookies) the provided username and password will be used to authenticate the user, always using the XWiki authentication service.

There is a problem with user names because apparently XMPP clients (like Pidgin) sends usernames always in lowercase. This means that if the XWiki user contains upper case letters, authentication will fail.
