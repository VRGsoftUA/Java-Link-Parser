package net.vrgsoft.library;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;

public class LinkCrawler {

    private final String HTTP_PROTOCOL = "http://";
    private final String HTTPS_PROTOCOL = "https://";

    private OnPreloadCallback callback;
    private Map<String, ParseContent> mCache = new HashMap<>();
    private PublishProcessor<Result> mProcessor = PublishProcessor.create();

    public void setPreloadCallback(OnPreloadCallback callback) {
        this.callback = callback;
    }

    public Flowable<Result> parseUrl(String url) {
        initUrl(url);
        return mProcessor;
    }

    private void initUrl(final String url) {
        if (callback != null) {
            callback.onPre();
        }
        if (mCache.containsKey(url)) {
            mProcessor.onNext(new Result(mCache.get(url), isNull(mCache.get(url)), url));
        } else {
            getCode(url)
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Consumer<ParseContent>() {
                        @Override
                        public void accept(@NonNull ParseContent parseContent) throws Exception {
                            mCache.put(url, parseContent);
                            mProcessor.onNext(new Result(parseContent, isNull(parseContent), url));
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                        }
                    });
        }
    }

    /**
     * Get html code
     */
    private Single<ParseContent> getCode(final String url) {

        final ParseContent sourceContent = new ParseContent();

        return Single.fromCallable(new Callable<ParseContent>() {
            @Override
            public ParseContent call() throws Exception {
                ArrayList<String> urls;
                urls = SearchUrls.matches(url);

                if (urls.size() > 0)
                    sourceContent
                            .setFinalUrl(unshortenUrl(extendedTrim(urls.get(0))));
                else
                    sourceContent.setFinalUrl("");

                if (!sourceContent.getFinalUrl().equals("")) {
                    if (isImage(sourceContent.getFinalUrl())
                            && !sourceContent.getFinalUrl().contains("dropbox")) {
                        sourceContent.setSuccess(true);

                        sourceContent.getImages().add(sourceContent.getFinalUrl());

                        sourceContent.setTitle("");
                        sourceContent.setDescription("");

                    } else {
                        try {
                            Document doc = Jsoup
                                    .connect(sourceContent.getFinalUrl())
                                    .userAgent("Mozilla").get();

                            sourceContent.setHtmlCode(extendedTrim(doc.toString()));

                            HashMap<String, String> metaTags = getMetaTags(sourceContent
                                    .getHtmlCode());

                            sourceContent.setMetaTags(metaTags);

                            sourceContent.setTitle(metaTags.get("title"));
                            sourceContent.setDescription(metaTags
                                    .get("description"));

                            if (sourceContent.getTitle().equals("")) {
                                String matchTitle = Regex.match(
                                        sourceContent.getHtmlCode(),
                                        Regex.TITLE_PATTERN, 2);

                                if (!matchTitle.equals(""))
                                    sourceContent.setTitle(htmlDecode(matchTitle));
                            }

                            if (sourceContent.getDescription().equals(""))
                                sourceContent
                                        .setDescription(crawlCode(sourceContent
                                                .getHtmlCode()));

                            sourceContent.setDescription(sourceContent
                                    .getDescription().replaceAll(
                                            Regex.SCRIPT_PATTERN, ""));

                            if (!metaTags.get("image").equals(""))
                                sourceContent.getImages().add(
                                        metaTags.get("image"));
                            else {
                                sourceContent.setImages(getImages(doc));
                            }

                            sourceContent.setSuccess(true);
                        } catch (Exception e) {
                            sourceContent.setSuccess(false);
                        }
                    }
                }

                String[] finalLinkSet = sourceContent.getFinalUrl().split("&");
                sourceContent.setUrl(finalLinkSet[0]);

                sourceContent.setCannonicalUrl(cannonicalPage(sourceContent
                        .getFinalUrl()));
                sourceContent.setDescription(stripTags(sourceContent
                        .getDescription()));
                return sourceContent;
            }
        });
        // Don't forget the http:// or https://


    }

    private boolean isNull(ParseContent parseContent) {
        return !parseContent.isSuccess() &&
                extendedTrim(parseContent.getHtmlCode()).equals("") &&
                !isImage(parseContent.getFinalUrl());
    }

    /**
     * Gets content from a html tag
     */

    private String getTagContent(String tag, String content) {

        String pattern = "<" + tag + "(.*?)>(.*?)</" + tag + ">";
        String result = "", currentMatch = "";

        List<String> matches = Regex.matchAll(content, pattern, 2);

        int matchesSize = matches.size();
        for (int i = 0; i < matchesSize; i++) {
            currentMatch = stripTags(matches.get(i));
            if (currentMatch.length() >= 120) {
                result = extendedTrim(currentMatch);
                break;
            }
        }

        if (result.equals("")) {
            String matchFinal = Regex.match(content, pattern, 2);
            result = extendedTrim(matchFinal);
        }

        result = result.replaceAll("&nbsp;", "");

        return htmlDecode(result);
    }

    /**
     * Gets images from the html code
     */
    private List<String> getImages(Document document) {
        List<String> matches = new ArrayList<String>();

        Elements media = document.select("[src]");

        for (Element srcElement : media) {
            if (srcElement.tagName().equals("img")) {
                matches.add(srcElement.attr("abs:src"));
            }
        }

        return matches;
    }

    /**
     * Transforms from html to normal string
     */
    private String htmlDecode(String content) {
        return Jsoup.parse(content).text();
    }

    /**
     * Crawls the code looking for relevant information
     */
    private String crawlCode(String content) {
        String result = "";
        String resultSpan = "";
        String resultParagraph = "";
        String resultDiv = "";

        resultSpan = getTagContent("span", content);
        resultParagraph = getTagContent("p", content);
        resultDiv = getTagContent("div", content);

        result = resultSpan;

        if (resultParagraph.length() > resultSpan.length()
                && resultParagraph.length() >= resultDiv.length())
            result = resultParagraph;
        else if (resultParagraph.length() > resultSpan.length()
                && resultParagraph.length() < resultDiv.length())
            result = resultDiv;
        else
            result = resultParagraph;

        return htmlDecode(result);
    }

    /**
     * Returns the cannoncial url
     */
    private String cannonicalPage(String url) {

        String cannonical = "";
        if (url.startsWith(HTTP_PROTOCOL)) {
            url = url.substring(HTTP_PROTOCOL.length());
        } else if (url.startsWith(HTTPS_PROTOCOL)) {
            url = url.substring(HTTPS_PROTOCOL.length());
        }

        int urlLength = url.length();
        for (int i = 0; i < urlLength; i++) {
            if (url.charAt(i) != '/')
                cannonical += url.charAt(i);
            else
                break;
        }

        return cannonical;

    }

    /**
     * Strips the tags from an element
     */
    private String stripTags(String content) {
        return Jsoup.parse(content).text();
    }

    /**
     * Verifies if the url is an image
     */
    private boolean isImage(String url) {
        return url.matches(Regex.IMAGE_PATTERN);
    }

    /**
     * Returns meta tags from html code
     */
    private HashMap<String, String> getMetaTags(String content) {

        HashMap<String, String> metaTags = new HashMap<String, String>();
        metaTags.put("url", "");
        metaTags.put("title", "");
        metaTags.put("description", "");
        metaTags.put("image", "");

        List<String> matches = Regex.matchAll(content,
                Regex.METATAG_PATTERN, 1);

        for (String match : matches) {
            final String lowerCase = match.toLowerCase();
            if (lowerCase.contains("property=\"og:url\"")
                    || lowerCase.contains("property='og:url'")
                    || lowerCase.contains("name=\"url\"")
                    || lowerCase.contains("name='url'"))
                updateMetaTag(metaTags, "url", separeMetaTagsContent(match));
            else if (lowerCase.contains("property=\"og:title\"")
                    || lowerCase.contains("property='og:title'")
                    || lowerCase.contains("name=\"title\"")
                    || lowerCase.contains("name='title'"))
                updateMetaTag(metaTags, "title", separeMetaTagsContent(match));
            else if (lowerCase
                    .contains("property=\"og:description\"")
                    || lowerCase
                    .contains("property='og:description'")
                    || lowerCase.contains("name=\"description\"")
                    || lowerCase.contains("name='description'"))
                updateMetaTag(metaTags, "description", separeMetaTagsContent(match));
            else if (lowerCase.contains("property=\"og:image\"")
                    || lowerCase.contains("property='og:image'")
                    || lowerCase.contains("name=\"image\"")
                    || lowerCase.contains("name='image'"))
                updateMetaTag(metaTags, "image", separeMetaTagsContent(match));
        }

        return metaTags;
    }

    private void updateMetaTag(HashMap<String, String> metaTags, String url, String value) {
        if (value != null && (value.length() > 0)) {
            metaTags.put(url, value);
        }
    }

    /**
     * Gets content from metatag
     */
    private String separeMetaTagsContent(String content) {
        String result = Regex.match(content, Regex.METATAG_CONTENT_PATTERN,
                1);
        return htmlDecode(result);
    }

    /**
     * Unshortens a short url
     */
    private String unshortenUrl(String shortURL) {
        if (!shortURL.startsWith(HTTP_PROTOCOL)
                && !shortURL.startsWith(HTTPS_PROTOCOL))
            return "";

        URLConnection urlConn = connectURL(shortURL);
        urlConn.getHeaderFields();

        String finalResult = urlConn.getURL().toString();

        urlConn = connectURL(finalResult);
        urlConn.getHeaderFields();

        shortURL = urlConn.getURL().toString();

        while (!shortURL.equals(finalResult)) {
            finalResult = unshortenUrl(finalResult);
        }

        return finalResult;
    }

    /**
     * Takes a valid url and return a URL object representing the url address.
     */
    private URLConnection connectURL(String strURL) {
        URLConnection conn = null;
        try {
            URL inputURL = new URL(strURL);
            conn = inputURL.openConnection();
        } catch (MalformedURLException e) {
            System.out.println("Please input a valid URL");
        } catch (IOException ioe) {
            System.out.println("Can not connect to the URL");
        }
        return conn;
    }

    /**
     * Removes extra spaces and trim the string
     */
    static String extendedTrim(String content) {
        return content.replaceAll("\\s+", " ").replace("\n", " ")
                .replace("\r", " ").trim();
    }
}
