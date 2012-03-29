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

import org.apache.vysper.xmpp.extension.xep0124.BoshServlet;
import org.apache.vysper.xmpp.server.Endpoint;
import org.apache.vysper.xmpp.server.XMPPServer;

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

    @Override
    public void init(ServletConfig sc) throws ServletException
    {
        XMPPServer server = (XMPPServer) sc.getServletContext().getAttribute(Constants.XMPP_SERVER_ATTRIBUTE);
        if (server == null) {
            throw new ServletException("No XMPP server available");
        }

        server.addEndpoint(this);
        setServerRuntimeContext(server.getServerRuntimeContext());
        setAccessControlAllowOrigin(Arrays.asList("*"));
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
