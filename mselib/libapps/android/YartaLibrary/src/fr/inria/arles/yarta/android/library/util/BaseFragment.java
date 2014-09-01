package fr.inria.arles.yarta.android.library.util;

import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import fr.inria.arles.iris.web.ElggClient;
import fr.inria.arles.yarta.android.library.ContentClientPictures;
import fr.inria.arles.yarta.android.library.msemanagement.StorageAccessManagerEx;

public abstract class BaseFragment extends SherlockFragment {

	protected JobRunner runner;
	protected StorageAccessManagerEx sam;
	protected ContentClientPictures contentClient;

	protected ElggClient client = ElggClient.getInstance();

	public void setRunner(JobRunner runner) {
		this.runner = runner;
	}

	public void setSAM(StorageAccessManagerEx sam) {
		this.sam = sam;
	}

	public void setContentClient(ContentClientPictures contentClient) {
		this.contentClient = contentClient;
	}

	public abstract void refreshUI();

	protected String getCtrlText(int txtId) {
		TextView txt = (TextView) getView().findViewById(txtId);
		if (txt != null) {
			return txt.getText().toString();
		}
		return null;
	}

	protected void setCtrlText(int txtId, Spanned text) {
		if (getView() != null) {
			TextView txt = (TextView) getView().findViewById(txtId);
			if (txt != null) {
				txt.setText(text);
			}
		}
	}

	protected void setCtrlText(int txtId, String text) {
		if (getView() != null) {
			TextView txt = (TextView) getView().findViewById(txtId);
			if (txt != null) {
				txt.setText(text);
			}
		}
	}

	protected void setFocusable(int viewId, boolean focusable) {
		if (getView() != null) {
			getView().findViewById(viewId).setFocusable(focusable);
		}
	}

	protected void setCtrlVisibility(int ctrlId, int visibility) {
		if (getView() != null) {
			View v = getView().findViewById(ctrlId);
			if (v != null) {
				v.setVisibility(visibility);
			}
		}
	}
}
