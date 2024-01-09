package com.msbte.msbtescholar;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.msbte.msbtescholar.R;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    String websiteURL = "https://www.msbtescholar.com"; // sets web url
    private WebView webview;
    SwipeRefreshLayout mySwipeRefreshLayout;
    private Stack<String> pageHistory = new Stack<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!CheckNetwork.isInternetAvailable(this)) {
            // Handle no internet connection
            showNoInternetDialog();
        } else {
            initializeWebView();
        }

        // Swipe to refresh functionality
        mySwipeRefreshLayout = findViewById(R.id.swipeContainer);

        mySwipeRefreshLayout.setOnRefreshListener(
                () -> webview.reload()
        );
    }

    private void initializeWebView() {
        // Webview stuff
        webview = findViewById(R.id.webView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webview.loadUrl(websiteURL);
        webview.setWebViewClient(new WebViewClientDemo());
        pageHistory.push(websiteURL); // Save the initial URL in history
    }

    private void showNoInternetDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No internet connection available")
                .setMessage("Please Check your Mobile data or Wifi network.")
                .setPositiveButton("Ok", (dialog, which) -> finish())
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webview.canGoBack()) {
            webview.goBack();
            pageHistory.pop(); // Remove the current page from history
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private class WebViewClientDemo extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("http:") || url.startsWith("https:")) {
                view.loadUrl(url);
                return true;
            } else if (url.startsWith("mailto:") || url.startsWith("tel:") || url.startsWith("geo:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                view.getContext().startActivity(intent);
                return true;
            } else if (url.startsWith("whatsapp:")) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_VIEW);
                sendIntent.setData(Uri.parse(url));
                view.getContext().startActivity(sendIntent);
                return true;
            } else if (url.startsWith("intent://instagram.com/")) {
                try {
                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                    view.getContext().startActivity(intent);
                } catch (Exception e) {
                    // Handle the exception
                }
                return true;
            } else {
                // Handle other custom URL schemes or unsupported URLs here
                return super.shouldOverrideUrlLoading(view, url);
            }
        }
    }
    static class CheckNetwork {
        private static final String TAG = CheckNetwork.class.getSimpleName();

        public static boolean isInternetAvailable(Context context) {
            NetworkInfo info = (NetworkInfo) ((ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

            return info != null && info.isConnected();
        }
    }
}
