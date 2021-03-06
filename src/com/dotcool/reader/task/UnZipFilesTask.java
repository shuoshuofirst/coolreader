package com.dotcool.reader.task;

import java.io.IOException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dotcool.reader.LNReaderApplication;
import com.dotcool.R;
import com.dotcool.reader.callback.CallbackEventData;
import com.dotcool.reader.callback.ICallbackEventData;
import com.dotcool.reader.callback.ICallbackNotifier;
import com.dotcool.reader.helper.Util;

public class UnZipFilesTask extends AsyncTask<Void, ICallbackEventData, Void> implements ICallbackNotifier {
	private static final String TAG = UnZipFilesTask.class.toString();
	private final String zipName;
	private final String rootPath;
	private ICallbackNotifier callback;
	private String source;
	private boolean hasError = false;

	public static UnZipFilesTask instance;

	public static UnZipFilesTask getInstance() {
		return instance;
	}

	public static UnZipFilesTask getInstance(String zipName, String rootPath, ICallbackNotifier callback, String source) {
		if (instance == null || instance.getStatus() == Status.FINISHED) {
			instance = new UnZipFilesTask(zipName, rootPath, callback, source);
		}
		else {
			instance.setCallback(callback, source);
		}
		return instance;
	}

	public void setCallback(ICallbackNotifier callback, String source) {
		this.callback = callback;
		this.source = source;
	}

	private UnZipFilesTask(String zipName, String rootPath, ICallbackNotifier callback, String source) {
		this.zipName = zipName;
		this.rootPath = rootPath;
		this.callback = callback;
		this.source = source;
	}

	public void onCallback(ICallbackEventData message) {
		publishProgress(message);
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		Context ctx = LNReaderApplication.getInstance().getApplicationContext();
		// unzip the files
		try {
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.unzip_files_task_progress)));
			Util.unzipFiles(zipName, rootPath, this);
		} catch (IOException e) {
			Log.e(TAG, "Failed to unzip files.", e);
			publishProgress(new CallbackEventData(ctx.getResources().getString(R.string.unzip_files_task_error, e.getMessage())));
			hasError = true;
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(ICallbackEventData... values) {
		Log.d(TAG, values[0].getMessage());
		if (callback != null)
			callback.onCallback(new CallbackEventData(values[0].getMessage(), source));
	}

	@Override
	protected void onPostExecute(Void result) {
		if (!hasError) {
			String message = LNReaderApplication.getInstance().getApplicationContext().getResources().getString(R.string.unzip_files_task_complete, zipName, rootPath);
			Log.d(TAG, message);
			if (callback != null)
				callback.onCallback(new CallbackEventData(message, source));
		}
	}
}
