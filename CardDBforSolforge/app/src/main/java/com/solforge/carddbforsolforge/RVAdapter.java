package com.solforge.carddbforsolforge;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;


public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardViewHolder> {

    private List<Card> cards;
    private Context context;
    private CardSearchFrag fragment;

    RVAdapter (List<Card> cards, Context context, CardSearchFrag fragment) {
        this.cards = new ArrayList<>(cards);
        this.context = context;
        this.fragment = fragment;
    }

    public class CardViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        CardView cv;
        ImageView cardImage;
        ImageView cardRarity;
        ImageView cardFaction;
        TextView cardTitle;

        CardViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            cv = (CardView)itemView.findViewById(R.id.cv);
            cardImage = (ImageView)itemView.findViewById(R.id.card_image);
            cardRarity = (ImageView)itemView.findViewById(R.id.card_rarity);
            cardFaction = (ImageView)itemView.findViewById(R.id.card_faction);
            cardTitle = (TextView)itemView.findViewById(R.id.card_name);
        }

        @Override
        public void onClick(View v) {
            fragment.startEXCV(cards, getAdapterPosition());
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }
    public List<Card> getCards () { return cards; }
    public void setCards (List<Card> vals) {
        cards.clear();
        cards.addAll(vals);
    }

    @Override
    public CardViewHolder onCreateViewHolder (ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.card_view_layout, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CardViewHolder cardViewHolder, int i) {
        Glide.with(context)
                .load(context.getResources().getString(R.string.basepath) +
                        cards.get(i).getImage(0))
                .placeholder(R.drawable.ic_image_black)
                .error(R.drawable.ic_broken_image_black)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(cardViewHolder.cardImage);

        cardViewHolder.cardTitle.setText(cards.get(i).getName());
        cardViewHolder.cardRarity.setImageDrawable(ContextCompat.getDrawable(context,
                Card.cardRarityHelper(cards.get(i))));
        cardViewHolder.cardFaction.setImageResource(Card.cardFactionHelper(cards.get(i)));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public Card removeItem (int position) {
        final Card card = cards.remove(position);
        notifyItemRemoved(position);
        return card;
    }

    public void addItem (int position, Card card) {
        this.cards.add(position, card);
        notifyItemInserted(position);
    }

    public void moveItem (int fromPosition, int toPosition) {
        final Card card = this.cards.remove(fromPosition);
        this.cards.add(toPosition, card);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo (List<Card> input) {
        applyAndAnimateRemovals(input);
        applyAndAnimateAdditions(input);
        applyAndAnimateMoveItems(input);
    }

    private void applyAndAnimateRemovals (List<Card> input) {
        for (int i = this.cards.size() - 1; i >= 0; i--) {
            final Card card = this.cards.get(i);
            if (!input.contains(card)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions (List<Card> input) {
        for (int i = 0; i < input.size(); i++) {
            final Card card = input.get(i);
            if (!this.cards.contains(card)) {
                addItem(i, card);
            }
        }
    }

    private void applyAndAnimateMoveItems (List<Card> input) {
        for (int toPosition = input.size() - 1; toPosition >= 0; toPosition--) {
            final Card card = input.get(toPosition);
            final int fromPostion = this.cards.indexOf(card);
            if (fromPostion >= 0 && fromPostion != toPosition) {
                moveItem(fromPostion, toPosition);
            }
        }
    }
}

