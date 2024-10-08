package com.sec.internal.helper.userconsent;

import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;
import com.sec.internal.constants.ims.cmstore.utils.OMAGlobalVariables;
import java.util.regex.Matcher;

public final class HyperlinkUtils {
    private HyperlinkUtils() {
    }

    public static void processLinks(TextView textView, String str, IHyperlinkOnClickListener iHyperlinkOnClickListener) {
        Matcher matcher = HyperlinkPatterns.webUrlPattern.matcher(str);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (matcher.find(i)) {
            String group = matcher.group();
            sb.append(str.substring(i, matcher.start()));
            sb.append("<a href='");
            if (!group.contains(OMAGlobalVariables.HTTP) && !group.contains("rtsp")) {
                sb.append("http://");
            }
            sb.append(group);
            sb.append("'>");
            sb.append(group);
            sb.append("</a>");
            i = matcher.end() > str.length() ? str.length() : matcher.end();
        }
        sb.append(str.substring(i, str.length()));
        Spanned fromHtml = Html.fromHtml(sb.toString(), 0);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(fromHtml);
        for (URLSpan makeLinkClickable : (URLSpan[]) spannableStringBuilder.getSpans(0, fromHtml.length(), URLSpan.class)) {
            makeLinkClickable(spannableStringBuilder, makeLinkClickable, iHyperlinkOnClickListener);
        }
        textView.setText(spannableStringBuilder);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private static void makeLinkClickable(SpannableStringBuilder spannableStringBuilder, final URLSpan uRLSpan, final IHyperlinkOnClickListener iHyperlinkOnClickListener) {
        spannableStringBuilder.setSpan(new ClickableSpan() {
            public void onClick(View view) {
                IHyperlinkOnClickListener.this.onClick(view, Uri.parse(uRLSpan.getURL()));
            }
        }, spannableStringBuilder.getSpanStart(uRLSpan), spannableStringBuilder.getSpanEnd(uRLSpan), spannableStringBuilder.getSpanFlags(uRLSpan));
        spannableStringBuilder.removeSpan(uRLSpan);
    }
}
