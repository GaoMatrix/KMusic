
package com.gao.kmusic;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.gao.kmusic.adapter.MySongListAdapter;
import com.gao.kmusic.bean.Music;
import com.gao.kmusic.receiver.ScanSdFilesReceiver;
import com.gao.kmusic.service.MediaService;
import com.gao.kmusic.util.HandlerManager;
import com.gao.kmusic.util.MediaUtil;
import com.gao.kmusic.util.PromptManager;

public class MainActivity extends Activity {
    private ListView mSongListView;
    private MySongListAdapter mSongAdapter;

    /************* 音乐控制 ****************/
    private ImageView mPlayPauseImageView;// 播放暂停
    private ImageView mPlayNextImageView;// 播放下一首
    private ImageView mPlayPrevImageView;// 播放上一首
    private ImageView mPlayModeImageView;// 修改播放模式

    private ScanSdFilesReceiver mScanReceiver;
    private ImageView mReflashSongListImageView;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantValue.STARTED:
                    // 开始刷新播放列表界面
                    PromptManager.showProgressDialog(MainActivity.this);
                    break;
                case ConstantValue.FINISHED:
                    // 结束刷新播放列表界面
                    MediaUtil.getInstacen().initMusics(MainActivity.this);
                    PromptManager.closeProgressDialog();
                    mSongAdapter.notifyDataSetChanged();
                    unregisterReceiver(mScanReceiver);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HandlerManager.putHandler(mHandler);
        init();
        setLitener();
    }

    private void setLitener() {
        mPlayPauseImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                switch (MediaUtil.PLAYSTATE) {
                    case ConstantValue.OPTION_PLAY:
                    case ConstantValue.OPTION_CONTINUE:
                        startPlayService(null, ConstantValue.OPTION_PAUSE);// 暂停不需要music的资源信息
                        mPlayPauseImageView.setImageResource(R.drawable.appwidget_pause);
                        break;
                    case ConstantValue.OPTION_PAUSE:
                        if (MediaUtil.CURRENTPOS >= 0
                                && MediaUtil.CURRENTPOS < MediaUtil.getInstacen()
                                        .getSongList().size()) {
                            startPlayService(MediaUtil.getInstacen().getSongList()
                                    .get(MediaUtil.CURRENTPOS),
                                    ConstantValue.OPTION_CONTINUE);// 播放需要music的资源信息
                            mPlayPauseImageView
                                    .setImageResource(R.drawable.img_playback_bt_play);

                        }
                        break;
                }
            }
        });
    }

    private void startPlayService(Music music, int option) {
        Intent intent = new Intent(getApplicationContext(), MediaService.class);
        if (music != null) {
            intent.putExtra("file", music.getPath());
        }
        intent.putExtra("option", option);
        startService(intent);
    }

    private void init() {
        loadSongList();
        initMediaController();
        initReflashSongList();
    }

    /**
     * 刷新播放列表
     */
    private void initReflashSongList() {
        // 刷新列表
        mReflashSongListImageView = (ImageView) findViewById(R.id.title_right);
        mReflashSongListImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                reflash();
            }
        });
    }

    /**
     * 当新插入歌曲文件到sdcard的时候，下面两种方式让系统重新搜索文件，并更新数据库
     */
    public void reflash() {
        // Intent intent = new Intent();
        // intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
        // intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
        // sendBroadcast(intent);

        IntentFilter intentFilter = new IntentFilter(
                Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        mScanReceiver = new ScanSdFilesReceiver();
        registerReceiver(mScanReceiver, intentFilter);
        sendBroadcast(new Intent(
                Intent.ACTION_MEDIA_MOUNTED,
                Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    }

    private void initMediaController() {
        mPlayPauseImageView = (ImageView) findViewById(R.id.imgPlay);
        mPlayPrevImageView = (ImageView) findViewById(R.id.imgPrev);
        mPlayNextImageView = (ImageView) findViewById(R.id.imgNext);

        if (MediaUtil.PLAYSTATE == ConstantValue.OPTION_PAUSE) {
            mPlayPauseImageView.setImageResource(R.drawable.appwidget_pause);
        }
    }

    private void loadSongList() {
        MediaUtil.getInstacen().initMusics(getApplicationContext());// 在手机的多媒体数据库中查询声音
        mSongAdapter = new MySongListAdapter(getApplicationContext());
        mSongListView = (ListView) findViewById(R.id.play_list);
        mSongListView.setAdapter(mSongAdapter);

        // new InitDataTask().execute();//线程池，如操作的线程过多，等待情况
        new InitDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);// 不用等待
    }

    /**
     * 音乐资源过多
     */
    class InitDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            PromptManager.showProgressDialog(MainActivity.this);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // 加载多媒体信息
            MediaUtil.getInstacen().initMusics(MainActivity.this);
            // SystemClock.sleep(100);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            PromptManager.closeProgressDialog();
            mSongAdapter.notifyDataSetChanged();
        }
    }
}
