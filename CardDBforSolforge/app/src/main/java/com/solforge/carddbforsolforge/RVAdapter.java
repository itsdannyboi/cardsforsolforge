package com.solforge.carddbforsolforge;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.Collections;
import java.util.List;

/**
 * Created by Daniel on 6/29/2016.
 */

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.CardViewHolder> {

    private List<Card> cards;
    private Context context;

    RVAdapter (List<Card> cards, Context context) {
        this.cards = cards;
        this.context = context;
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
            // TODO: create new expanded cardview Fragment
            CardSearchFrag.startEXCV(cards, getAdapterPosition());
            Toast.makeText(context, cards.get(getAdapterPosition()).getName(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
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
                cardRarityHelper(i)));
        cardViewHolder.cardFaction.setImageResource(cardFactionHelper(i));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public int cardFactionHelper (int i) {
        int faction;
        switch (cards.get(i).getFaction()) {
            case "Alloyin":
                faction = R.drawable.ic_alloyin_symbol;
                break;
            case "Nekrium":
                faction = R.drawable.ic_nekrium_symbol;
                break;
            case "Tempys":
                faction = R.drawable.ic_tempys_symbol;
                break;
            case "Uterra":
                faction = R.drawable.ic_uterra_symbol;
                break;
            default:
                faction = R.drawable.ic_solforge_symbol;
                break;
        }
        return faction;
    }

    public int cardRarityHelper (int i) {
        int rarity;
        switch (cards.get(i).getRarity()) {
            case "Common":
                rarity = R.drawable.rec_common;
                break;
            case "Rare":
                rarity = R.drawable.rec_rare;
                break;
            case "Heroic":
                rarity = R.drawable.rec_heroic;
                break;
            case "Legendary":
                rarity = R.drawable.rec_legendary;
                break;
            default:
                rarity = R.drawable.rec_default;
                break;
        }
        return rarity;
    }

    public void sortAndUpdateData(int i) {
        switch (i) {
            case 0:
                // Sort by name
                Collections.sort(cards);
                break;
            case 1:
                // Sort by set
                Collections.sort(cards, new Card.BySet());
                break;
            case 2:
                // Sort by rarity
                Collections.sort(cards, new Card.ByRarity());
                break;
            case 3:
                // Sort by faction
                Collections.sort(cards, new Card.ByFaction());
                break;
            case 4:
                // Sort by type
                Collections.sort(cards, new Card.ByType());
                break;
            default:
                break;
        }
        notifyDataSetChanged();
    }
}

