package kr.or.hieating.utils;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.web.util.HtmlUtils;

public final class HtmlSanitizer {

  private static final Pattern BLOCKED_CONTENT =
      Pattern.compile(
          "(?is)<\\s*(script|style|iframe|object|embed|meta|link)\\b.*?<\\s*/\\s*\\1\\s*>");
  private static final Pattern TAG = Pattern.compile("(?is)<\\s*(/)?\\s*([a-z0-9]+)([^>]*)>");
  private static final Pattern ATTRIBUTE =
      Pattern.compile("(?is)([a-z_:][-a-z0-9_:.]*)\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s\"'>]+)");
  private static final Set<String> ALLOWED_TAGS =
      Set.of(
          "p",
          "br",
          "strong",
          "b",
          "em",
          "i",
          "u",
          "ul",
          "ol",
          "li",
          "h2",
          "h3",
          "h4",
          "blockquote",
          "div",
          "span",
          "a",
          "img");

  private HtmlSanitizer() {}

  public static String sanitize(String html) {
    if (html == null || html.isBlank()) {
      return "";
    }

    String withoutBlockedContent = BLOCKED_CONTENT.matcher(html).replaceAll("");
    Matcher matcher = TAG.matcher(withoutBlockedContent);
    StringBuilder sanitized = new StringBuilder();
    int cursor = 0;

    while (matcher.find()) {
      sanitized.append(escapeText(withoutBlockedContent.substring(cursor, matcher.start())));
      sanitized.append(sanitizeTag(matcher));
      cursor = matcher.end();
    }

    sanitized.append(escapeText(withoutBlockedContent.substring(cursor)));
    return sanitized.toString();
  }

  private static String sanitizeTag(Matcher matcher) {
    String closingSlash = matcher.group(1);
    String tagName = matcher.group(2).toLowerCase(Locale.ROOT);
    String attributes = matcher.group(3);

    if (!ALLOWED_TAGS.contains(tagName)) {
      return "";
    }

    if (closingSlash != null) {
      return tagName.equals("br") || tagName.equals("img") ? "" : "</" + tagName + ">";
    }

    return switch (tagName) {
      case "br" -> "<br>";
      case "a" -> sanitizeLink(attributes);
      case "img" -> sanitizeImage(attributes);
      default -> "<" + tagName + ">";
    };
  }

  private static String sanitizeLink(String attributes) {
    String href = attributeValue(attributes, "href");
    if (!isSafeUrl(href, true)) {
      return "<a>";
    }
    return "<a href=\"" + escapeAttribute(href) + "\" rel=\"nofollow noopener noreferrer\">";
  }

  private static String sanitizeImage(String attributes) {
    String src = attributeValue(attributes, "src");
    if (!isSafeUrl(src, false)) {
      return "";
    }

    String alt = attributeValue(attributes, "alt");
    if (alt == null || alt.isBlank()) {
      return "<img src=\"" + escapeAttribute(src) + "\">";
    }
    return "<img src=\"" + escapeAttribute(src) + "\" alt=\"" + escapeAttribute(alt) + "\">";
  }

  private static String attributeValue(String attributes, String name) {
    Matcher matcher = ATTRIBUTE.matcher(attributes);
    while (matcher.find()) {
      if (matcher.group(1).equalsIgnoreCase(name)) {
        return unquote(matcher.group(2));
      }
    }
    return null;
  }

  private static String unquote(String value) {
    String unquoted = value;
    if ((unquoted.startsWith("\"") && unquoted.endsWith("\""))
        || (unquoted.startsWith("'") && unquoted.endsWith("'"))) {
      unquoted = unquoted.substring(1, unquoted.length() - 1);
    }
    return HtmlUtils.htmlUnescape(unquoted).trim();
  }

  private static boolean isSafeUrl(String value, boolean allowFragment) {
    if (value == null || value.isBlank()) {
      return false;
    }

    String normalized = value.replaceAll("[\\p{Cntrl}\\s]+", "").toLowerCase(Locale.ROOT);
    if (normalized.startsWith("javascript:")
        || normalized.startsWith("data:")
        || normalized.startsWith("vbscript:")) {
      return false;
    }

    return normalized.startsWith("https://")
        || normalized.startsWith("http://")
        || normalized.startsWith("/")
        || (allowFragment && normalized.startsWith("#"));
  }

  private static String escapeText(String text) {
    return HtmlUtils.htmlEscape(text);
  }

  private static String escapeAttribute(String value) {
    return HtmlUtils.htmlEscape(value, "UTF-8");
  }
}
