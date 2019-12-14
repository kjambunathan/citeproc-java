package de.undercouch.citeproc.csl.internal.behavior;

import de.undercouch.citeproc.csl.internal.RenderContext;
import de.undercouch.citeproc.csl.internal.Token;
import de.undercouch.citeproc.helper.NodeHelper;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import java.util.function.Consumer;

/**
 * Wraps around a render function and removes all periods from its result
 * @author Michel Kraemer
 */
public class StripPeriods implements Behavior {
    private final boolean stripPeriods;

    /**
     * Parses an XML node and determines if periods should be removed or not
     * @param node the XML node
     */
    public StripPeriods(Node node) {
        stripPeriods = Boolean.parseBoolean(NodeHelper.getAttrValue(node, "strip-periods"));
    }

    @Override
    public void accept(Consumer<RenderContext> renderFunction, RenderContext ctx) {
        if (stripPeriods) {
            RenderContext child = new RenderContext(ctx);
            renderFunction.accept(child);
            child.getResult().getTokens().stream()
                    .map(this::transform)
                    .forEach(ctx::emit);
        } else {
            renderFunction.accept(ctx);
        }
    }

    /**
     * Removes all periods from a token's text
     * @param t the token
     * @return the new token with periods removed
     */
    private Token transform(Token t) {
        String s = StringUtils.remove(t.getText(), '.');
        return new Token(s, t.getType());
    }
}
