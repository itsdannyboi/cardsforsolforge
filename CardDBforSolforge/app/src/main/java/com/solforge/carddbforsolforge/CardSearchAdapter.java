package com.solforge.carddbforsolforge;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;


public class CardSearchAdapter extends RecyclerView.Adapter<CardSearchAdapter.CardViewHolder> {

    private List<Card> cards;
    private Context context;
    private CardSearchFrag fragment;
    private String basepath;
    private boolean isGrid;

    CardSearchAdapter(List<Card> cards, Context context, CardSearchFrag fragment) {
        this.cards = new ArrayList<>(cards);
        this.context = context;
        this.fragment = fragment;
        this.basepath = context.getResources().getString(R.string.basepath);
        this.isGrid = false;
    }

    // ViewHolder
    public class CardViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener {
        CardView cv;
        LinearLayout imageContainer;
        FrameLayout nonLevelOneImageContainer;
        RecyclerView nonLevelOneImages;
        View emptySpace;
        ImageView cardImage;
        ImageView cardRarity;
        ImageView cardFaction;
        TextView cardTitle;
        TextView cardDesc;

        CardViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            // common layouts
            cv = (CardView) itemView.findViewById(R.id.cv);
            imageContainer = (LinearLayout) itemView.findViewById(R.id.image_holder);
            cardImage = (ImageView) itemView.findViewById(R.id.card_image);
            cardRarity = (ImageView) itemView.findViewById(R.id.card_rarity);
            cardFaction = (ImageView) itemView.findViewById(R.id.card_faction);
            cardTitle = (TextView) itemView.findViewById(R.id.card_name);

            // linear list only layouts
            nonLevelOneImageContainer = (FrameLayout)
                    itemView.findViewById(R.id.non_level_one_image_holder);
            nonLevelOneImages = (RecyclerView) itemView.findViewById(R.id.non_level_one_images);
            emptySpace = (View) itemView.findViewById(R.id.empty_space);
            cardDesc = (TextView) itemView.findViewById(R.id.card_desc);

            nonLevelOneImages.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            fragment.startEXCV(cards.get(getAdapterPosition()));
        }
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    @Override
    public CardViewHolder onCreateViewHolder (ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_card_layout, viewGroup, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CardViewHolder cardViewHolder, int i) {
        configureViewHolder(cardViewHolder, i, isGrid);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // viewholder configurations
    public void configureViewHolder (CardViewHolder viewHolder, int position, boolean isGrid) {
        // fields common to both viewholders
        Glide.with(context)
                .load(basepath + cards.get(position).getImage(0))
                .placeholder(R.drawable.ic_image_black)
                .error(R.drawable.ic_broken_image_black)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(viewHolder.cardImage);

        viewHolder.cardTitle.setText(cards.get(position).getName());
        viewHolder.cardRarity.setImageDrawable(ContextCompat.getDrawable(context,
                Card.cardRarityHelper(cards.get(position))));
        viewHolder.cardFaction.setImageResource(Card.cardFactionHelper(cards.get(position)));

        // linear layout specific
        boolean[] levels = cards.get(position).getLevels();
        ArrayList<String> imageEnds = new ArrayList<>(3);
        for (int i = 0; i < levels.length; i++) {
            if (levels[i]) {
                imageEnds.add(cards.get(position).getImage(i+1));
            } else {
                i = levels.length;
            }
        }

        if (imageEnds.size() > 0 && (!cards.get(position).getType3().equals("mt") ||
                cards.get(position).getType().equals("Creature"))) {
            viewHolder.emptySpace.setVisibility(View.GONE);
            viewHolder.nonLevelOneImages.setVisibility(View.VISIBLE);

            viewHolder.nonLevelOneImages.setHasFixedSize(true);
            viewHolder.nonLevelOneImages.setLayoutManager(new GridLayoutManager(context, 2));
            viewHolder.nonLevelOneImages.setAdapter(new LeveledImageAdapter(imageEnds, context, basepath));
            viewHolder.nonLevelOneImages.scrollToPosition(0);
        } else {
            viewHolder.nonLevelOneImages.setVisibility(View.GONE);
            viewHolder.emptySpace.setVisibility(View.VISIBLE);
        }

        viewHolder.cardDesc.setText(cards.get(position).getDesc(0));

        if (isGrid) {
            viewHolder.nonLevelOneImageContainer.setVisibility(View.GONE);
            viewHolder.cardDesc.setVisibility(View.GONE);
        } else {
            viewHolder.nonLevelOneImageContainer.setVisibility(View.VISIBLE);
            viewHolder.cardDesc.setVisibility(View.VISIBLE);
        }
    }

    // getters/setters
    public boolean getIsGrid () { return this.isGrid; }
    public void setIsGrid (boolean val) { this.isGrid = val; }
    public List<Card> getCards () { return cards; }
    public void setCards (List<Card> vals) {
        cards.clear();
        cards.addAll(vals);
    }

    // utility functions
    public void changeLayoutType (boolean isGrid) {
        int count = getItemCount();
        setIsGrid(isGrid);
        notifyItemRangeChanged(0, count);
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

class LeveledImageAdapter extends RecyclerView.Adapter<LeveledImageAdapter.ImageHolder> {

    List<String> imageEnds;
    Context context;
    String basepath;

    LeveledImageAdapter(List<String> imageEnds, Context context, String basepath) {
        this.imageEnds = new ArrayList<>(imageEnds);
        this.context = context;
        this.basepath = basepath;
    }

    // ViewHolder
    public class ImageHolder extends RecyclerView.ViewHolder {

        ImageView leveledImage;

        ImageHolder (View itemView) {
            super(itemView);

            leveledImage = (ImageView) itemView.findViewById(R.id.leveled_image);
        }

    }

    @Override
    public int getItemCount () {
        return imageEnds.size();
    }

    @Override
    public ImageHolder onCreateViewHolder (ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_view_leveled_image_holder, viewGroup, false);
        return new ImageHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageHolder imageHolder, int i) {
        Glide.with(context)
                .load(basepath + imageEnds.get(i))
                .placeholder(R.drawable.ic_image_black)
                .error(R.drawable.ic_broken_image_black)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(imageHolder.leveledImage);;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}

