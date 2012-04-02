<?xml version="1.0" encoding="UTF-8"?>

<!--
  *
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
  *
-->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" encoding="ISO-8859-1" />

  <xsl:param name="stylesheets.vm.path"></xsl:param>
  <xsl:param name="javascript.vm.path"></xsl:param>
  <xsl:param name="footer.vm.path"></xsl:param>
  <xsl:param name="strophe.js.path"></xsl:param>
  <xsl:param name="strophe.muc.js.path"></xsl:param>
  <xsl:param name="xwiki-chat.js.path"></xsl:param>
  <xsl:param name="xwiki-chat-ui.js.path"></xsl:param>
  <xsl:param name="xwiki-chat.css.path"></xsl:param>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <!-- Declare here all the properties that are not present in the original skin. -->
  <xsl:template match="class">

    <class>

      <xsl:apply-templates select="@*|node()" />

      <stylesheets.vm>
        <customDisplay></customDisplay>
        <disabled>0</disabled>
        <editor>PureText</editor>
        <name>javascript.vm</name>
        <number>10</number>
        <picker>0</picker>
        <prettyName>javascript.vm</prettyName>
        <rows>15</rows>
        <size>80</size>
        <unmodifiable>0</unmodifiable>
        <validationMessage></validationMessage>
        <validationRegExp></validationRegExp>
        <classType>com.xpn.xwiki.objects.classes.TextAreaClass</classType>
      </stylesheets.vm>

      <javascript.vm>
        <customDisplay></customDisplay>
        <disabled>0</disabled>
        <editor>PureText</editor>
        <name>javascript.vm</name>
        <number>11</number>
        <picker>0</picker>
        <prettyName>javascript.vm</prettyName>
        <rows>15</rows>
        <size>80</size>
        <unmodifiable>0</unmodifiable>
        <validationMessage></validationMessage>
        <validationRegExp></validationRegExp>
        <classType>com.xpn.xwiki.objects.classes.TextAreaClass</classType>
      </javascript.vm>
      
      <!-- The footer.vm field is already present in the original skin, no need to declare it here. -->

      <strophe.js>
        <customDisplay></customDisplay>
        <disabled>0</disabled>
        <editor>PureText</editor>
        <name>javascript.vm</name>
        <number>12</number>
        <picker>0</picker>
        <prettyName>javascript.vm</prettyName>
        <rows>15</rows>
        <size>80</size>
        <unmodifiable>0</unmodifiable>
        <validationMessage></validationMessage>
        <validationRegExp></validationRegExp>
        <classType>com.xpn.xwiki.objects.classes.TextAreaClass</classType>
      </strophe.js>

      <strophe.muc.js>
        <customDisplay></customDisplay>
        <disabled>0</disabled>
        <editor>PureText</editor>
        <name>javascript.vm</name>
        <number>13</number>
        <picker>0</picker>
        <prettyName>javascript.vm</prettyName>
        <rows>15</rows>
        <size>80</size>
        <unmodifiable>0</unmodifiable>
        <validationMessage></validationMessage>
        <validationRegExp></validationRegExp>
        <classType>com.xpn.xwiki.objects.classes.TextAreaClass</classType>
      </strophe.muc.js>

      <xwiki-chat.js>
        <customDisplay></customDisplay>
        <disabled>0</disabled>
        <editor>PureText</editor>
        <name>javascript.vm</name>
        <number>14</number>
        <picker>0</picker>
        <prettyName>javascript.vm</prettyName>
        <rows>15</rows>
        <size>80</size>
        <unmodifiable>0</unmodifiable>
        <validationMessage></validationMessage>
        <validationRegExp></validationRegExp>
        <classType>com.xpn.xwiki.objects.classes.TextAreaClass</classType>
      </xwiki-chat.js>

      <xwiki-chat-ui.js>
        <customDisplay></customDisplay>
        <disabled>0</disabled>
        <editor>PureText</editor>
        <name>javascript.vm</name>
        <number>15</number>
        <picker>0</picker>
        <prettyName>javascript.vm</prettyName>
        <rows>15</rows>
        <size>80</size>
        <unmodifiable>0</unmodifiable>
        <validationMessage></validationMessage>
        <validationRegExp></validationRegExp>
        <classType>com.xpn.xwiki.objects.classes.TextAreaClass</classType>
      </xwiki-chat-ui.js>

      <xwiki-chat.css>
        <customDisplay></customDisplay>
        <disabled>0</disabled>
        <editor>PureText</editor>
        <name>javascript.vm</name>
        <number>16</number>
        <picker>0</picker>
        <prettyName>javascript.vm</prettyName>
        <rows>15</rows>
        <size>80</size>
        <unmodifiable>0</unmodifiable>
        <validationMessage></validationMessage>
        <validationRegExp></validationRegExp>
        <classType>com.xpn.xwiki.objects.classes.TextAreaClass</classType>
      </xwiki-chat.css>
    </class>
  </xsl:template>

  <xsl:template match="object">
    <xsl:variable name="stylesheets.vm" select="unparsed-text($stylesheets.vm.path,'utf-8')" />
    <xsl:variable name="javascript.vm" select="unparsed-text($javascript.vm.path,'utf-8')" />
    <xsl:variable name="footer.vm" select="unparsed-text($footer.vm.path,'utf-8')" />
    <xsl:variable name="strophe.js" select="unparsed-text($strophe.js.path,'utf-8')" />
    <xsl:variable name="strophe.muc.js" select="unparsed-text($strophe.muc.js.path,'utf-8')" />
    <xsl:variable name="xwiki-chat.js" select="unparsed-text($xwiki-chat.js.path,'utf-8')" />
    <xsl:variable name="xwiki-chat-ui.js" select="unparsed-text($xwiki-chat-ui.js.path,'utf-8')" />
    <xsl:variable name="xwiki-chat.css" select="unparsed-text($xwiki-chat.css.path,'utf-8')" />
    <object>
      <xsl:apply-templates select="@*|node()" />

      <property>
        <stylesheets.vm>
          <xsl:value-of select="$stylesheets.vm" />
        </stylesheets.vm>
      </property>

      <property>
        <javascript.vm>
          <xsl:value-of select="$javascript.vm" />
        </javascript.vm>
      </property>

      <property>
        <footer.vm>
          <xsl:value-of select="$footer.vm" />
        </footer.vm>
      </property>

      <property>
        <strophe.js>
          <xsl:value-of select="$strophe.js" />
        </strophe.js>
      </property>

      <property>
        <strophe.muc.js>
          <xsl:value-of select="$strophe.muc.js" />
        </strophe.muc.js>
      </property>

      <property>
        <xwiki-chat.js>
          <xsl:value-of select="$xwiki-chat.js" />
        </xwiki-chat.js>
      </property>

      <property>
        <xwiki-chat-ui.js>
          <xsl:value-of select="$xwiki-chat-ui.js" />
        </xwiki-chat-ui.js>
      </property>

      <property>
        <xwiki-chat.css>
          <xsl:value-of select="$xwiki-chat.css" />
        </xwiki-chat.css>
      </property>

    </object>
  </xsl:template>

</xsl:stylesheet>