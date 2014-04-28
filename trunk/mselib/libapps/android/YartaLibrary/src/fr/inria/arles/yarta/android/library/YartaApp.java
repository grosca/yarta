package fr.inria.arles.yarta.android.library;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.inria.arles.iris.R;
import fr.inria.arles.yarta.middleware.communication.CommunicationManager;
import fr.inria.arles.yarta.middleware.msemanagement.MSEApplication;
import fr.inria.arles.yarta.middleware.msemanagement.MSEManager;
import fr.inria.arles.yarta.middleware.msemanagement.StorageAccessManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;

/**
 * Basic Yarta Application which handles all MSE functionality (client).
 */
public class YartaApp extends Application implements MSEApplication {

	/**
	 * This is the UI Observer interface which should be implemented by those
	 * who want real time updates over UI data.
	 */
	public interface Observer {
		public void updateInfo();
	}

	private MSEManager mse;
	private CommunicationManager comm;
	private StorageAccessManager sam;

	private List<Observer> observers = new ArrayList<Observer>();

	/**
	 * In case it's the very first time, copy the base rdf & policy to the
	 * specified folder.
	 */
	private void ensureBaseFiles(Context context) {
		String dataPath = getFilesDir().getAbsolutePath();
		String baseOntologyFilePath = getString(R.string.service_baseRDF);
		String basePolicyFilePath = getString(R.string.service_basePolicy);

		dumpAsset(context, dataPath, baseOntologyFilePath);
		dumpAsset(context, dataPath, basePolicyFilePath);
	}

	/**
	 * Dumps an asset in the specified folder.
	 */
	private void dumpAsset(Context context, String folder, String fileName) {
		try {
			InputStream fin = context.getAssets().open(fileName,
					AssetManager.ACCESS_RANDOM);
			FileOutputStream fout = new FileOutputStream(folder + "/"
					+ fileName);

			int count = 0;
			byte buffer[] = new byte[4096];

			while ((count = fin.read(buffer)) != -1) {
				fout.write(buffer, 0, count);
			}

			fin.close();
			fout.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void initMSE(Observer observer) {
		if (observer != null) {
			addObserver(observer);
		}

		ensureBaseFiles(this);

		String dataPath = getFilesDir().getAbsolutePath();
		String baseOntologyFilePath = getString(R.string.service_baseRDF);
		String basePolicyFilePath = getString(R.string.service_basePolicy);

		if (mse == null) {
			mse = new MSEManager();

			try {
				mse.initialize(dataPath + "/" + baseOntologyFilePath, dataPath
						+ "/" + basePolicyFilePath, this, this);
			} catch (Exception ex) {
				ex.printStackTrace();
				mse = null;
			}
		} else {
			if (observer != null) {
				observer.updateInfo();
			} else {
				// no observer, but initialized already
				// so we might call it manually
				handleKBReady(mse.getOwnerUID());
			}
		}
	}

	public void uninitMSE() {
		try {
			mse.shutDown();
		} catch (Exception ex) {
		} finally {
			mse = null;
		}
	}

	@Override
	public void handleKBReady(String userId) {
		if (userId != null && userId.length() > 0) {
			comm = mse.getCommunicationManager();
			sam = mse.getStorageAccessManager();

			mse.setOwnerUID(userId);
			sam.setOwnerID(userId);

			notifyAllObservers();

			startMainActivity();
		} else {
			uninitMSE();
		}
	}

	/**
	 * Starts the main activity of the app;
	 */
	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	@Override
	public void onTerminate() {
		uninitMSE();
		super.onTerminate();
	}

	public void addObserver(Observer observer) {
		if (!observers.contains(observer)) {
			observers.add(observer);
		}
	}

	public void removeObserver(Observer observer) {
		observers.remove(observer);
	}

	private void notifyAllObservers() {
		for (Observer observer : observers) {
			observer.updateInfo();
		}
	}

	public void sendNotify(final String peerId) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				getCOMM().sendNotify(peerId);
			}
		}).start();
	}

	public MSEManager getMSE() {
		return mse;
	}

	public StorageAccessManager getSAM() {
		return sam;
	}

	public CommunicationManager getCOMM() {
		return comm;
	}

	@Override
	public void handleNotification(String notification) {
		notifyAllObservers();
	}

	@Override
	public boolean handleQuery(String query) {
		return true;
	}

	@Override
	public String getAppId() {
		return "fr.inria.arles.yarta";
	}

	// TODO: login workaround
	private Observer loginObserver;

	/**
	 * This is called from login activity.
	 * 
	 * @param userId
	 */
	public void onLogin(String userId) {
		if (loginObserver != null) {
			loginObserver.updateInfo();
		}
	}

	/**
	 * The service will set this before starting the login activity.
	 * 
	 * @param observer
	 */
	public void setLoginObserver(Observer observer) {
		this.loginObserver = observer;
	}
}
