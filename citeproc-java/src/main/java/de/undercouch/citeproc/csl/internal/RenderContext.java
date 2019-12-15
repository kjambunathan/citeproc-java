package de.undercouch.citeproc.csl.internal;

import de.undercouch.citeproc.csl.CSLDate;
import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.csl.CSLName;
import de.undercouch.citeproc.csl.internal.locale.LLocale;
import de.undercouch.citeproc.csl.internal.locale.LTerm;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains information necessary to render citations and bibliographies. This
 * includes citation items, variables, and terms. The render context also
 * provides methods to emit rendered text to a {@link TokenBuffer}. This buffer
 * is available through the {@link #getResult()} method.
 * @author Michel Kraemer
 */
public class RenderContext {
    /**
     * The style used to render citation items and bibliographies
     */
    private final SStyle style;

    /**
     * Localization data
     */
    private final LLocale locale;

    /**
     * The citation item to render
     */
    private final CSLItemData itemData;

    /**
     * A token buffer collecting the rendered text
     */
    private final TokenBuffer result = new TokenBuffer();

    /**
     * The number of times a variable was fetched from the context. This
     * variable is an {@link AtomicInteger} so we can pass it down to child
     * contexts to allow them to update the value.
     */
    private final AtomicInteger variablesCalled;

    /**
     * The number of times an empty variable was fetched from the context. This
     * value is always lower than or equal to {@link #variablesCalled}
     */
    private final AtomicInteger variablesEmpty;

    /**
     * Creates a new render context
     * @param style the style used to render citation items and bibliographies
     * @param locale localization data
     * @param itemData the citation item to render
     */
    public RenderContext(SStyle style, LLocale locale, CSLItemData itemData) {
        this.style = style;
        this.locale = locale;
        this.itemData = itemData;
        this.variablesCalled = new AtomicInteger(0);
        this.variablesEmpty = new AtomicInteger(0);
    }

    /**
     * Creates a new render context that has the same attributes as the given
     * parent context but with an empty token buffer. Changes to any of the
     * properties (except for the token buffer) will reflect in the parent
     * context.
     * @param parent the parent context
     */
    public RenderContext(RenderContext parent) {
        this.style = parent.style;
        this.locale = parent.locale;
        this.itemData = parent.itemData;
        this.variablesCalled = parent.variablesCalled;
        this.variablesEmpty = parent.variablesEmpty;
    }

    /**
     * Get the localization data
     * @return the localization data
     */
    public LLocale getLocale() {
        return locale;
    }

    /**
     * Get the macro with the given name
     * @param name the macro's name
     * @return the macro (never {@code null})
     */
    public SMacro getMacro(String name) {
        SMacro result = style.getMacros().get(name);
        if (result == null) {
            throw new IllegalArgumentException("Unknown macro: " + name);
        }
        return result;
    }

    /**
     * Get the value of the variable with the given name
     * @param name the variable's name
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the variable is unknown
     */
    public String getVariable(String name) {
        String result;
        switch (name) {
            case "genre":
                result = itemData.getGenre();
                break;
            case "citation-number":
                result = itemData.getCitationNumber();
                break;
            case "container-title":
                result = itemData.getContainerTitle();
                break;
            case "number":
                result = itemData.getNumber();
                break;
            case "page":
                result = itemData.getPage();
                break;
            case "publisher":
                result = itemData.getPublisher();
                break;
            case "publisher-place":
                result = itemData.getPublisherPlace();
                break;
            case "title":
                result = itemData.getTitle();
                break;
            case "URL":
                result = itemData.getURL();
                break;
            case "volume":
                result = itemData.getVolume();
                break;
            case "number-of-volumes":
                result = itemData.getNumberOfVolumes();
                break;
            case "issue":
                result = itemData.getIssue();
                break;
            default:
                throw new IllegalArgumentException("Unknown variable: " + name);
        }

        variablesCalled.incrementAndGet();
        if (result == null) {
            variablesEmpty.incrementAndGet();
        }
        return result;
    }

    /**
     * Get the value of the date variable with the given name
     * @param name the variable's name
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the date variable is unknown
     */
    public CSLDate getDateVariable(String name) {
        CSLDate result;
        switch (name) {
            case "accessed":
                result = itemData.getAccessed();
                break;
            case "issued":
                result = itemData.getIssued();
                break;
            default:
                throw new IllegalArgumentException("Unknown date variable: " + name);
        }

        variablesCalled.incrementAndGet();
        if (result == null) {
            variablesEmpty.incrementAndGet();
        }
        return result;
    }

    public CSLName[] getNameVariable(String name) {
        CSLName[] result;
        switch (name) {
            case "author":
                result = itemData.getAuthor();
                break;
            case "editor":
                result = itemData.getEditor();
                break;
            default:
                throw new IllegalArgumentException("Unknown name variable: " + name);
        }

        variablesCalled.incrementAndGet();
        if (result == null) {
            variablesEmpty.incrementAndGet();
        }
        return result;
    }

    /**
     * Get the singular long form of a term
     * @param name the term's name
     * @return the term's value (never {@code null})
     */
    public String getTerm(String name) {
        return getTerm(name, LTerm.Form.LONG);
    }

    /**
     * Get the long form of a term
     * @param name the term's name
     * @param plural {@code true} if the plural form should be retrieved,
     * {@code false} for the singular form
     * @return the term's value (never {@code null})
     */
    public String getTerm(String name, boolean plural) {
        return getTerm(name, LTerm.Form.LONG, plural);
    }

    /**
     * Get the singular form of a term
     * @param name the term's name
     * @param form the form to retrieve
     * @return the term's value (never {@code null})
     */
    public String getTerm(String name, LTerm.Form form) {
        return getTerm(name, form, false);
    }

    /**
     * Get a term
     * @param name the term's name
     * @param form the form to retrieve
     * @param plural {@code true} if the plural form should be retrieved,
     * {@code false} for the singular form
     * @return the term's value (never {@code null})
     */
    public String getTerm(String name, LTerm.Form form, boolean plural) {
        Map<String, LTerm> tm = locale.getTerms().get(form);
        if (tm == null) {
            throw new IllegalStateException("Unknown term form: " + form);
        }
        LTerm t = tm.get(name);
        if (t == null) {
            throw new IllegalStateException("Unknown term: " + name);
        }
        if (plural) {
            return t.getMultiple();
        }
        return t.getSingle();
    }

    /**
     * Get the citation item currently being rendered
     * @return the citation item
     */
    public CSLItemData getItemData() {
        return itemData;
    }

    /**
     * Get the number of times a variable was fetched from the context
     * @return the number
     */
    public int getNumberOfCalledVariables() {
        return variablesCalled.get();
    }

    /**
     * Get the number of times an empty variable was fetched from the context.
     * This value is always lower than or equal to the one returned by
     * {@link #getNumberOfCalledVariables()}.
     * @return the number
     */
    public int getNumberOfEmptyVariables() {
        return variablesEmpty.get();
    }

    /**
     * Emit a text token
     * @param text the text token
     * @return this render context
     */
    public RenderContext emit(String text) {
        return emit(text, Token.Type.TEXT);
    }

    /**
     * Emit a token of a given type
     * @param text the token's text
     * @param type the token's type
     * @return this render context
     */
    public RenderContext emit(String text, Token.Type type) {
        result.append(text, type);
        return this;
    }

    /**
     * Emit a token
     * @param token the token
     * @return this render context
     */
    public RenderContext emit(Token token) {
        result.append(token);
        return this;
    }

    /**
     * Emit all tokens from the given token buffer
     * @param buffer the token buffer
     * @return this render context
     */
    public RenderContext emit(TokenBuffer buffer) {
        result.append(buffer);
        return this;
    }

    /**
     * Get a token buffer containing all emitted tokens
     * @return the token buffer
     */
    public TokenBuffer getResult() {
        return result;
    }
}
