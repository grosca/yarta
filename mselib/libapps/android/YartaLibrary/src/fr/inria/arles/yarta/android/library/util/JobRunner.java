package fr.inria.arles.yarta.android.library.util;

import fr.inria.arles.yarta.android.library.ui.BaseActivity;
import android.os.AsyncTask;

public class JobRunner {

	public static abstract class Job {
		public void doWork() {
		}

		public void doUIBefore() {
		}

		public void doUIAfter() {
		}
	}

	public JobRunner(BaseActivity parent) {
		this.parent = parent;
	}

	public void run(int messageStringId, Job job) {
		ProgressDialog dialog = ProgressDialog.show(parent,
				parent.getString(messageStringId));
		new AsyncRunnerTask(job).execute(dialog);
	}

	public void runBackground(Job job) {
		new AsyncRunnerTask(job).execute();
	}

	private BaseActivity parent;

	private class AsyncRunnerTask extends AsyncTask<Object, Integer, Long> {

		private Job job;

		public AsyncRunnerTask(Job job) {
			super();
			this.job = job;
		}

		@Override
		protected void onPreExecute() {
			job.doUIBefore();
			parent.showProgress();
		}

		@Override
		protected Long doInBackground(Object... params) {
			if (params.length > 0) {
				dialog = (ProgressDialog) params[0];
			}
			job.doWork();
			return 0L;
		}

		@Override
		protected void onPostExecute(Long result) {
			parent.hideProgress();
			try {
				dialog.dismiss();
				dialog = null;
			} catch (Exception ex) {
				// do nothing
			}

			try {
				job.doUIAfter();
			} catch (IllegalStateException ex) {
				// activity not attached anymore;
			}
		}

		private ProgressDialog dialog;
	}
}
