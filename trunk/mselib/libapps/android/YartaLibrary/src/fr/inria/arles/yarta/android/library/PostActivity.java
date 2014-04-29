package fr.inria.arles.yarta.android.library;

import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import fr.inria.arles.iris.R;
import fr.inria.arles.yarta.android.library.web.ImageCache;
import fr.inria.arles.yarta.android.library.web.PostItem;
import fr.inria.arles.yarta.android.library.web.UserItem;
import fr.inria.arles.yarta.android.library.web.WebClient;
import fr.inria.arles.yarta.android.library.util.PullToRefreshListView;
import fr.inria.arles.yarta.android.library.util.JobRunner.Job;

public class PostActivity extends BaseActivity implements
		PullToRefreshListView.OnRefreshListener, PostsListAdapter.Callback,
		CommentAddDialog.Callback {

	public static final String PostGuid = "PostGuid";

	private static final int MENU_ADD = 1;

	private String postGuid;

	private PostsListAdapter adapter;
	private PullToRefreshListView list;

	private WebClient client = WebClient.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_post);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		postGuid = getIntent().getStringExtra(PostGuid);

		adapter = new PostsListAdapter(this);
		adapter.setCallback(this);

		list = (PullToRefreshListView) findViewById(R.id.listComents);
		list.setAdapter(adapter);
		list.setOnRefreshListener(this);

		trackUI("PostView");
	}

	@Override
	protected void onResume() {
		super.onResume();

		refreshUI();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem item = menu.add(0, MENU_ADD, 0, R.string.post_add_comment);
		item.setIcon(R.drawable.icon_add);
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCommentAdded() {
		refreshCommentsList();
	}

	private void onAddComment() {
		CommentAddDialog dlg = new CommentAddDialog(this, postGuid);
		dlg.setCallback(this);
		dlg.setRunner(runner);
		dlg.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD:
			onAddComment();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private List<PostItem> items;

	private void refreshCommentsList() {
		runner.runBackground(new Job() {

			@Override
			public void doWork() {
				items = client.getPostComments(postGuid);
			}

			@Override
			public void doUIAfter() {
				adapter.setItems(items);
				list.onRefreshComplete();
				setListViewHeightBasedOnChildren(list);

				boolean noItems = items.size() == 0;
				list.setVisibility(noItems ? View.GONE : View.VISIBLE);
				findViewById(R.id.listEmpty).setVisibility(
						noItems ? View.VISIBLE : View.GONE);
				new Thread(lazyImageLoader).start();
			}
		});
	}

	public static void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(),
				MeasureSpec.AT_MOST);
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		listView.setLayoutParams(params);
		listView.requestLayout();
	}

	private Runnable lazyImageLoader = new Runnable() {

		@Override
		public void run() {
			for (PostItem item : items) {
				String url = item.getOwner().getAvatarURL();
				if (ImageCache.getDrawable(url) == null) {
					try {
						ImageCache.setDrawable(url,
								ImageCache.drawableFromUrl(url));
						handler.post(refreshListAdapter);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	};

	private Handler handler = new Handler();
	private Runnable refreshListAdapter = new Runnable() {

		@Override
		public void run() {
			adapter.notifyDataSetChanged();
		}
	};

	public void refreshUI() {
		runner.runBackground(new Job() {

			private PostItem item;

			@Override
			public void doWork() {
				item = client.getPost(postGuid);

				if (item != null) {
					if (item.getOwner() != null) {
						String avatarURL = item.getOwner().getAvatarURL();
						if (ImageCache.getDrawable(avatarURL) == null) {
							try {
								ImageCache.setDrawable(avatarURL,
										ImageCache.drawableFromUrl(avatarURL));
							} catch (Exception ex) {
							}
						}
					}
				}
			}

			@Override
			public void doUIAfter() {
				if (item == null) {
					return;
				} else if (item.getOwner() == null) {
					return;
				}
				setCtrlText(R.id.name, Html.fromHtml(item.getOwner().getName()));
				setCtrlText(R.id.title, Html.fromHtml(item.getTitle()));
				setCtrlText(R.id.content, Html.fromHtml(item.getDescription()));
				setCtrlText(R.id.time, getString(R.string.post_time_na));

				ImageView image = (ImageView) findViewById(R.id.icon);
				Drawable drawable = ImageCache.getDrawable(item.getOwner()
						.getAvatarURL());
				if (drawable != null) {
					image.setImageDrawable(drawable);
				} else {
					image.setVisibility(View.GONE);
				}

				refreshCommentsList();
			}
		});
	}

	@Override
	public void onRefresh() {
		refreshCommentsList();
	}

	@Override
	public void onClickProfile(UserItem item) {
		Intent intent = new Intent(this, ProfileActivity.class);
		intent.putExtra(ProfileActivity.UserName, item.getUsername());
		startActivity(intent);
	}

	@Override
	public void onClickPost(PostItem item) {
		// nothing happens, it's a comment
	}
}
