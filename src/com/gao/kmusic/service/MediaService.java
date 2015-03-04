package com.gao.kmusic.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.gao.kmusic.ConstantValue;
import com.gao.kmusic.util.MediaUtil;
import com.gao.kmusic.util.PromptManager;

public class MediaService extends Service implements OnCompletionListener, OnSeekCompleteListener, OnErrorListener {

	// mediaplayer
	private static MediaPlayer player; //设置成静态的防止多个MediaPlayer
	// private static ProgressThread thread;
	private static ProgressTask task;
	private String file;
	private int postion = 0;

	// private Handler handler;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (player == null) {
			player = new MediaPlayer();
			player.setOnSeekCompleteListener(this);
			player.setOnCompletionListener(this);
			player.setOnErrorListener(this);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		int option = intent.getIntExtra("option", -1);
		int progress = intent.getIntExtra("progress", -1);
		if (progress != -1) {
			this.postion = progress;
			// this.postion = progress * player.getDuration() / 100;
		}
		switch (option) {
			case ConstantValue.OPTION_PLAY:
				file = intent.getStringExtra("file");
				play(file);
				MediaUtil.PLAYSTATE = option;
				break;
			case ConstantValue.OPTION_PAUSE:
				postion = player.getCurrentPosition();
				pause();
				MediaUtil.PLAYSTATE = option;
				break;
			case ConstantValue.OPTION_CONTINUE:
				playerToPosiztion(postion);
				// if (MediaUtil.PLAYSTATE == ConstantValue.OPTION_PLAY)
				if (file == "" || file == null) {
					file = intent.getStringExtra("file");
					play(file);
				} else {
					player.start();
				}
				MediaUtil.PLAYSTATE = option;
				break;
			case ConstantValue.OPTION_UPDATE_PROGESS:
				playerToPosiztion(postion);
				break;
		}
	}

	@Override
	public void onDestroy() {
		stop();
		super.onDestroy();
	}

	private void play(String path) {

		if (player == null) {
			player = new MediaPlayer();
		}

		try {
			player.reset();
			player.setDataSource(path);
			player.prepare();
			player.start();

			/*
			 * Message msg = Message.obtain(); msg.what = ConstantValue.SEEKBAR_MAX; msg.arg1 = player.getDuration();
			 * 
			 * HandlerManager.getHandler().sendMessage(msg);
			 */
			/*
			 * if (thread == null) { thread = new ProgressThread(); thread.start(); }
			 */
			if (task == null) {
				task = new ProgressTask();
				task.execute();
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private void pause() {
		if (player != null && player.isPlaying()) {
			player.pause();
		}
	}

	private void stop() {
		if (player != null) {
			player.stop();
			player.release();
		}
	}

	private void playerToPosiztion(int posiztion) {

		if (posiztion > 0 && posiztion < player.getDuration()) {
			player.seekTo(posiztion);
		}
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		//HandlerManager.getHandler().sendEmptyMessage(ConstantValue.PLAY_END);
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {

		if (player.isPlaying()) {
			player.start();
		}
	}

	private static final String TAG = "MediaService";

	// private class ProgressThread extends Thread {
	//
	// @Override
	// public void run() {
	// while (true) {
	// Log.i(TAG, "isPlaying:" + player.isPlaying());
	//
	// SystemClock.sleep(1000);
	// }
	// }
	// }
	private class ProgressTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			while (true) {
				Log.i(TAG, "isPlaying:" + player.isPlaying());
				SystemClock.sleep(1000);
				publishProgress();
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			if (player.isPlaying()) {
				Message msg = Message.obtain();
				msg.what = ConstantValue.SEEKBAR_CHANGE;
				msg.arg1 = player.getCurrentPosition() + 1000;
				msg.arg2 = player.getDuration();
//				HandlerManager.getHandler().sendMessage(msg);
			}
			super.onProgressUpdate(values);
		}

	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		PromptManager.showToast(getApplicationContext(), "亲，音乐文件加载出错了");
		return false;
	}

}
