package com.gao.kmusic.util;

import android.os.Handler;

/**
 * Handler在Activity中创建是要给别人用的，给Receiver和Service使用，例如播放完了改界面上的东西，
 * 依然要拿到Handler信息进行操作的，可以把他Handler做成全局的Global的信息进行倒腾，也可以
 * 变成static的来回传递，下面使用的是线程一级的变量共享ThreadLocal来实现，无论是Activity，Receiver
 * 还是Service都是在UIThread里面跑的。所以在Activity里面将Handler存起来，当在Receiver，和Service
 * 里面要使用UIThread里面的Handler的时候就可以拿到这个Handler
 * @author GaoMatrix
 *
 */
public class HandlerManager {
	private static ThreadLocal<Handler> threadLocal = new ThreadLocal<Handler>();

	public static Handler getHandler() {
		return threadLocal.get();
	}

	public static void putHandler(Handler value) {
		threadLocal.set(value);//UiThread  id， Receiver和Service也都是泡在UI Thread里面
	}
}
