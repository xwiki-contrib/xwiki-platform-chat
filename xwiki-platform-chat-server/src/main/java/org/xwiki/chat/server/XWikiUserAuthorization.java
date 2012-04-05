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

import java.security.Principal;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.authorization.UserAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.chat.server.stub.XWikiEngineContextStub;
import org.xwiki.chat.server.stub.XWikiServletRequestStub;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.container.Container;
import org.xwiki.container.servlet.ServletContainerInitializer;
import org.xwiki.context.Execution;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiEngineContext;
import com.xpn.xwiki.web.XWikiResponse;
import com.xpn.xwiki.web.XWikiServletResponseStub;

/**
 * This class authorizes everyone to connect to the server.
 * 
 * @version $Id$
 */
public class XWikiUserAuthorization implements UserAuthorization
{
    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(XWikiUserAuthorization.class);

    /**
     * Constructor.
     * 
     * @param componentManager The component manager to be used for initializing XWiki contexts.
     */
    public XWikiUserAuthorization(ComponentManager componentManager)
    {
        com.xpn.xwiki.web.Utils.setComponentManager(componentManager);
    }

    /**
     * {@inheritDoc}
     */
    public boolean verifyCredentials(Entity jid, String passwordCleartext, Object credentials)
    {
        logger.info("User with JID {} is logging in ({})", jid.toString(), credentials);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean verifyCredentials(String username, String passwordCleartext, Object credentials)
    {
        /* Just to shut up CheckStyle. */
        final String splitCharacter = "@";

        /* Isolate the username if it's provided as a JID. */
        String xwikiUserName = username;
        if (username.contains(splitCharacter)) {
            xwikiUserName = username.split(splitCharacter)[0];
        }

        XWikiContext xwikiContext = null;

        try {
            xwikiContext = initializeXWikiContext();

            if (xwikiContext != null) {
                /* Try to authenticate the username using XWiki authentication service. */
                Principal principal =
                    xwikiContext.getWiki().getAuthService()
                        .authenticate(xwikiUserName, passwordCleartext, xwikiContext);
                if (principal != null) {
                    logger.info("User {} logged in", username);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Unable to initialize XWiki context for authentication", e);
        } finally {
            /* Cleanup */
            if (xwikiContext != null) {
                cleanupComponents();
            }
        }

        logger.info("Invalid credentials for user {}", username);

        return false;
    }

    /**
     * Initializes the XWiki context. This code mostly duplicates the logic found in
     * {@link org.xwiki.wysiwyg.server.filter.XWikiContextInitializationFilter#initializeXWikiContext(ServletRequest, ServletResponse)}
     * 
     * @return An initialized XWiki context.
     * @throws Exception If an error in initialization occurs.
     */
    protected XWikiContext initializeXWikiContext() throws Exception
    {
        // Not all request types specify an action (e.g. GWT-RPC) so we default to the empty string.
        String action = "";
        XWikiEngineContext xwikiEngine = new XWikiEngineContextStub();
        XWikiServletRequestStub xwikiRequest = new XWikiServletRequestStub();
        XWikiResponse xwikiResponse = new XWikiServletResponseStub();

        // Create the XWiki context.
        XWikiContext context = Utils.prepareContext(action, xwikiRequest, xwikiResponse, xwikiEngine);

        // Initialize the Container component which is the new way of transporting the Context in the new component
        // architecture. Further initialization might require the Container component.
        initializeContainerComponent(context);

        // Initialize the XWiki database. XWiki#getXWiki(XWikiContext) calls XWikiContext.setWiki(XWiki).
        XWiki xwiki = XWiki.getXWiki(context);

        // Initialize the URL factory.
        context.setURLFactory(xwiki.getURLFactoryService().createURLFactory(context.getMode(), context));

        // Prepare the localized resources, according to the selected language.
        xwiki.prepareResources(context);

        return context;
    }

    /**
     * Initializes container components. This code mostly duplicates the logic found in
     * {@link org.xwiki.wysiwyg.server.filter.XWikiContextInitializationFilter#initializeContainerComponent(XWikiContext)}
     * 
     * @param context the XWiki context
     * @throws Exception if the container component initialization fails
     */
    protected void initializeContainerComponent(XWikiContext context) throws Exception
    {
        // Initialize the Container fields (request, response, session). Note that this is a bridge between the old core
        // and the component architecture. In the new component architecture we use ThreadLocal to transport the
        // request, response and session to components which require them.
        ServletContainerInitializer containerInitializer = Utils.getComponent(ServletContainerInitializer.class);

        containerInitializer.initializeRequest(context.getRequest().getHttpServletRequest(), context);
        containerInitializer.initializeResponse(context.getResponse().getHttpServletResponse());
        containerInitializer.initializeSession(context.getRequest().getHttpServletRequest());
    }

    /**
     * We must ensure we clean the ThreadLocal variables located in the Container and Execution components as otherwise
     * we will have a potential memory leak.
     */
    protected void cleanupComponents()
    {
        Container container = Utils.getComponent(Container.class);
        container.removeRequest();
        container.removeResponse();
        container.removeSession();

        Execution execution = Utils.getComponent(Execution.class);
        execution.removeContext();
    }

}
