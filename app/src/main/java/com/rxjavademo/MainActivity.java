package com.rxjavademo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Log.e("wbb", "currentThread: " + Thread.currentThread().getName());
        // 被观察者，上游
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                Log.e("wbb", "currentThread: " + Thread.currentThread().getName());
                // 处理数据
                e.onNext("处理数据");
                e.onComplete(); // 调用成功
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())  //回到主线程去处理请求注册结果
                .doOnNext(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.e("wbb", "doOnNext:完成数据处理，主线程更新UI " + s);
                    }
                })
                .observeOn(Schedulers.io())
//                .map(new Function<String, Integer>() { // 一个变量变成另一个变量
//                    @Override
//                    public Integer apply(String s) throws Exception {
//                        Log.e("wbb", "对上面的数据继续处理，子线程，map: " + Thread.currentThread().getName());
//
//                        return 1000;
//                    }
//                })
                .flatMap(new Function<String, ObservableSource<Integer>>() {
                    @Override
                    public ObservableSource<Integer> apply(String s) throws Exception {
                        List<Integer> list = new ArrayList<Integer>();
                        list.add(101);
                        return Observable.fromIterable(list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() { // 观察者，下游
                    @Override
                    public void onSubscribe(Disposable d) {
                        // 开始运行前
                        Log.e("wbb", "onSubscribe: " + Thread.currentThread().getName());
                        compositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(Integer value) {
                        Log.e("wbb", "处理数据完成：主线程 value: " + value+" currentThread: "+Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("wbb", "onError: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.e("wbb", "onComplete: " + Thread.currentThread().getName());
                    }
                });
    }
}
