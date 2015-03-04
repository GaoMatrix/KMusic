
package com.gao.kmusic;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.gao.kmusic.adapter.MySongListAdapter;
import com.gao.kmusic.util.MediaUtil;

public class MainActivity extends Activity {
    private ListView songListView;
    private MySongListAdapter songAdapter;

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
        songAdapter = new MySongListAdapter(getApplicationContext());
        songListView = (ListView) findViewById(R.id.play_list);
        songListView.setAdapter(songAdapter);
    }

}
