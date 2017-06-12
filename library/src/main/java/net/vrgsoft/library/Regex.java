package net.vrgsoft.library;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {
    public static final String IMAGE_PATTERN = "(.+?)\\.(jpg|png|gif|bmp)$";
    public static final String IMAGE_TAG_PATTERN = "<img(.*?)src=(\"|')(.+?)(gif|jpg|png|bmp)(\"|')(.*?)(/)?>(</img>)?";
    public static final String ICON_TAG_PATTERN = "<img(.*?)src=(\"|')(.+?)(gif|jpg|png|bmp)(\"|')(.*?)(/)?>(</img>)?";
    public static final String ICON_REV_TAG_PATTERN = "<img(.*?)src=(\"|')(.+?)(gif|jpg|png|bmp)(\"|')(.*?)(/)?>(</img>)?";
    public static final String ITEMPROP_IMAGE_TAG_PATTERN = "<img(.*?)src=(\"|')(.+?)(gif|jpg|png|bmp)(\"|')(.*?)(/)?>(</img>)?";
    public static final String ITEMPROP_IMAGE_REV_TAG_PATTERN = "<img(.*?)src=(\"|')(.+?)(gif|jpg|png|bmp)(\"|')(.*?)(/)?>(</img>)?";
    public static final String TITLE_PATTERN = "<title(.*?)>(.*?)</title>";
    public static final String SCRIPT_PATTERN = "<script(.*?)>(.*?)</script>";
    public static final String METATAG_PATTERN = "<meta(.*?)>";
    public static final String METATAG_CONTENT_PATTERN = "content=\"(.*?)\"";
    public static final String URL_PATTERN = "<\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]>";

    public static String match(String content, String pattern, int index) {

        String match = "";
        Matcher matcher = Pattern.compile(pattern).matcher(content);

        while (matcher.find()) {
            match = matcher.group(index);
            break;
        }

        return LinkCrawler.extendedTrim(match);
    }

    public static List<String> matchAll(String content, String pattern,
                                        int index) {

        List<String> matches = new ArrayList<String>();
        Matcher matcher = Pattern.compile(pattern).matcher(content);

        while (matcher.find()) {
            matches.add(LinkCrawler.extendedTrim(matcher.group(index)));
        }

        return matches;
    }


}
