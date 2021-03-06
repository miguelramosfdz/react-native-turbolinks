package com.reactlibrary.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.basecamp.turbolinks.TurbolinksAdapter;
import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.ReactConstants;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;
import com.reactlibrary.R;
import com.reactlibrary.react.ReactAppCompatActivity;
import com.reactlibrary.util.TurbolinksRoute;

import java.net.MalformedURLException;
import java.net.URL;

import static com.reactlibrary.RNTurbolinksModule.INTENT_INITIAL_VISIT;
import static com.reactlibrary.RNTurbolinksModule.INTENT_MESSAGE_HANDLER;
import static com.reactlibrary.RNTurbolinksModule.INTENT_NAVIGATION_BAR_HIDDEN;
import static com.reactlibrary.RNTurbolinksModule.INTENT_ROUTE;
import static com.reactlibrary.RNTurbolinksModule.INTENT_USER_AGENT;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

public class WebActivity extends ReactAppCompatActivity implements GenericActivity, TurbolinksAdapter {

    private static final int HTTP_FAILURE = 0;
    private static final int NETWORK_FAILURE = 1;

    private HelperActivity helperAct;
    private TurbolinksRoute route;
    private String messageHandler;
    private String userAgent;
    private Boolean initialVisit;
    private Boolean navigationBarHidden;
    private TurbolinksView turbolinksView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        helperAct = new HelperActivity(this);
        route = getIntent().getParcelableExtra(INTENT_ROUTE);
        initialVisit = getIntent().getBooleanExtra(INTENT_INITIAL_VISIT, true);
        navigationBarHidden = getIntent().getBooleanExtra(INTENT_NAVIGATION_BAR_HIDDEN, false);
        messageHandler = getIntent().getStringExtra(INTENT_MESSAGE_HANDLER);
        userAgent = getIntent().getStringExtra(INTENT_USER_AGENT);

        helperAct.renderToolBar((Toolbar) findViewById(R.id.toolbar));
        turbolinksView = findViewById(R.id.turbolinks_view);
        visitTurbolinksView(turbolinksView, route.getUrl());
    }

    private void visitTurbolinksView(TurbolinksView turbolinksView, String url) {
        Context context = getApplicationContext();
        TurbolinksSession session = TurbolinksSession.getDefault(context);
        WebSettings settings = session.getWebView().getSettings();
        if (messageHandler != null) session.addJavascriptInterface(this, messageHandler);
        if (userAgent != null) settings.setUserAgentString(userAgent);
        session.activity(this).adapter(this).view(turbolinksView).visit(url);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .restoreWithCachedSnapshot(true)
                .view(turbolinksView)
                .visit(route.getUrl());
    }

    @Override
    public void onReceivedError(int errorCode) {
        WritableMap params = Arguments.createMap();
        params.putInt("code", NETWORK_FAILURE);
        params.putInt("statusCode", 0);
        params.putString("description", "Network Failure.");
        params.putInt("tabIndex", 0);
        getEventEmitter().emit("turbolinksError", params);
    }

    @Override
    public void requestFailedWithStatusCode(int statusCode) {
        WritableMap params = Arguments.createMap();
        params.putInt("code", HTTP_FAILURE);
        params.putInt("statusCode", statusCode);
        params.putString("description", "HTTP Failure. Code:" + statusCode);
        params.putInt("tabIndex", 0);
        getEventEmitter().emit("turbolinksError", params);
    }

    @Override
    public void visitProposedToLocationWithAction(String location, String action) {
        try {
            WritableMap params = Arguments.createMap();
            URL urlLocation = new URL(location);
            params.putString("component", null);
            params.putString("url", urlLocation.toString());
            params.putString("path", urlLocation.getPath());
            params.putString("action", action);
            getEventEmitter().emit("turbolinksVisit", params);
        } catch (MalformedURLException e) {
            Log.e(ReactConstants.TAG, "Error parsing URL. " + e.toString());
        }
    }

    @Override
    public void visitCompleted() {
        renderTitle();
        handleVisitCompleted();
    }

    @Override
    public void onPageFinished() {
    }

    @Override
    public void pageInvalidated() {
    }

    @Override
    public void onBackPressed() {
        if (getInitialVisit()) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return helperAct.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return helperAct.onOptionsItemSelected(item);
    }

    @Override
    public void renderTitle() {
        WebView webView = TurbolinksSession.getDefault(getApplicationContext()).getWebView();
        String title = route.getTitle() != null ? route.getTitle() : webView.getTitle();
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle(route.getSubtitle());
    }

    @Override
    public RCTDeviceEventEmitter getEventEmitter() {
        return getReactInstanceManager().getCurrentReactContext().getJSModule(RCTDeviceEventEmitter.class);
    }

    @Override
    public void handleTitlePress(Toolbar toolbar) {
        final WebView webView = TurbolinksSession.getDefault(getApplicationContext()).getWebView();
        toolbar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    WritableMap params = Arguments.createMap();
                    URL urlLocation = new URL(webView.getUrl());
                    params.putString("component", null);
                    params.putString("url", urlLocation.toString());
                    params.putString("path", urlLocation.getPath());
                    getEventEmitter().emit("turbolinksTitlePress", params);
                } catch (MalformedURLException e) {
                    Log.e(ReactConstants.TAG, "Error parsing URL. " + e.toString());
                }
            }
        });
    }

    private void handleVisitCompleted() {
        String javaScript = "document.documentElement.outerHTML";
        final WebView webView = TurbolinksSession.getDefault(getApplicationContext()).getWebView();
        webView.evaluateJavascript(javaScript, new ValueCallback<String>() {
            public void onReceiveValue(String source) {
                try {
                    WritableMap params = Arguments.createMap();
                    URL urlLocation = new URL(webView.getUrl());
                    params.putString("url", urlLocation.toString());
                    params.putString("path", urlLocation.getPath());
                    params.putString("source", unescapeJava(source));
                    getEventEmitter().emit("turbolinksVisitCompleted", params);
                } catch (MalformedURLException e) {
                    Log.e(ReactConstants.TAG, "Error parsing URL. " + e.toString());
                }
            }
        });
    }

    @Override
    public TurbolinksRoute getRoute() {
        return route;
    }

    @Override
    public Boolean getInitialVisit() {
        return initialVisit;
    }

    @Override
    public Boolean getNavigationBarHidden() {
        return navigationBarHidden;
    }

    @JavascriptInterface
    public void postMessage(String message) {
        getEventEmitter().emit("turbolinksMessage", message);
    }
}
