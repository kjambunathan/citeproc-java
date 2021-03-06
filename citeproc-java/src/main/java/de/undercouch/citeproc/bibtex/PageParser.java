package de.undercouch.citeproc.bibtex;

import de.undercouch.citeproc.bibtex.internal.InternalPageLexer;
import de.undercouch.citeproc.bibtex.internal.InternalPageParser;
import de.undercouch.citeproc.bibtex.internal.InternalPageParser.PagesContext;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Parses pages
 * @author Michel Kraemer
 */
public class PageParser {
    /**
     * Parses a given page or range of pages. If the given string cannot
     * be parsed, the method will return a page range with a literal string.
     * @param pages the page or range of pages
     * @return the parsed page or page range (never {@code null})
     */
    public static PageRange parse(String pages) {
        CharStream cs = CharStreams.fromString(pages);
        InternalPageLexer lexer = new InternalPageLexer(cs);
        lexer.removeErrorListeners(); // do not output errors to console
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        InternalPageParser parser = new InternalPageParser(tokens);
        parser.removeErrorListeners(); // do not output errors to console
        PagesContext ctx = parser.pages();
        if (ctx.literal == null || ctx.literal.isEmpty() ||
                ctx.exception != null || parser.getNumberOfSyntaxErrors() > 0) {
            // unparsable fall back to literal string
            return new PageRange(pages, null, null, false);
        }
        return new PageRange(ctx.literal, ctx.pageFrom, ctx.numberOfPages,
                ctx.multiplePages);
    }
}
