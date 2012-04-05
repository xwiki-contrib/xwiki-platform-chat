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
package org.xwiki.chat.server.vysper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.axis.encoding.Base64;
import org.apache.vysper.xml.fragment.Attribute;
import org.apache.vysper.xml.fragment.XMLElement;
import org.apache.vysper.xml.fragment.XMLFragment;
import org.apache.vysper.xml.fragment.XMLSemanticError;
import org.apache.vysper.xml.fragment.XMLText;
import org.apache.vysper.xmpp.extension.xep0124.BoshHandler;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.apache.vysper.xmpp.stanza.StanzaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.reference.DocumentReference;

import com.xpn.xwiki.XWikiContext;

/**
 * The BOSH handler. This handler processes incoming stanzas from a BOSH conection. We use it in order to intercept
 * "auth" stanzas and try to authenticate the user by using the cookies in the request.
 * 
 * @version $Id$
 */
public class XWikiBoshHandler extends BoshHandler
{
    /**
     * The name of the "auth" stanza.
     */
    private static final String AUTH_STANZA_NAME = "auth";

    /**
     * Logger.
     */
    private final Logger logger = LoggerFactory.getLogger(XWikiBoshHandler.class);

    /**
     * The execution object to be used for getting the xwiki context.
     */
    private Execution execution;

    /**
     * This is the password that will be used for all the users that are authenticated using cookies. The same password
     * will be used in {@link XWikiUserAuthorization#verifyCredentials(String, String, Object)} in order to recognize
     * which users have already been authenticated using the cookie mechanism. This password is dynamically generated at
     * startup in {@link XMPPServerContextListener#contextInitialized(javax.servlet.ServletContextEvent)}
     */
    private String cookieAuthenticationPassword;

    /**
     * Constructor.
     * 
     * @param cookieAuthenticationPassword the password that will be used in credentials data for identifying users that
     *            have been identified by using cookies.
     * @param componentManager the component manager to be used in order to retrieve the Execution object.
     * @throws Exception if the component manager cannot lookup the Execution object.
     */
    public XWikiBoshHandler(String cookieAuthenticationPassword, ComponentManager componentManager) throws Exception
    {
        this.cookieAuthenticationPassword = cookieAuthenticationPassword;
        execution = componentManager.lookup(Execution.class);
    }

    /**
     * This function rewrites credentials data in an "auth" stanza if the user has been authenticated using cookies. It
     * basically replace the data sent by the client with the authenticate XWiki user name, the global cookie
     * authentication password so that the {@link XWikiUserAuthorization} module will receive this data when asked to
     * authenticate the user.
     * 
     * @param xwikiUser the authenticated XWiki user name.
     * @param original the original "auth" stanza to be rewritten.
     * @return the modified "auth" stanza.
     */
    private Stanza addXWikiUserToAuthStanza(String xwikiUser, Stanza original)
    {
        StanzaBuilder stanzaBuilder =
            new StanzaBuilder(original.getName(), original.getNamespaceURI(), original.getNamespacePrefix());

        List<Attribute> originalAttributes = original.getAttributes();
        for (Attribute originalAttribute : originalAttributes) {
            stanzaBuilder.addAttribute(originalAttribute);
        }

        for (XMLElement innerElement : original.getInnerElements()) {
            if (AUTH_STANZA_NAME.equals(innerElement.getName())) {
                List<XMLFragment> innerFragments = new ArrayList<XMLFragment>();

                /*
                 * Write the new credentials payload. It consists of a base64 encoded string with three components
                 * separated by 0s. The first component is unused, the second is the user name and the third is the
                 * password.
                 */
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    bos.write(0);
                    bos.write(xwikiUser.getBytes());
                    bos.write(0);
                    /* Use the global cookie authentication password to mark this user as being cookie-authenticated. */
                    bos.write(cookieAuthenticationPassword.getBytes());
                    bos.write(0);
                } catch (IOException e) {
                    /* Should not happen */
                }
                innerFragments.add(new XMLText(Base64.encode(bos.toByteArray())));

                XMLElement newAuthElement =
                    new XMLElement(innerElement.getNamespaceURI(), innerElement.getName(),
                        innerElement.getNamespacePrefix(), innerElement.getAttributes(), innerFragments);

                stanzaBuilder.addPreparedElement(newAuthElement);
            } else {
                stanzaBuilder.addPreparedElement(innerElement);
            }
        }

        return stanzaBuilder.build();
    }

    @Override
    public void process(HttpServletRequest httpRequest, Stanza body)
    {
        Stanza bodyToBeProcessed = body;

        /* If an auth stanza is coming, try to take authentication information from XWiki */
        XMLElement authElement = null;
        try {
            authElement = body.getSingleInnerElementsNamed(AUTH_STANZA_NAME);
        } catch (XMLSemanticError e) {
            /* Do nothing */
        }

        if (authElement != null) {
            /* This is an "auth" stanza. Try to authenticate the user using cookies. */
            ExecutionContext executionContext = execution.getContext();
            XWikiContext xwikiContext = (XWikiContext) executionContext.getProperty("xwikicontext");

            DocumentReference xwikiUserReference = xwikiContext.getUserReference();
            String xwikiUser = null;

            /* If cookies authentication is successful, rewrite the "auth" stanza accordingly. */
            if (xwikiUserReference != null) {
                xwikiUser = xwikiContext.getUserReference().getName();
                bodyToBeProcessed = addXWikiUserToAuthStanza(xwikiUser, body);
            }
        }

        super.process(httpRequest, bodyToBeProcessed);
    }

}
