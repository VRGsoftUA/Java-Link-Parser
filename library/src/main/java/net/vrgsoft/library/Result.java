package net.vrgsoft.library;

public class Result {
    private ParseContent mParseContent;
    private boolean isNull;
    private String mUrl;

    public Result(ParseContent mParseContent, boolean isNull, String mUrl) {
        this.mParseContent = mParseContent;
        this.isNull = isNull;
        this.mUrl = mUrl;
    }

    public ParseContent getmParseContent() {
        return mParseContent;
    }

    public boolean isNull() {
        return isNull;
    }

    public String getmUrl() {
        return mUrl;
    }
}
