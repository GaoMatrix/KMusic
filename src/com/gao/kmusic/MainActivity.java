
package com.gao.kmusic;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import com.gao.kmusic.adapter.MySongListAdapter;
import com.gao.kmusic.util.MediaUtil;
import com.gao.kmusic.util.PromptManager;

public class MainActivity extends Activity {
    private ListView mSongListView;
    private MySongListAdapter mSongAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        loadSongList();
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
