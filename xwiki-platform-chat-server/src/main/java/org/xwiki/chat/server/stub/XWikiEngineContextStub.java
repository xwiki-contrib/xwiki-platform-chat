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
package org.xwiki.chat.server.stub;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.xpn.xwiki.web.XWikiEngineContext;

/**
 * This is a stub used for simulating servlet requests in order to be able to create an XWiki context.
 * 
 * @version $Id$
 */
public class XWikiEngineContextStub implements XWikiEngineContext
{

    /**
     * Attributes.
     */
    private Map<String, Object> attributes;

    /**
     * Constructor.
     */
    public XWikiEngineContextStub()
    {
        attributes = new HashMap<String, Object>();
    }

    @Override
    public Object getAttribute(String name)
    {
        return attributes.get(name);
    }

    @Override
    public void setAttribute(String name, Object value)
    {
        attributes.put(name, value);
    }

    @Override
    public String getRealPath(String path)
    {
        return "/";
    }

    @Override
    public URL getResource(String name) throws MalformedURLException
    {
        return getClass().getClassLoader().getResource(name);
    }

    @Override
    public InputStream getResourceAsStream(String name)
    {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    @Override
    public String getMimeType(String filename)
    {
        return null;
    }

}
