package com.cm.media.viewmodel;

import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.cm.media.entity.vod.VodDetail;
import com.cm.media.entity.vod.parse.ResolvedStream;
import com.cm.media.entity.vod.parse.ResolvedVod;
import com.cm.media.repository.RemoteRepo;
import com.cm.media.util.CollectionUtils;
import com.cm.media.util.UrlParser;
import com.cm.media.util.WebViewVodParser;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.text.StringsKt;
import org.json.JSONException;

import java.util.List;

public class PlayerViewModel extends ViewModel {
    private final MutableLiveData<VodDetail> vodDetailLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> playingUrlLiveData = new MutableLiveData<>();

    public MutableLiveData<VodDetail> getVodDetailLiveData() {
        return vodDetailLiveData;
    }

    public MutableLiveData<String> getPlayingUrlLiveData() {
        return playingUrlLiveData;
    }

    public void start(ViewGroup group, int videoId) {
        Disposable disposable = RemoteRepo.getInstance().getRxVodDetail(videoId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vodDetailEntity -> {
                    if (vodDetailEntity != null && vodDetailEntity.getData() != null) {
                        VodDetail detail = vodDetailEntity.getData();
                        vodDetailLiveData.setValue(detail);
                        if (detail != null && !detail.getPlays().isEmpty()) {
                            Log.i("url", "source:" + detail.getSource());
                            Log.i("url", "plays:" + CollectionUtils.stringValue(detail.getPlays()));
                            String playUrl = detail.getPlays().get(0).getPlayUrl();
                            onProcessPlayUrl(group, detail.getSource(), playUrl);
                        }
                    }
                }, throwable -> {

                });
    }

    private void onProcessPlayUrl(ViewGroup group, int source, String url) throws JSONException {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (source == 0) {
            long time = System.currentTimeMillis() / 1000;
            String finalUrl = url + "?" + "stTime=" + time + "&token=" + UrlParser.getToken(time, url);
            playingUrlLiveData.setValue(finalUrl);
        } else if (source == 7) {
            Disposable disposable = RemoteRepo.getInstance().resolveRxVCinemaUrl(url)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(resolvedVodEntity -> {
                        Log.i("url", "resolve:" + resolvedVodEntity);
                        if (resolvedVodEntity == null) {
                            return;
                        }
                        ResolvedVod vod = resolvedVodEntity.getData();
                        List<ResolvedStream> streamList = vod.getStreams();
                        if (CollectionUtils.isEmptyList(streamList)) {
                            return;
                        }
                        playingUrlLiveData.setValue(streamList.get(0).getPlay_list().get(0).getUrl());
                    }, throwable -> Log.e("error", "error!", throwable));

        } else if (source != 8 && !url.contains("m3u")) {
            String parseUrl = "http://www.itono.cn/vip/xlyy.php?url=" + (StringsKt.split(url, new String[]{"?"}, false, 6).get(0));
//            ParseWebUrlHelper parseWebUrlHelper = ParseWebUrlHelper.getInstance().init(activity, parseUrl);
//            parseWebUrlHelper.setOnParseListener(new ParseWebUrlHelper.OnParseWebUrlListener() {
//                @Override
//                public void onFindUrl(String url) {
//                    Log.d("webUrl", url);
//                    playingUrlLiveData.postValue(url);
//                    //*****处理代码
//                }
//
//                @Override
//                public void onError(String errorMsg) {
//                    Log.d("error", errorMsg);
//                    //****出错监听
//                }
//            });
//            parseWebUrlHelper.startParse();

            WebViewVodParser parser = new WebViewVodParser(group, parseUrl);
            parser.start(new WebViewVodParser.ParseResultCallback()

            {
                @Override
                public void onSuccess(String ret) {
                    Log.e("playUrl:", ret);
                    playingUrlLiveData.setValue(ret);
                }

                @Override
                public void onFailed(String msg) {
                    Log.e("error", "parseError," + msg);
                }
            });
        } else {
            playingUrlLiveData.setValue(url);
        }
    }
}