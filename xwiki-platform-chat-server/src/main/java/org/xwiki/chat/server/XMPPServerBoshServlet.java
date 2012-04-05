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
package org.xwiki.chat.server;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.vysper.xmpp.extension.xep0124.BoshDecoder;
import org.apache.vysper.xmpp.extension.xep0124.BoshResponse;
import org.apache.vysper.xmpp.extension.xep0124.BoshServlet;
import org.apache.vysper.xmpp.server.Endpoint;
import org.apache.vysper.xmpp.server.XMPPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xwiki.chat.server.vysper.XWikiBoshHandler;
import org.xwiki.component.manager.ComponentManager;

/**
 * This is a subclass of the BoshServlet that binds a BOSH endpoint to the XMPP server that has been initialized and
 * that is made available through servlet context attributes.
 * 
 * @version $Id$
 */
public class XMPPServerBoshServlet extends BoshServlet implements Endpoint
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -3960093311691945133L;

    /**
     * Taken from super class.
     */
    private static final String SERVER_IDENTIFICATION = "Vysper/0.5";

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(XMPPServerBoshServlet.class);

    /**
     * The BOSH handler to be used for handling cookie authentication.
     */
    private XWikiBoshHandler boshHandler;

    @Override
    public void init(ServletConfig sc) throws ServletException
    {
        /*
         * Generate a random global cookie authentication password that will be used to identify users that have been
         * authenticated using cookies.
         */
        String cookieAuthenticationPassword =
            (String) sc.getServletContext().getAttribute(Constants.XMPP_COOKIE_AUTHENTICATION_PASSWORD_ATTRIBUTE);
        if (cookieAuthenticationPassword == null) {
            throw new ServletException("No XMPP cookie authentication password available.");
        }

        /* Retrieve the embedded XMPP server and add the BOSH endpoint to it. */
        XMPPServer server = (XMPPServer) sc.getServletContext().getAttribute(Constants.XMPP_SERVER_ATTRIBUTE);
        if (server == null) {
            throw new ServletException("No XMPP server available.");
        }

        server.addEndpoint(this);
        setServerRuntimeContext(server.getServerRuntimeContext());
        setAccessControlAllowOrigin(Arrays.asList("*"));

        /* Retrieve the component manager to be used for retrieving XWiki execution contexts. */
        ComponentManager componentManager =
            (ComponentManager) sc.getServletContext().getAttribute(ComponentManager.class.getName());
        if (componentManager == null) {
            throw new ServletException("XWiki component manager not found in context.");
        }

        /* Create the BOSH handler for intercepting "auth" stanzas and enable cookie authentication. */
        try {
            boshHandler = new XWikiBoshHandler(cookieAuthenticationPassword, componentManager);
        } catch (Exception e) {
            throw new ServletException("Unable to initialize XWiki BOSH handler.");
        }
        boshHandler.setServerRuntimeContext(server.getServerRuntimeContext());

        super.init(sc);
    }

    /*
     * This has been copied from the super class. This was needed because the boshHandler variable used in the
     * superclass method is a private variable that is not overridable.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        BoshResponse boshResponse = (BoshResponse) req.getAttribute("response");
        if (boshResponse != null) {
            // if the continuation is resumed or expired
            writeResponse(resp, boshResponse);
            return;
        }

        BoshDecoder decoder = new BoshDecoder(boshHandler, req);
        try {
            decoder.decode();
        } catch (SAXException e) {
            logger.error("Exception thrown while decoding XML", e);
        }
    }

    /**
     * Copied from the superclass, in order to make doPost work.
     * 
     * @param resp response.
     * @param respData response data.
     * @throws IOException if something goes wrong.
     */
    private void writeResponse(HttpServletResponse resp, BoshResponse respData) throws IOException
    {
        resp.addDateHeader("Date", System.currentTimeMillis());
        resp.addHeader("Server", SERVER_IDENTIFICATION);
        resp.setContentType(respData.getContentType());
        resp.setContentLength(respData.getContent().length);
        if (getAccessControlAllowOrigin() != null) {
            resp.addHeader("Access-Control-Allow-Origin", createAccessControlAllowOrigin());
        }
        resp.getOutputStream().write(respData.getContent());
        resp.flushBuffer();
    }

    /**
     * Copied from superclass in order to make doPost work.
     * 
     * @return access control string.
     */
    private String createAccessControlAllowOrigin()
    {
        StringBuffer crossDomain = new StringBuffer();
        boolean first = true;
        for (String domain : getAccessControlAllowOrigin()) {
            if (!first) {
                crossDomain.append(',');
            }
            crossDomain.append(domain);
            first = false;
        }
        return crossDomain.toString();
    }

    /**
     * {@inheritDoc}
     */
    public void start() throws IOException
    {
        /* Don't do anything. This is managed by the servlet container. */
    }

    /**
     * {@inheritDoc}
     */
    public void stop()
    {
        /* Don't do anything. This is managed by the servlet container. */
    }
}
