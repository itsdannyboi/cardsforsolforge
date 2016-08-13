package com.solforge.carddbforsolforge;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class CardSearchFrag extends Fragment implements SortByDialog.sortByDialogListener,
        FilterDialogFragment.FilterDialogListener {

    private static final String TAG = "Card Search Frag";
    private static final String KEY_SPAN = "span";
    private static final String KEY_DECK_EDIT = "deckEdit";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final String KEY_SCROLL_POSITION = "scrollPosition";
    private static final String KEY_SORT_METHOD = "sortMethod";
    private static final String KEY_FILTERS = "selectedFilters";

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    private LayoutManagerType currentLayoutManagerType;
    private Parcelable layoutManagerState;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CardSearchAdapter adapter;
    private TextView emptyView;
    private ToggleButton toggleButton;

    private List<Card> cards;
    private HashMap<String, Boolean> selectedFilters;
    private int sortByItemSelected = 0;
    private int scrollPosition;
    private int span = 2;
    private boolean inDeckEdit;

    public static CardSearchFrag newInstance() {
        return CardSearchFrag.newInstance(2, false);
    }

    public static CardSearchFrag newInstance(int i, boolean inDeckEdit) {
        CardSearchFrag fragment = new CardSearchFrag();
        Bundle args = new Bundle();
        args.putInt(KEY_SPAN, i);
        args.putBoolean(KEY_DECK_EDIT, inDeckEdit);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (this.getArguments() != null) {
            Bundle args = getArguments();
            span = args.getInt(KEY_SPAN);
            inDeckEdit = args.getBoolean(KEY_DECK_EDIT);
        }
        if (cards == null) { cards = MainActivity.getCardsDatabase(); }
        if (selectedFilters == null) { initSelected(); }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_card_search, container, false);
        rootView.setTag(TAG);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv);
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);

        layoutManager = new LinearLayoutManager(getActivity());

        // if currentLayoutManagerType hasn't been set yet, set it
        // ie when returning from backstack currentLayoutManagerType should be set already
        if (currentLayoutManagerType == null) {
            currentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        // restore state after rotations or activity saveInstanceState called
        if (savedInstanceState != null) {
            currentLayoutManagerType = (LayoutManagerType) savedInstanceState
                    .getSerializable(KEY_LAYOUT_MANAGER);
            scrollPosition = savedInstanceState.getInt(KEY_SCROLL_POSITION);
            sortByItemSelected = savedInstanceState.getInt(KEY_SORT_METHOD);
            selectedFilters = (HashMap<String, Boolean>) savedInstanceState
                    .getSerializable(KEY_FILTERS);
        }

        setRecyclerViewLayoutManager(currentLayoutManagerType);
        adapter = new CardSearchAdapter(cards, getActivity(), CardSearchFrag.this);
        recyclerView.setAdapter(adapter);

        // restore filter and sort state if configuration changed or returning from backstack
        checkFilterAndSort(savedInstanceState);

        // restore scroll if configuration changed or returning from backstack
        if (scrollPosition > 0) {
            recyclerView.scrollToPosition(scrollPosition);
        }

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
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
                // change card layout type
                adapter.changeLayoutType(isChecked);
            }
        });
        // used for restoring state
        if (currentLayoutManagerType == LayoutManagerType.GRID_LAYOUT_MANAGER) {
            toggleButton.setChecked(true);
        }

        MenuItem search = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        searchView.setIconifiedByDefault(true);
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
    public void onPause() {
        scrollPosition = getAdapterScrollPosition();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // save currently selected layout manager
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, currentLayoutManagerType);
        savedInstanceState.putInt(KEY_SCROLL_POSITION, getAdapterScrollPosition());
        savedInstanceState.putInt(KEY_SORT_METHOD, sortByItemSelected);
        savedInstanceState.putSerializable(KEY_FILTERS, selectedFilters);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onClickSortMethod (int i) {
        sortByItemSelected = i;
        List<Card> currentCards = new ArrayList<>(adapter.getCards());
        final List<Card> sortedCards = sort(currentCards, i);
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

    private void setRecyclerViewLayoutManager (LayoutManagerType layoutManagerType) {
        // If a layout manager has already been set, get current scroll position
        scrollPosition = getAdapterScrollPosition();

        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                layoutManager = new StaggeredGridLayoutManager(span,
                        StaggeredGridLayoutManager.VERTICAL);
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

    private int getAdapterScrollPosition () {
        if (recyclerView.getLayoutManager() != null) {
            if (currentLayoutManagerType == LayoutManagerType.LINEAR_LAYOUT_MANAGER) {
                return ((LinearLayoutManager) recyclerView.getLayoutManager())
                        .findFirstVisibleItemPosition();
            } else if (currentLayoutManagerType == LayoutManagerType.GRID_LAYOUT_MANAGER) {
                int[] visiblePositions = new int[2];
                ((StaggeredGridLayoutManager) recyclerView.getLayoutManager())
                        .findFirstCompletelyVisibleItemPositions(visiblePositions);
                if (visiblePositions[0] == RecyclerView.NO_POSITION) {
                    if (visiblePositions[1] == RecyclerView.NO_POSITION){
                        return 0;
                    }
                    return visiblePositions[1];
                } else {
                    return visiblePositions[0];
                }
            }
        }
        return 0;
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

    public void startEXCV (Card selectedCard) {
        EXCVFragment excvFragment = EXCVFragment.newInstance(selectedCard);
        scrollPosition = getAdapterScrollPosition();
        if (inDeckEdit) {
            this.getFragmentManager().beginTransaction()
                    .add(R.id.frag_content, excvFragment, "Extended Card View")
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        } else {
            this.getFragmentManager().beginTransaction()
                    .replace(R.id.frag_content, excvFragment, "Extended Card View")
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
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
        if (query != null) {
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
        return input;
    }

    private List<Card> sort (List<Card> input, int sortMethod) {
        switch (sortMethod) {
            case 0:
                // Sort by name
                Collections.sort(input);
                Collections.sort(cards);
                break;
            case 1:
                // Sort by set
                Collections.sort(input, new Card.BySet());
                Collections.sort(cards, new Card.BySet());
                break;
            case 2:
                // Sort by rarity
                Collections.sort(input, new Card.ByRarity());
                Collections.sort(cards, new Card.ByRarity());
                break;
            case 3:
                // Sort by faction
                Collections.sort(input, new Card.ByFaction());
                Collections.sort(cards, new Card.ByFaction());
                break;
            case 4:
                // Sort by type
                Collections.sort(input, new Card.ByType());
                Collections.sort(cards, new Card.ByType());
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
            if (desc[i] != null) {
                inDesc = inDesc || (desc[i].toLowerCase()).contains(query);
            }
            if (tribe[i] != null) {
                inTrib = inTrib || (tribe[i].toLowerCase()).contains(query);
            }
        }
        return inName || inDesc || inTrib;
    }

    private void checkFilterAndSort (Bundle savedInstanceState) {
        // check if filters are null, if they are try to pull from savedInstanceState
        if (selectedFilters == null && savedInstanceState != null) {
            selectedFilters = (HashMap<String, Boolean>) savedInstanceState
                    .getSerializable(KEY_FILTERS);
        }
        // if no savedInstanceState, filters is either null or exists, if not null apply
        if (selectedFilters != null && selectedFilters.containsValue(false)) {
            onClickFilter(selectedFilters);
        }

        // check if sort is null, if so try to pull from savedInstanceState
        if (savedInstanceState != null) {
            sortByItemSelected = savedInstanceState.getInt(KEY_SORT_METHOD);
        }
        // if no savedInstanceState, sort is either null or exists, if not null apply
        if (sortByItemSelected != 0) {
            onClickSortMethod(sortByItemSelected);
        }
    }
}
