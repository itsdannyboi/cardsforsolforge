package com.solforge.carddbforsolforge;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class CardSearchFrag extends Fragment implements SortByDialog.sortByDialogListener,
        FilterDialogFragment.FilterDialogListener {

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
    private TextView emptyView;
    private ToggleButton toggleButton;

    private List<Card> cards;
    private int sortByItemSelected = 0;
    private HashMap<String, Boolean> selectedFilters;

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
        initSelected();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_search, container, false);
        rootView.setTag(TAG);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv);
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);

        layoutManager = new LinearLayoutManager(getActivity());

        currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;

        if (savedInstanceState != null) {
            // Restore saved layout manager type
            currentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
        }
        setRecyclerViewLayoutManager(currentLayoutManagerType);

        adapter = new RVAdapter(cards, getActivity(), CardSearchFrag.this);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.card_search_menu, menu);
        MenuItem toggle = menu.findItem(R.id.nav_switch);
        toggle.setActionView(R.layout.action_view_switch);
        RelativeLayout relativeLayout = (RelativeLayout) MenuItemCompat.getActionView(toggle);
        toggleButton = (ToggleButton) relativeLayout.findViewById(R.id.toggle_layout);
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Change layout between grid and linear
                setRecyclerViewLayoutManager(isChecked
                        ? LayoutManagerType.GRID_LAYOUT_MANAGER
                        : LayoutManagerType.LINEAR_LAYOUT_MANAGER);
            }
        });

        /*MenuItem search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query != null) {
                    final List<Card> searchedCards = search(cards, query);
                    adapter.animateTo(searchedCards);
                    if (!isDataEmpty(searchedCards)) {
                        recyclerView.scrollToPosition(0);
                    }
                    return true;
                }
                return false;
            }
        });*/

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
                DialogFragment filterFrag = FilterDialogFragment.newInstance(selectedFilters);
                filterFrag.setTargetFragment(CardSearchFrag.this, 300);
                filterFrag.show(this.getFragmentManager(), "FilterDialog");
                return true;
            case R.id.action_sort:
                DialogFragment sortFrag = SortByDialog.newInstance(sortByItemSelected);
                sortFrag.setTargetFragment(CardSearchFrag.this, 300);
                sortFrag.show(this.getFragmentManager(), "SortByDialog");
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

    private void setRecyclerViewLayoutManager (LayoutManagerType layoutManagerType) {
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

    private List<Card> getCards() {
        List<Card> temp = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(loadJSON());
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

    private String loadJSON() {
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

    private void initSelected() {
        String[] rarity = getResources().getStringArray(R.array.rarity);
        String[] faction = getResources().getStringArray(R.array.faction);
        String[] set = getResources().getStringArray(R.array.set);

        // 2 accounts for creatures and spells
        // The rest is total number of rarities, factions, and sets
        selectedFilters = new HashMap<>(2 + rarity.length + faction.length + set.length);

        selectedFilters.put(getString(R.string.creature), true);
        selectedFilters.put(getString(R.string.spell), true);

        // all values start as true
        // this is so that all checkboxes are true
        for (int i = 0; i < returnGreatest(rarity.length, faction.length, set.length); i++) {
            if (i < rarity.length) {
                selectedFilters.put(rarity[i], true);
            }
            if (i < faction.length) {
                selectedFilters.put(faction[i], true);
            }
            if (i < set.length) {
                selectedFilters.put(set[i], true);
            }
        }
    }

    private int returnGreatest(int one, int two, int three) {
        if (one > two) {
            return (one > three) ? one : three;
        } else {
            return (two > three) ? two : three;
        }
    }

    @Override
    public void onClickSortMethod (int i) {
        sortByItemSelected = i;
        final List<Card> sortedCards = sort(cards, i);
        adapter.animateTo(sortedCards);
        if (!isDataEmpty(sortedCards)) {
            recyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void onClickFilter (HashMap<String, Boolean> selectedFilters) {
        this.selectedFilters = new HashMap<>(selectedFilters);
        final List<Card> filteredCards = filter(cards, selectedFilters);
        adapter.animateTo(filteredCards);
        if (!isDataEmpty(filteredCards)) {
            recyclerView.scrollToPosition(0);
        }
    }

    public void startEXCV (List<Card> cards, int position) {
        EXCVFragment excvFragment = EXCVFragment.newInstance(cards.get(position));
        this.getFragmentManager().beginTransaction()
                .replace(R.id.frag_content, excvFragment, "Extended Card View")
                .addToBackStack(null)
                .commit();
    }

    private boolean isDataEmpty (List<Card> input) {
        // if input data is empty, hide recyclerview
        // and show empty textview
        if (input.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            return true;
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            return false;
        }
    }

    private List<Card> search (List<Card> input, String query) {
        query = query.toLowerCase();

        final List<Card> searchedCards = new ArrayList<>();
        for (Card currCard : input) {
            final String name = currCard.getName();
            final String[] desc = currCard.getDesc();
            final String[] tribe = currCard.getTribe();
            if (containsQuery(query, name, desc, tribe)) {
                searchedCards.add(currCard);
            }
        }
        return searchedCards;
    }

    private List<Card> sort (List<Card> input, int sortMethod) {
        switch (sortMethod) {
            case 0:
                // Sort by name
                Collections.sort(input);
                break;
            case 1:
                // Sort by set
                Collections.sort(input, new Card.BySet());
                break;
            case 2:
                // Sort by rarity
                Collections.sort(input, new Card.ByRarity());
                break;
            case 3:
                // Sort by faction
                Collections.sort(input, new Card.ByFaction());
                break;
            case 4:
                // Sort by type
                Collections.sort(input, new Card.ByType());
                break;
            default:
                break;
        }
        return input;
    }

    private List<Card> filter (List<Card> input, HashMap<String, Boolean> filters) {
        final List<Card> filteredCards = new ArrayList<>();
        for (Card currCard : input) {
            // evaluates to false if card is to be filtered out
            // evaluates to true if card is to stay
            boolean filterCard = filters.get(currCard.getType()) &&
                    filters.get(currCard.getRarity()) &&
                    filters.get(currCard.getFaction()) &&
                    filters.get(setNumToString(currCard.getSet()));
            if (filterCard) {
                filteredCards.add(currCard);
            }
        }
        return filteredCards;
    }

    private String setNumToString (int setNum) {
        String error = getResources().getString(R.string.error);
        String[] set = getResources().getStringArray(R.array.set);

        return (setNum <= set.length && setNum > 0) ? set[setNum - 1] : error;
    }

    private boolean containsQuery (String query, String name, String[] desc, String[] tribe) {
        boolean inName = (name.toLowerCase()).contains(query);
        boolean inDesc = false;
        boolean inTrib = false;
        for (int i = 0; i < desc.length; i++) {
            inDesc = inDesc || (desc[i].toLowerCase()).contains(query);
            inTrib = inTrib || (tribe[i].toLowerCase()).contains(query);
        }
        return inName || inDesc || inTrib;
    }
}
