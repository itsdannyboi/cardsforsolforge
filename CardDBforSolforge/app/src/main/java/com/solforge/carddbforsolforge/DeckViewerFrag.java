package com.solforge.carddbforsolforge;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;


public class DeckViewerFrag extends Fragment implements DeckBuilderFrag.saveDeckListener {
    // TODO: implement long touch hold to reorganize
    // TODO: implement swipe to delete

    private static final String TAG = "Deck Viewer Frag";

    private FloatingActionButton fab;
    private RelativeLayout deckViewer;
    private LinearLayout deckBuilder;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private DeckViewerAdapter adapter;

    private List<Deck> decks;

    public DeckViewerFrag() {
        // Required empty public constructor
    }

    public static DeckViewerFrag newInstance () {
        return new DeckViewerFrag();
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.decks == null) { this.decks = MainActivity.getDeckList(); }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_deck_viewer, container, false);
        rootView.setTag(TAG);

        fab = (FloatingActionButton) rootView.findViewById(R.id.new_deck);
        deckViewer = (RelativeLayout) rootView.findViewById(R.id.deck_viewer);
        deckBuilder = (LinearLayout) rootView.findViewById(R.id.deck_builder);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv);
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);

        adapter = new DeckViewerAdapter(decks, getActivity(), DeckViewerFrag.this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startEditDeck(new Deck());
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        isDataEmpty(decks);

        return rootView;
    }

    @Override
    public void onClickSave (List<Deck> deckList) {
        adapter.animateTo(deckList);
    }

    // local functions
    private boolean isDataEmpty (List<Deck> input) {
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

    public void startEditDeck (Deck deckSelected) {
        CardSearchFrag cardSearchFrag = CardSearchFrag.newInstance(1, true);
        DeckBuilderFrag deckBuilderFrag = DeckBuilderFrag.newInstance(deckSelected);
        deckBuilderFrag.setTargetFragment(DeckViewerFrag.this, 300);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.left_pane, cardSearchFrag, MainActivity.FRAG_IDENTIFIER);
        fragmentTransaction.replace(R.id.right_pane, deckBuilderFrag, MainActivity.FRAG_IDENTIFIER);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        fab.setVisibility(View.GONE);
        deckViewer.setVisibility(View.GONE);
        deckBuilder.setVisibility(View.VISIBLE);
    }

    public void exitEditDeck () {
        fab.setVisibility(View.VISIBLE);
        deckViewer.setVisibility(View.VISIBLE);
        deckBuilder.setVisibility(View.GONE);
    }
}

class DeckViewerAdapter extends RecyclerView.Adapter<DeckViewerAdapter.DeckHolder> {

    private List<Deck> decks;
    private Context context;
    private DeckViewerFrag fragment;

    public DeckViewerAdapter(List<Deck> decks, Context context, DeckViewerFrag fragment) {
        this.decks = decks;
        this.context = context;
        this.fragment = fragment;
    }

    // ViewHolder
    public class DeckHolder extends RecyclerView.ViewHolder {

        CardView cv;
        RelativeLayout hiddenContainer;
        ImageView expandableArrow;
        ImageView alloyin;
        ImageView nekrium;
        ImageView tempys;
        ImageView uterra;
        TextView deckName;
        RecyclerView deckContents;
        Button editDeck;

        boolean viewToggle = false;

