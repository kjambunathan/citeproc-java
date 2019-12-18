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
        if (style.getLocale() != null &&
                (style.getLocale().getLang() == null ||
                        (style.getLocale().getLang().getLanguage().equals(locale.getLang().getLanguage()) &&
                                (style.getLocale().getLang().getCountry().isEmpty() ||
                                        style.getLocale().getLang().getCountry().equals(locale.getLang().getCountry()))))) {
            // additional localization data in the style file overrides or
            // augments the given localization data
            locale = locale.merge(style.getLocale());
        }

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
     * Get the value of a string, date, or name variable
     * @param name the variable's name
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the variable is unknown
     */
    public Object getVariable(String name) {
        Object result = getStringVariable(name);
        if (result != null) {
            return result;
        }

        result = getDateVariable(name);
        if (result != null) {
            return result;
        }

        return getNameVariable(name);
    }

    /**
     * Get the value of the string variable with the given name
     * @param name the variable's name
     * @return the variable's value or {@code null} if the value is not set
     * @throws IllegalArgumentException if the variable is unknown
     */
    public String getStringVariable(String name) {
        String result;
        switch (name) {
            case "abstract":
                result = itemData.getAbstrct();
                break;
            case "annote":
                result = itemData.getAnnote();
                break;
            case "archive":
                result = itemData.getArchive();
                break;
            case "archive_location":
                result = itemData.getArchiveLocation();
                break;
            case "archive-place":
                result = itemData.getArchivePlace();
                break;
            case "authority":
                result = itemData.getAuthority();
                break;
            case "call-number":
                result = itemData.getCallNumber();
                break;
            case "chapter-number":
                result = itemData.getChapterNumber();
                break;
            case "citation-label":
                result = itemData.getCitationLabel();
                break;
            case "citation-number":
                result = itemData.getCitationNumber();
                break;
            case "collection-number":
                result = itemData.getCollectionNumber();
                break;
            case "collection-title":
                result = itemData.getCollectionTitle();
                break;
            case "container-title":
                result = itemData.getContainerTitle();
                break;
            case "container-title-short":
                result = itemData.getContainerTitleShort();
                break;
            case "dimensions":
                result = itemData.getDimensions();
                break;
            case "DOI":
                result = itemData.getDOI();
                break;
            case "edition":
                result = itemData.getEdition();
                break;
            case "event":
                result = itemData.getEvent();
                break;
            case "event-place":
                result = itemData.getEventPlace();
                break;
            case "first-reference-note-number":
                result = itemData.getFirstReferenceNoteNumber();
                break;
            case "genre":
                result = itemData.getGenre();
                break;
            case "ISBN":
                result = itemData.getISBN();
                break;
            case "ISSN":
                result = itemData.getISSN();
                break;
            case "issue":
                result = itemData.getIssue();
                break;
            case "jurisdiction":
                result = itemData.getJurisdiction();
                break;
            case "keyword":
                result = itemData.getKeyword();
                break;
            case "locator":
                result = itemData.getLocator();
                break;
            case "medium":
                result = itemData.getMedium();
                break;
            case "note":
                result = itemData.getNote();
                break;
            case "number":
                result = itemData.getNumber();
                break;
            case "number-of-pages":
                result = itemData.getNumberOfPages();
                break;
            case "number-of-volumes":
                result = itemData.getNumberOfVolumes();
                break;
            case "original-publisher":
                result = itemData.getOriginalPublisher();
                break;
            case "original-publisher-place":
                result = itemData.getOriginalPublisherPlace();
                break;
            case "original-title":
                result = itemData.getOriginalTitle();
                break;
            case "page":
                result = itemData.getPage();
                break;
            case "page-first":
                result = itemData.getPageFirst();
                break;
            case "PMCID":
                result = itemData.getPMCID();
                break;
            case "PMID":
                result = itemData.getPMID();
                break;
            case "publisher":
                result = itemData.getPublisher();
                break;
            case "publisher-place":
                result = itemData.getPublisherPlace();
                break;
            case "references":
                result = itemData.getReferences();
                break;
            case "reviewed-title":
                result = itemData.getReviewedTitle();
                break;
            case "scale":
                result = itemData.getScale();
                break;
            case "section":
                result = itemData.getSection();
                break;
            case "source":
                result = itemData.getSource();
                break;
            case "status":
                result = itemData.getStatus();
                break;
            case "title":
                result = itemData.getTitle();
                break;
            case "title-short":
                result = itemData.getTitleShort();
                break;
            case "URL":
                result = itemData.getURL();
                break;
            case "version":
                result = itemData.getVersion();
                break;
            case "volume":
                result = itemData.getVolume();
                break;
            case "year-suffix":
                result = itemData.getYearSuffix();
                break;
            default:
                result = null;
                break;
        }

        variablesCalled.incrementAndGet();
        if (result == null || result.isEmpty()) {
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
            case "container":
                result = itemData.getContainer();
                break;
            case "event-date":
                result = itemData.getEventDate();
                break;
            case "issued":
                result = itemData.getIssued();
                break;
            case "original-date":
                result = itemData.getOriginalDate();
                break;
            case "submitted":
                result = itemData.getSubmitted();
                break;
            default:
                result = null;
                break;
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
            case "collection-editor":
                result = itemData.getCollectionEditor();
                break;
            case "composer":
                result = itemData.getComposer();
                break;
            case "container-author":
                result = itemData.getContainerAuthor();
                break;
            case "director":
                result = itemData.getDirector();
                break;
            case "editor":
                result = itemData.getEditor();
                break;
            case "editorial-director":
                result = itemData.getEditorialDirector();
                break;
            case "illustrator":
                result = itemData.getIllustrator();
                break;
            case "interviewer":
                result = itemData.getInterviewer();
                break;
            case "original-author":
                result = itemData.getOriginalAuthor();
                break;
            case "recipient":
                result = itemData.getRecipient();
                break;
            case "reviewed-author":
                result = itemData.getReviewedAuthor();
                break;
            case "translator":
                result = itemData.getTranslator();
                break;
            default:
                result = null;
                break;
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