package hr.ifkinaklet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiModeManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.EditText;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView web;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        UiModeManager um = (UiModeManager) getSystemService(UI_MODE_SERVICE);
        boolean tv = um != null
                && um.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        web.setBackgroundColor(0xFF2A0F1A);
        setContentView(web);

        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        web.addJavascriptInterface(new Bridge(), "Android");
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(
                    WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                (dialog, which) -> result.confirm())
                        .setOnCancelListener(dialog -> result.cancel())
                        .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(
                    WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                (dialog, which) -> result.confirm())
                        .setNegativeButton(android.R.string.cancel,
                                (dialog, which) -> result.cancel())
                        .setOnCancelListener(dialog -> result.cancel())
                        .show();
                return true;
            }

            @Override
            public boolean onJsPrompt(
                    WebView view, String url, String message,
                    String defaultValue, JsPromptResult result) {
                EditText input = new EditText(MainActivity.this);
                input.setText(defaultValue);

                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(message)
                        .setView(input)
                        .setPositiveButton(android.R.string.ok,
                                (dialog, which) ->
                                        result.confirm(input.getText().toString()))
                        .setNegativeButton(android.R.string.cancel,
                                (dialog, which) -> result.cancel())
                        .setOnCancelListener(dialog -> result.cancel())
                        .show();
                return true;
            }
        });

        web.setWebViewClient(new WebViewClient());
        web.loadUrl("file:///android_asset/index.html" + (tv ? "?tv=1" : ""));
    }

    @Override
    public void onBackPressed() {
        web.evaluateJavascript("(window.goBack ? goBack() : false)", value -> {
            if (!"true".equals(value)) {
                finish();
            }
        });
    }

    private class Bridge {
        @JavascriptInterface
        public void share(String name, String text) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, name);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            startActivity(Intent.createChooser(intent, name));
        }
    }
}
