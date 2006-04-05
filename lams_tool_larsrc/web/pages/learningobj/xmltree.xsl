<?xml version="1.0"?>

<!-- 
  Copyright (C) 2005 LAMS Foundation (http://lamsfoundation.org)

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  USA

  http://www.gnu.org/licenses/gpl.txt 
-->

   <xsl:stylesheet 
      xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
      version="1.0">

   <xsl:output method="html"></xsl:output>
   
   <xsl:param name="urlPrefix"/>
   <xsl:param name="urlSuffix"/>

   <xsl:template match="/">
      <xsl:apply-templates/>
   </xsl:template>
   
   <xsl:template match="organizations">
var TREE_ITEMS = [
      		<xsl:apply-templates/>
];
   </xsl:template>
   

   <xsl:template match="item">
	<xsl:choose>
		<xsl:when test="@resource">
			['<xsl:value-of select="@title" />','<xsl:value-of select="$urlPrefix" /><xsl:value-of select="@resource"/><xsl:value-of select="urlSuffix" />',
		    <xsl:apply-templates/>
		    ],
		</xsl:when>
		<xsl:otherwise>
		    ['<xsl:value-of select="@title" />',0,
		    <xsl:apply-templates/>
		    ],		    
		</xsl:otherwise>
	</xsl:choose>
   </xsl:template>
   
   </xsl:stylesheet>