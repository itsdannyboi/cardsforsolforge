package com.solforge.carddbforsolforge;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class CardSearchFrag extends Fragment implements SortByDialog.sortByDialogListener {

    private static final String TAG = "Card Search Frag";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    private LayoutManagerType currentLayoutManagerType;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RVAdapter adapter;

    private List<Card> cards;

    private SwitchCompat switchCompat;

    private int sortByItemSelected = 0;

    public static CardSearchFrag newInstance() {
        CardSearchFrag fragment = new CardSearchFrag();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        cards = getCards();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_search, container, false);
        rootView.setTag(TAG);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv);

        layoutManager = new LinearLayoutManager(getActivity());

        currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type
            currentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(currentLayoutManagerType);

        adapter = new RVAdapter(cards, getActivity());
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.card_search_menu, menu);
        MenuItem item = menu.findItem(R.id.nav_switch);
        item.setActionView(R.layout.action_view_switch);
        RelativeLayout relativeLayout = (RelativeLayout) MenuItemCompat.getActionView(item);
        switchCompat = (SwitchCompat) relativeLayout.findViewById(R.id.action_bar_switch);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Change layout between grid and linear
                setRecyclerViewLayoutManager(isChecked
                        ? LayoutManagerType.GRID_LAYOUT_MANAGER
                        : LayoutManagerType.LINEAR_LAYOUT_MANAGER);

                // Change text size so that card names are smaller for grid
                //((TextView) getActivity().findViewById(R.id.card_name)).setTextSize(isChecked
                //        ? R.dimen.grid_layout_text_size : R.dimen.linear_layout_text_size);
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // case for settings
                return true;
            case R.id.action_filter:
                // case for filter
                return true;
            case R.id.action_sort:
                DialogFragment dialogFragment = SortByDialog.newInstance(sortByItemSelected);
                dialogFragment.setTargetFragment(CardSearchFrag.this, 300);
                dialogFragment.show(getFragmentManager(), "SortByDialog");
                return true;
            default:
                // if we get here, the user's action was not recognized
                // using superclass
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save currently selected layout manager
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, currentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    public void setRecyclerViewLayoutManager (LayoutManagerType layoutManagerType) {
        int scrollPosition = 0;

        // If a layout manager has already been set, get current scroll position
        if (recyclerView.getLayoutManager() != null) {
            scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                    .findFirstCompletelyVisibleItemPosition();
        }

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                layoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                currentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            case LINEAR_LAYOUT_MANAGER:
                layoutManager = new LinearLayoutManager(getActivity());
                currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
                break;
            default:
                layoutManager = new LinearLayoutManager(getActivity());
                currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.scrollToPosition(scrollPosition);
    }

    public List<Card> getCards() {
        List<Card> temp = new ArrayList<>();
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(loadJSON());
            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (!(obj.getString("rarity").equals("Token"))) {
                    temp.add(new Card(obj));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        Collections.sort(temp);
        return temp;
    }

    public String loadJSON() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("cardDB.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onClickSortMethod (int i) {
        sortByItemSelected = i;
        adapter.sortAndUpdateData(i);
    }

    public static void startEXCV (List<Card> cards, int position) {
        EXCVFragment excvFragment = EXCVFragment.newInstance(cards.get(position));
        // TODO: figure out how to start this fragment
    }
}
