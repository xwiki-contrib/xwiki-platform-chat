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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.vysper.mina.TCPEndpoint;
import org.apache.vysper.storage.OpenStorageProviderRegistry;
import org.apache.vysper.storage.StorageProviderRegistry;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.MUCModule;
import org.apache.vysper.xmpp.modules.extension.xep0049_privatedata.PrivateDataModule;
import org.apache.vysper.xmpp.modules.extension.xep0054_vcardtemp.VcardTempModule;
import org.apache.vysper.xmpp.modules.extension.xep0092_software_version.SoftwareVersionModule;
import org.apache.vysper.xmpp.modules.extension.xep0119_xmppping.XmppPingModule;
import org.apache.vysper.xmpp.modules.extension.xep0202_entity_time.EntityTimeModule;
import org.apache.vysper.xmpp.modules.roster.persistence.MemoryRosterManager;
import org.apache.vysper.xmpp.server.XMPPServer;

/**
 * This context listener is used to start a Vysper server when the web application is deployed. The server is made
 * available to other servlets via a context attribute.
 * 
 * @version $Id$
 */
public class XMPPServerContextListener implements ServletContextListener
{
    /**
     * The domain to be used to the server.
     */
    private static final String SERVER_DOMAIN = "xwiki";

    /**
     * The XMPP server.
     */
    private XMPPServer server;

    /**
     * {@inheritDoc}
     */
    public void contextInitialized(ServletContextEvent sce)
    {
        try {
            StorageProviderRegistry providerRegistry = new OpenStorageProviderRegistry();
            providerRegistry.add(new MemoryRosterManager());
            providerRegistry.add(new NullUserAuthorization());

            server = new XMPPServer(SERVER_DOMAIN);
            /* Also open a TCP endpoint on port 5222 so that desktop clients can connect as well. */
            server.addEndpoint(new TCPEndpoint());
            server.setStorageProviderRegistry(providerRegistry);

            server.setTLSCertificateInfo(sce.getServletContext().getResourceAsStream("/WEB-INF/bogus_mina_tls.cert"),
                "boguspw");

            server.start();

            server.addModule(new SoftwareVersionModule());
            server.addModule(new EntityTimeModule());
            server.addModule(new VcardTempModule());
            server.addModule(new XmppPingModule());
            server.addModule(new PrivateDataModule());
            server.addModule(new MUCModule());

            sce.getServletContext().setAttribute(Constants.XMPP_SERVER_ATTRIBUTE, server);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void contextDestroyed(ServletContextEvent sce)
    {
        server.stop();
    }

}