        DeckHolder (View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewToggle = !viewToggle;
                    if (viewToggle) {
                        // if true, expand card
                        hiddenContainer.setVisibility(View.VISIBLE);
                    } else {
                        // if false, hide card contents and edit deck button
                        hiddenContainer.setVisibility(View.GONE);
                    }
                }
            });

            cv = (CardView) itemView.findViewById(R.id.cv);
            hiddenContainer = (RelativeLayout) itemView.findViewById(R.id.hidden_container);
            expandableArrow = (ImageView) itemView.findViewById(R.id.dropdown_arrow);
            alloyin = (ImageView) itemView.findViewById(R.id.alloyin);
            nekrium = (ImageView) itemView.findViewById(R.id.nekrium);
            tempys = (ImageView) itemView.findViewById(R.id.tempys);
            uterra = (ImageView) itemView.findViewById(R.id.uterra);
            deckName = (TextView) itemView.findViewById(R.id.deck_name);
            deckContents = (RecyclerView) itemView.findViewById(R.id.deck_contents);
            editDeck = (Button) itemView.findViewById(R.id.edit_deck);

            editDeck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.startEditDeck(decks.get(getAdapterPosition()));
                }
            });
        }
    }

    @Override
    public int getItemCount () {
        return decks.size();
    }

    @Override
    public DeckHolder onCreateViewHolder (ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_deck_list_holder,
                viewGroup, false);
        return new DeckHolder(view);
    }

    @Override
    public void onBindViewHolder (DeckHolder deckHolder, int i) {
        Deck currDeck = decks.get(i);

        // Show Relevant Deck Factions
        if (currDeck.getFactionCount(Deck.ALLOYIN) > 0) {
            deckHolder.alloyin.setVisibility(View.VISIBLE);
        } else {
            deckHolder.alloyin.setVisibility(View.GONE);
        }
        if (currDeck.getFactionCount(Deck.NEKRIUM) > 0) {
            deckHolder.nekrium.setVisibility(View.VISIBLE);
        } else {
            deckHolder.nekrium.setVisibility(View.GONE);
        }
        if (currDeck.getFactionCount(Deck.TEMPYS) > 0) {
            deckHolder.tempys.setVisibility(View.VISIBLE);
        } else {
            deckHolder.tempys.setVisibility(View.GONE);
        }
        if (currDeck.getFactionCount(Deck.UTERRA) > 0) {
            deckHolder.uterra.setVisibility(View.VISIBLE);
        } else {
            deckHolder.uterra.setVisibility(View.GONE);
        }

        deckHolder.deckName.setText(currDeck.getDeckName());

        deckHolder.deckContents.setLayoutManager(new LinearLayoutManager(context));
        deckHolder.deckContents.setAdapter(new DeckContentsAdapter(context, currDeck));
    }

    @Override
    public void onAttachedToRecyclerView (RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // utility functions
    public Deck removeItem (int position) {
        final Deck deck = decks.remove(position);
        notifyItemRemoved(position);
        return deck;
    }

    public void addItem (int position, Deck deck) {
        this.decks.add(position, deck);
        notifyItemInserted(position);
    }

    public void moveItem (int fromPosition, int toPosition) {
        final Deck deck = this.decks.remove(fromPosition);
        this.decks.add(toPosition, deck);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo (List<Deck> input) {
        applyAndAnimateRemovals(input);
        applyAndAnimateAdditions(input);
        applyAndAnimateMoveItems(input);
    }

    private void applyAndAnimateRemovals (List<Deck> input) {
        for (int i = this.decks.size() - 1; i >= 0; i--) {
            final Deck deck = this.decks.get(i);
            if (!input.contains(deck)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions (List<Deck> input) {
        for (int i = 0; i < input.size(); i++) {
            final Deck deck = input.get(i);
            if (!this.decks.contains(deck)) {
                addItem(i, deck);
            }
        }
    }

    private void applyAndAnimateMoveItems (List<Deck> input) {
        for (int toPosition = input.size() - 1; toPosition >= 0; toPosition--) {
            final Deck deck = input.get(toPosition);
            final int fromPosition = this.decks.indexOf(deck);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }
}

class DeckContentsAdapter extends RecyclerView.Adapter<DeckContentsAdapter.ContentHolder> {

    HashMap<String, Integer> cardCount;
    List<Card> cards;
    Context context;

    DeckContentsAdapter (Context context, Deck deck) {
        this.cardCount = deck.getCardCount();
        this.cards = deck.getDeck();
        this.context = context;
    }

    // ViewHolder
    public class ContentHolder extends RecyclerView.ViewHolder {

        TextView cardName;
        TextView cardCount;

        ContentHolder (View itemView) {
            super(itemView);

            cardName = (TextView) itemView.findViewById(R.id.card_name);
            cardCount = (TextView) itemView.findViewById(R.id.card_count);
        }
    }

    @Override
    public int getItemCount () {
        return cards.size();
    }

    @Override
    public ContentHolder onCreateViewHolder (ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_deck_contents,
                viewGroup, false);
        return new ContentHolder(view);
    }

    @Override
    public void onBindViewHolder (ContentHolder contentHolder, int i) {
        String name = cards.get(i).getName();
        String count = "x" + cardCount.get(name);

        contentHolder.cardName.setText(name);
        contentHolder.cardCount.setText(count);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
