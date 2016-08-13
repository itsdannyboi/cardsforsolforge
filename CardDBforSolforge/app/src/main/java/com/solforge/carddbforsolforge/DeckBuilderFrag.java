package com.solforge.carddbforsolforge;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;


public class DeckBuilderFrag extends Fragment {

    private static final String TAG = "Deck Builder Fragment";
    private static final String KEY_DECK = "selectedDeck";


    private RecyclerView recyclerView;
    private TextView emptyView;
    private EditText deckName;
    private ImageView alloyin;
    private ImageView nekrium;
    private ImageView tempys;
    private ImageView uterra;

    private Deck currDeck;

    public DeckBuilderFrag() {
        // Required empty public constructor
    }

    public static DeckBuilderFrag newInstance(Deck currDeck) {
        DeckBuilderFrag deckBuilderFrag = new DeckBuilderFrag();
        Bundle args = new Bundle();
        args.putParcelable(KEY_DECK, currDeck);
        deckBuilderFrag.setArguments(args);
        return deckBuilderFrag;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.currDeck == null) {
            if (savedInstanceState != null) {
                this.currDeck = savedInstanceState.getParcelable(KEY_DECK);
            } else {
                this.currDeck = getArguments().getParcelable(KEY_DECK);
            }
        }
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_deck_builder, container, false);
        rootView.setTag(TAG);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.rv);
        emptyView = (TextView) rootView.findViewById(R.id.empty_view);
        deckName = (EditText) rootView.findViewById(R.id.edit_text_deckname);
        alloyin = (ImageView) rootView.findViewById(R.id.alloyin);
        nekrium = (ImageView) rootView.findViewById(R.id.nekrium);
        tempys = (ImageView) rootView.findViewById(R.id.tempys);
        uterra = (ImageView) rootView.findViewById(R.id.uterra);
        Button saveDeck = (Button) rootView.findViewById(R.id.save_deck);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new DeckBuilderAdapter(currDeck, getContext()));

        saveDeck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currDeck.setDeckName(deckName.getText().toString());

                MainActivity parentActivity = (MainActivity) getActivity();
                parentActivity.saveDeck(currDeck);
                // TODO: allow setting to return to DeckViewer after save or stay in DeckBuilder
                saveDeckListener deckListener = (saveDeckListener) getTargetFragment();
                deckListener.onClickSave(MainActivity.getDeckList());
            }
        });

        if (currDeck.getDeckName() != null) {
            deckName.setText(currDeck.getDeckName());
        }

        checkFactions();
        isDataEmpty(currDeck);

        return rootView;
    }

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_DECK, currDeck);
        super.onSaveInstanceState(savedInstanceState);
    }

    // local functions
    private void checkFactions () {
        // Show Relevant Deck Factions
        if (currDeck.getFactionCount(Deck.ALLOYIN) > 0) {
            alloyin.setVisibility(View.VISIBLE);
        } else {
            alloyin.setVisibility(View.GONE);
        }
        if (currDeck.getFactionCount(Deck.NEKRIUM) > 0) {
            nekrium.setVisibility(View.VISIBLE);
        } else {
            nekrium.setVisibility(View.GONE);
        }
        if (currDeck.getFactionCount(Deck.TEMPYS) > 0) {
            tempys.setVisibility(View.VISIBLE);
        } else {
            tempys.setVisibility(View.GONE);
        }
        if (currDeck.getFactionCount(Deck.UTERRA) > 0) {
            uterra.setVisibility(View.VISIBLE);
        } else {
            uterra.setVisibility(View.GONE);
        }
    }

    private boolean isDataEmpty (Deck currDeck) {
        if (currDeck.getDeck().isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            return true;
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            return false;
        }
    }

    public interface saveDeckListener {
        void onClickSave (List<Deck> deckList);
    }
}

class DeckBuilderAdapter extends RecyclerView.Adapter<DeckBuilderAdapter.DeckContentsHolder> {

    private Context context;
    private Deck currDeck;
    private List<Card> cardsInDeck;

    DeckBuilderAdapter (Deck currDeck, Context context) {
        this.currDeck = currDeck;
        this.cardsInDeck = currDeck.getDeck();
        this.context = context;
    }

    // ViewHolder
    public class DeckContentsHolder extends RecyclerView.ViewHolder {

        TextView cardCount;
        TextView cardName;
        Button increaseCardCount;
        Button decreaseCardCount;

        DeckContentsHolder (View itemView) {
            super(itemView);

            cardCount = (TextView) itemView.findViewById(R.id.card_count);
            cardName = (TextView) itemView.findViewById(R.id.card_name);
            increaseCardCount = (Button) itemView.findViewById(R.id.increase_card_count);
            decreaseCardCount = (Button) itemView.findViewById(R.id.decrease_card_count);

            increaseCardCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currDeck.addCard(cardsInDeck.get(getAdapterPosition()));
                    notifyItemChanged(getAdapterPosition());
                }
            });

            decreaseCardCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    currDeck.removeCard(cardsInDeck.get(getAdapterPosition()));
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount () {
        return cardsInDeck.size();
    }

    @Override
    public DeckContentsHolder onCreateViewHolder (ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_deck_builder,
                viewGroup, false);
        return new DeckContentsHolder(view);
    }

    @Override
    public void onBindViewHolder (DeckContentsHolder deckContentsHolder, int i) {
        Card currCard = cardsInDeck.get(i);
        deckContentsHolder.cardCount.setText(currDeck.getCardCount(currCard));
        deckContentsHolder.cardName.setText(currCard.getName());
    }

    @Override
    public void onAttachedToRecyclerView (RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
