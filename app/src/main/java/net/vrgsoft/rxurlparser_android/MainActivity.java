package net.vrgsoft.rxurlparser_android;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.vrgsoft.library.LinkCrawler;
import net.vrgsoft.library.Result;
import net.vrgsoft.rxurlparser_android.databinding.ActivityMainBinding;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        LinkCrawler crawler = new LinkCrawler();
        crawler.parseUrl("https://github.com/")
                .subscribe(new Consumer<Result>() {
                    @Override
                    public void accept(@NonNull Result result) throws Exception {
                        mainBinding.setContent(result.getmParseContent());
                    }
                });
    }
}
