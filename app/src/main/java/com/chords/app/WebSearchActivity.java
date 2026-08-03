package com.chords.app;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

public class WebSearchActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_search);

        webView = findViewById(R.id.webViewSearch);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // חובה כדי לאפשר לאתרים לפעול כמו שצריך
        webSettings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());
        
        // טעינת עמוד חיפוש התחלתי (לדוגמה Google או מנוע חיפוש לבחירתך)
        webView.loadUrl("https://www.google.com");
    }

    @Override
    public void onBackPressed() {
        // אם המשתמש לחץ חזרה, נאפשר לו לחזור עמוד אחורה ב-WebView במקום לצאת מהמסך מיד
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
