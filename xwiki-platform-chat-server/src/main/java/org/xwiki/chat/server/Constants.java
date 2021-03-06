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

/**
 * Constants.
 * 
 * @version $Id$
 */
public interface Constants
{
    /**
     * The servlet context attribute name to be used for storing the XMPP server.
     */
    String XMPP_SERVER_ATTRIBUTE = "xmpp_server";

    /**
     * The servlet context attribute name to be used for storing the global cookie authentication password.
     */
    String XMPP_COOKIE_AUTHENTICATION_PASSWORD_ATTRIBUTE = "xmpp_cookie_authentication_password";
}
