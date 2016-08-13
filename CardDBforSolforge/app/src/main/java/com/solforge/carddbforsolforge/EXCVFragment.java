package com.solforge.carddbforsolforge;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class EXCVFragment extends Fragment {

    private static final String TAG = "Extended Card View Frag";
    private static final String KEY_CARD_SELECTED = "cardSelected";

    private ArrayList<String> imageNames;
    private Card cardSelected;
    private int levelSelected = 0;

    private LinearLayout dotsLayout;
    private TextView cardAttack;
    private TextView cardHealth;
    private TextView cardDesc;


    public EXCVFragment() {
        // Required empty public constructor
    }

    public static EXCVFragment newInstance (Card cardSelected) {
        EXCVFragment excvFragment = new EXCVFragment();
        Bundle args = new Bundle();
        args.putParcelable(KEY_CARD_SELECTED, cardSelected);
        excvFragment.setArguments(args);
        return excvFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_expanded_card_view, container, false);
        rootView.setTag(TAG);

        if (savedInstanceState != null) {
            cardSelected = savedInstanceState.getParcelable(KEY_CARD_SELECTED);
        } else {
            cardSelected = getArguments().getParcelable(KEY_CARD_SELECTED);
        }

        imageNames = new ArrayList<>(removeNulls(cardSelected.getImage()));

        // Layout Initializations
        ViewPager cardImage = (ViewPager) rootView.findViewById(R.id.image_view_pager);
        LinearLayout atkHealthContainer = (LinearLayout) rootView.findViewById(R.id.atk_health);
        dotsLayout = (LinearLayout) rootView.findViewById(R.id.image_indicator);

        ImageView cardRarity = (ImageView) rootView.findViewById(R.id.card_rarity);
        ImageView cardFaction = (ImageView) rootView.findViewById(R.id.full_card_faction);
        TextView cardName = (TextView) rootView.findViewById(R.id.full_card_name);
        TextView cardType = (TextView) rootView.findViewById(R.id.full_card_type);
        TextView cardSet = (TextView) rootView.findViewById(R.id.full_card_set);
        TextView cardDraft = (TextView) rootView.findViewById(R.id.draft_bool);
        cardAttack = (TextView) rootView.findViewById(R.id.full_card_attack);
        cardHealth = (TextView) rootView.findViewById(R.id.full_card_health);
        cardDesc = (TextView) rootView.findViewById(R.id.full_card_description);

        // set data
        cardRarity.setImageDrawable(ContextCompat.getDrawable(getContext(),
                Card.cardRarityHelper(cardSelected)));
        cardFaction.setImageResource(Card.cardFactionHelper(cardSelected));
        cardName.setText(cardSelected.getName());
        cardType.setText(Card.getFullType(cardSelected));
        cardSet.setText(getSet(cardSelected));
        cardDraft.setText(String.valueOf(cardSelected.getDraft()));
        setLevelDependentData(0);

        cardImage.setAdapter(new SlidingImageAdapter(getContext(), imageNames));
        cardImage.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setLevelDependentData(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        atkHealthContainer.setVisibility((cardSelected.getType()
                .equals(getString(R.string.creature))) ? View.VISIBLE : View.GONE);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.excv_menu, menu);

        MenuItem spinner = menu.findItem(R.id.spell_level_spinner);
        Spinner spellSpinner = (Spinner) MenuItemCompat.getActionView(spinner);

        int range = 1;
        for (int i = 0; i < cardSelected.getLevels().length; i++) {
            if (cardSelected.getLevels(i)) {
                range++;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                R.layout.spinner_item,
                Arrays.copyOfRange(getResources().getStringArray(R.array.levels), 0, range));
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spellSpinner.setAdapter(adapter);
        spellSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setLevelDependentData(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if (cardSelected.getType().equals(getString(R.string.creature))) {
            menu.findItem(R.id.spell_level_spinner).setVisible(false);
        } else {
            menu.findItem(R.id.spell_level_spinner).setVisible(true);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_CARD_SELECTED, cardSelected);
        super.onSaveInstanceState(savedInstanceState);
    }

    // local functions
    private void setLevelDependentData (int levelSelected) {
        if (imageNames.size() > 1) {
            setImageIndicator(levelSelected, imageNames.size());
        }
        cardAttack.setText(String.valueOf(cardSelected.getAtk(levelSelected)));
        cardHealth.setText(String.valueOf(cardSelected.getHealth(levelSelected)));
        cardDesc.setText(cardSelected.getDesc(levelSelected));
    }

    private void setImageIndicator (int currentPage, int numDots) {
        TextView[] dots = new TextView[numDots];

        dotsLayout.removeAllViews();
        for (int i = 0; i < numDots; i++) {
            dots[i] = new TextView(getContext());
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor(R.color.dot_indicator_inactive));
            dotsLayout.addView(dots[i]);
        }
        dots[currentPage].setTextColor(getResources().getColor(R.color.dot_indicator_active));
    }

    private List<String> removeNulls (String[] imageNames) {
        List<String> imageNamesAsList = new ArrayList<>();
        for (String image : imageNames) {
            if (image != null) {
                imageNamesAsList.add(image);
            }
        }
        return imageNamesAsList;
    }

    private String getSet (Card card) {
        String[] sets = getResources().getStringArray(R.array.set);
        int i = card.getSet();
        return ((i>0 && i <=sets.length) ? sets[i-1] : getString(R.string.error));
    }

    // getters/setters
    public Card getCardSelected () { return this.cardSelected; }
    public void setCardSelected (Card cardSelected) { this.cardSelected = cardSelected; }

}

class SlidingImageAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> imageEnds;

    public SlidingImageAdapter (Context context, List<String> imageEnds) {
        this.context = context;
        this.imageEnds = new ArrayList<>(imageEnds);
    }

    @Override
    public int getCount() {
        return imageEnds.size();
    }

    @Override
    public boolean isViewFromObject (View v, Object obj) {
        return v == obj;
    }

    @Override
    public Object instantiateItem (ViewGroup viewGroup, int position) {
        ImageView cardImage = new ImageView(context);
        Glide.with(context)
                .load(context.getResources().getString(R.string.basepath) +
                        imageEnds.get(position))
                .placeholder(R.drawable.ic_image_black)
                .error(R.drawable.ic_broken_image_black)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop()
                .into(cardImage);
        ((ViewPager) viewGroup).addView(cardImage);
        return cardImage;
    }

    @Override
    public void destroyItem (ViewGroup viewGroup, int position, Object obj) {
        View view = (View) obj;
        viewGroup.removeView(view);
    }
}
