package fr.inria.arles.yarta.android.library.ui;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import fr.inria.arles.iris.R;
import fr.inria.arles.iris.web.RequestItem;
import fr.inria.arles.iris.web.UserItem;
import fr.inria.arles.yarta.android.library.util.BaseFragment;
import fr.inria.arles.yarta.android.library.util.ImageCache;
import fr.inria.arles.yarta.android.library.util.PullToRefreshListView;
import fr.inria.arles.yarta.android.library.util.JobRunner.Job;

public class RequestsFragment extends BaseFragment implements
		PullToRefreshListView.OnRefreshListener, RequestsListAdapter.Callback {

	public interface Callback {
		public void onRefresh(List<RequestItem> requests);
	}

	private PullToRefreshListView list;
	private RequestsListAdapter adapter;
	private Callback callback;

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_requests, container,
				false);

		adapter = new RequestsListAdapter(getSherlockActivity());
		adapter.setCallback(this);

		list = (PullToRefreshListView) root.findViewById(R.id.listRequests);
		list.setAdapter(adapter);
		list.setOnRefreshListener(this);
		list.setEmptyView(root.findViewById(R.id.listEmpty));
		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshUI(null);
	}

	private List<RequestItem> items;

	@Override
	public void refreshUI(String notification) {
		execute(new Job() {

			@Override
			public void doWork() {
				items = getRequests();
			}

			@Override
			public void doUIAfter() {
				if (callback != null) {
					callback.onRefresh(items);
				}
				adapter.setItems(items);
				list.onRefreshComplete();
				new Thread(lazyImageLoader).start();
			}
		});
	}

	@Override
	public void onRefresh() {
		refreshUI(null);
	}

	public List<RequestItem> getRequests() {
		return client.getUserRequests();
	}

	private Handler handler = new Handler();
	private Runnable refreshListAdapter = new Runnable() {

		@Override
		public void run() {
			adapter.notifyDataSetChanged();
		}
	};

	private Runnable lazyImageLoader = new Runnable() {

		@Override
		public void run() {
			for (RequestItem item : items) {
				ImageCache.getBitmap(item.getUser());
				handler.post(refreshListAdapter);
			}
		}
	};

	@Override
	public void onAccept(final RequestItem item) {
		execute(new Job() {
			@Override
			public void doWork() {
				client.acceptRequest(item);
			}

			@Override
			public void doUIAfter() {
				refreshUI(null);
			}
		});
	}

	@Override
	public void onIgnore(final RequestItem item) {
		execute(new Job() {
			@Override
			public void doWork() {
				client.ignoreRequest(item);
			}

			@Override
			public void doUIAfter() {
				refreshUI(null);
			}
		});
	}

	@Override
	public void onProfile(UserItem item) {
		Intent intent = new Intent(getSherlockActivity(), ProfileActivity.class);
		intent.putExtra(ProfileActivity.UserName, item.getUsername());
		startActivity(intent);
	}
}
