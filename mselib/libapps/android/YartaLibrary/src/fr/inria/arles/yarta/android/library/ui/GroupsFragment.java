package fr.inria.arles.yarta.android.library.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import fr.inria.arles.iris.R;
import fr.inria.arles.yarta.android.library.resources.Group;
import fr.inria.arles.yarta.android.library.resources.Person;
import fr.inria.arles.yarta.android.library.util.BaseFragment;
import fr.inria.arles.yarta.android.library.util.PullToRefreshListView;
import fr.inria.arles.yarta.android.library.util.JobRunner.Job;

public class GroupsFragment extends BaseFragment implements
		PullToRefreshListView.OnRefreshListener,
		AdapterView.OnItemClickListener {

	private GroupsListAdapter adapter;
	private PullToRefreshListView list;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View root = inflater
				.inflate(R.layout.fragment_groups, container, false);

		adapter = new GroupsListAdapter(getSherlockActivity());

		list = (PullToRefreshListView) root.findViewById(R.id.listGroups);
		list.setAdapter(adapter);
		list.setOnRefreshListener(this);
		list.setOnItemClickListener(this);
		list.setEmptyView(root.findViewById(R.id.listEmpty));

		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshUI(null);
	}

	@Override
	public void refreshUI(String notification) {
		execute(new Job() {

			private List<Group> items;

			@Override
			public void doWork() {
				items = new ArrayList<Group>();

				try {
					Person person = sam.getMe();

					for (fr.inria.arles.yarta.resources.Group group : person
							.getIsMemberOf()) {
						items.add((Group) sam.getResourceByURI(group
								.getUniqueId()));
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public void doUIAfter() {
				adapter.setItems(items);
				list.onRefreshComplete();
			}
		});
	}

	@Override
	public void onRefresh() {
		refreshUI(null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Group item = (Group) adapter.getItem(position);
		Intent intent = new Intent(getSherlockActivity(), GroupActivity.class);
		intent.putExtra(GroupActivity.GroupGuid, item.getUniqueId());
		startActivity(intent);
	}
}
