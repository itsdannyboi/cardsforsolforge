package com.solforge.carddbforsolforge;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;


public class EXCVFragment extends Fragment {

    private static final String TAG = "Extended Card View Frag";
    private Card cardSelected;
    private int levelSelected = 0;

    public EXCVFragment() {
        // Required empty public constructor
    }

    public static EXCVFragment newInstance (Card cardSelected) {
        EXCVFragment excvFragment = new EXCVFragment();
        excvFragment.setCardSelected(cardSelected);
        return excvFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.expanded_card_view, container, false);
        rootView.setTag(TAG);

        ImageView cardImage = (ImageView) rootView.findViewById(R.id.full_card_image);
        ImageView cardFaction = (ImageView) rootView.findViewById(R.id.full_card_faction);
        TextView cardAttack = (TextView) rootView.findViewById(R.id.full_card_attack);
        TextView cardHealth = (TextView) rootView.findViewById(R.id.full_card_health);
        TextView cardName = (TextView) rootView.findViewById(R.id.full_card_name);
        TextView cardType = (TextView) rootView.findViewById(R.id.full_card_type);
        TextView cardSet = (TextView) rootView.findViewById(R.id.full_card_set);
        TextView cardDraft = (TextView) rootView.findViewById(R.id.draft_bool);
        TextView cardDesc = (TextView) rootView.findViewById(R.id.full_card_description);
        // code for divider?

        Glide.with(this)
                .load(getResources().getString(R.string.basepath) +
                        cardSelected.getImage(levelSelected))
                .placeholder(R.drawable.ic_image_black)
                .error(R.drawable.ic_broken_image_black)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(cardImage);

        cardFaction.setImageResource(cardFactionHelper(cardSelected));
        cardAttack.setText(String.valueOf(cardSelected.getAtk(levelSelected)));
        cardHealth.setText(String.valueOf(cardSelected.getHealth(levelSelected)));
        cardName.setText(cardSelected.getName());
        cardType.setText(getType(cardSelected));
        cardSet.setText(getSet(cardSelected));
        cardDraft.setText(String.valueOf(cardSelected.getDraft()));
        cardDesc.setText(cardSelected.getDesc(levelSelected));

        return rootView;
    }

    private int cardFactionHelper (Card cardSelected) {
        int faction;
        switch (cardSelected.getFaction()) {
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

    private String getType (Card card) {
        if (card.getType().equals("Spell")) {
            return card.getType();
        } else {
            return (card.getType() + " - " + card.getTribe(levelSelected));
        }
    }

    private String getSet (Card card) {
        String[] sets = getResources().getStringArray(R.array.set);
        int i = card.getSet();
        return ((i>0 && i <=sets.length) ? sets[i-1] : getString(R.string.error));
    }

    public Card getCardSelected () { return this.cardSelected; }
    public void setCardSelected (Card cardSelected) { this.cardSelected = cardSelected; }

}
