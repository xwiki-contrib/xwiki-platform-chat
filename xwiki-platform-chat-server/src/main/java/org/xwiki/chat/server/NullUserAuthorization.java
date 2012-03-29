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

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.authorization.UserAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class authorizes everyone to connect to the server.
 * 
 * @version $Id$
 */
public class NullUserAuthorization implements UserAuthorization
{
    /**
     * Logger.
     */
    private static Logger logger = LoggerFactory.getLogger(NullUserAuthorization.class);

    /**
     * {@inheritDoc}
     */
    public boolean verifyCredentials(Entity jid, String passwordCleartext, Object credentials)
    {
        logger.info("User with JID {} is logging in", jid.toString());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean verifyCredentials(String username, String passwordCleartext, Object credentials)
    {
        logger.info("User {} is logging in", username);
        return true;
    }

}
