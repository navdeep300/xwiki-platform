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
package org.xwiki.rendering.util;

import java.io.StringReader;
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
import java.util.List;

import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.internal.parser.WikiModelXHTMLParser;
import org.xwiki.rendering.internal.parser.WikiModelXWikiParser;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;

/**
 * Methods for helping in parsing. 
 * 
 * @version $Id$
 * @since 1.7M1
 */
public class ParserUtils
{
    public List<Block> parseInline(Parser parser, String content) throws ParseException
    {
        List<Block> result;
        
        // TODO: Use an inline parser. See http://jira.xwiki.org/jira/browse/XWIKI-2748

        // We want the XWiki parser to consider we're inside a paragraph already since links can only
        // happen in paragraph and for example if there's a macro specified as the label it should
        // generate an inline macro and not a standalone one. To force this we're explicitely adding
        // a paragraph and a word as the first content of the string to be parsed and we're removing it 
        // afterwards.
        if (WikiModelXWikiParser.class.isAssignableFrom(parser.getClass())) {
            result = parser.parse(new StringReader("xwikimarker " + content)).getChildren();
        } else if (WikiModelXHTMLParser.class.isAssignableFrom(parser.getClass())) {
            result = parser.parse(new StringReader("<p>xwikimarker " + content + "</p>")).getChildren();
        } else {
            result = parser.parse(new StringReader(content)).getChildren();
        }

        // Remove top level paragraph since we're already inside a paragraph.
        // TODO: Remove when http://code.google.com/p/wikimodel/issues/detail?id=87 is fixed
        removeTopLevelParagraph(result);
        
        // Remove our marker which is always the first 2 blocks (onWord("xwikimarker") + onSpace)
        if (WikiModelXWikiParser.class.isAssignableFrom(parser.getClass())
            || (WikiModelXHTMLParser.class.isAssignableFrom(parser.getClass())))
        {
            result.remove(0);
            result.remove(0);
        }
        
        return result;
    }
    
    /**
     * Removes any top level paragraph since for example for the following use case we don't want
     * an extra paragraph block: <code>= hello {{velocity}}world{{/velocity}}</code>.
     * 
     * @param blocks the blocks to check and convert
     */
    public void removeTopLevelParagraph(List<Block> blocks)
    {
        // Remove any top level paragraph so that the result of a macro can be used inline for example.
        // We only remove the paragraph if there's only one top level element and if it's a paragraph.
        if ((blocks.size() == 1) && ParagraphBlock.class.isAssignableFrom(blocks.get(0).getClass())) {
            Block paragraphBlock = blocks.remove(0);
            blocks.addAll(0, paragraphBlock.getChildren());
        }
    }
}
