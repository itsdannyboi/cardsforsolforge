package com.solforge.carddbforsolforge;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;


public class FilterDialogFragment extends DialogFragment {

    private final static String KEY_FILTERS = "filtersSelected";
    private FilterDialogListener dialogListener;
    private HashMap<String, Boolean> selectedFilters;

    interface FilterDialogListener {
        void onClickFilter(HashMap<String, Boolean> selectedFilters);
    }

    public FilterDialogFragment() {
        // Required empty public constructor
    }

    public static FilterDialogFragment newInstance(HashMap<String, Boolean> input) {
        FilterDialogFragment filterDialogFragment = new FilterDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(KEY_FILTERS, input);
        filterDialogFragment.setArguments(args);
        return filterDialogFragment;
    }

    @Override
    public Dialog onCreateDialog (Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectedFilters = (HashMap<String, Boolean>)
                    savedInstanceState.getSerializable(KEY_FILTERS);
        } else {
            selectedFilters = (HashMap<String, Boolean>)
                    getArguments().getSerializable(KEY_FILTERS);
        }

        final String[] rarity = getResources().getStringArray(R.array.rarity);
        final String[] faction = getResources().getStringArray(R.array.faction);
        final String[] set = getResources().getStringArray(R.array.set);
        final String creatureText = getString(R.string.creature);
        final String spellText = getString(R.string.spell);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_filter,
                null);

        // view data initialization/setting
        final CheckBox creatureFilter = (CheckBox) view.findViewById(R.id.creature_check_box);
        final CheckBox spellFilter = (CheckBox) view.findViewById(R.id.spell_check_box);
        final RelativeLayout creatureLayout = (RelativeLayout)
                view.findViewById(R.id.creatures_container);
        final RelativeLayout spellLayout = (RelativeLayout)
                view.findViewById(R.id.spell_container);

        creatureFilter.setChecked(selectedFilters.get(creatureText));
        spellFilter.setChecked(selectedFilters.get(spellText));

        creatureFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setItemSelected(creatureText, creatureFilter.isChecked());
            }
        });
        spellFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setItemSelected(spellText, spellFilter.isChecked());
            }
        });
        creatureLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creatureFilter.toggle();
                setItemSelected(creatureText, creatureFilter.isChecked());
            }
        });
        spellLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spellFilter.toggle();
                setItemSelected(spellText, spellFilter.isChecked());
            }
        });

        // recycler view initilizations
        RecyclerView rarityView = (RecyclerView) view.findViewById(R.id.rarity_rv);
        RecyclerView factionView = (RecyclerView) view.findViewById(R.id.faction_rv);
        RecyclerView setView = (RecyclerView) view.findViewById(R.id.set_rv);

        rarityView.setHasFixedSize(true);
        factionView.setHasFixedSize(true);
        setView.setHasFixedSize(true);

        rarityView.setLayoutManager(new LinearLayoutManager(getContext()));
        factionView.setLayoutManager(new LinearLayoutManager(getContext()));
        setView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        CTVAdapter rarityAdapter = new CTVAdapter(rarity, FilterDialogFragment.this);
        CTVAdapter factionAdapter = new CTVAdapter(faction, FilterDialogFragment.this);
        CTVAdapter setAdapter = new CTVAdapter(set, FilterDialogFragment.this);

        rarityView.setAdapter(rarityAdapter);
        factionView.setAdapter(factionAdapter);
        setView.setAdapter(setAdapter);

        // finish building dialog
        builder.setView(view).setTitle(R.string.filter_cards)
                .setPositiveButton(R.string.filter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogListener = (FilterDialogListener) getTargetFragment();
                        dialogListener.onClickFilter(selectedFilters);
                        getDialog().dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getDialog().dismiss();
                    }
                });
        return builder.create();
    }

    @Override
    public void onSaveInstanceState (Bundle savedInstanceState) {
        savedInstanceState.putSerializable(KEY_FILTERS, selectedFilters);
        super.onSaveInstanceState(savedInstanceState);
    }

    public HashMap<String, Boolean> getSelectedFilters () { return this.selectedFilters; }
    public void setSelectedFilters (HashMap<String, Boolean> vals) {
        this.selectedFilters = new HashMap<>(vals);
    }
    public boolean isItemSelected (String item) { return this.selectedFilters.get(item); }
    public void setItemSelected (String item, boolean val) { this.selectedFilters.put(item,
            val); }

}

class CTVAdapter extends RecyclerView.Adapter<CTVAdapter.CTVHolder> {
    private String[] data;
    private FilterDialogFragment fragment;

    public CTVAdapter (String[] data, FilterDialogFragment fragment) {
        this.data = data;
        this.fragment = fragment;
    }

    public class CTVHolder extends RecyclerView.ViewHolder{
        CheckBox cb;
        TextView filterVal;

        CTVHolder (View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cb.toggle();
                    fragment.setItemSelected(data[getAdapterPosition()], cb.isChecked());
                }
            });

            filterVal = (TextView) itemView.findViewById(R.id.filter_list_text);

            cb = (CheckBox) itemView.findViewById(R.id.filter_check_box);
            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.setItemSelected(data[getAdapterPosition()], cb.isChecked());
                }
            });
        }
    }

    @Override
    public int getItemCount() { return data.length; }

    public CTVHolder onCreateViewHolder (ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_list_items,
                viewGroup, false);
        return new CTVHolder(view);
    }

    @Override
    public void onBindViewHolder (CTVHolder ctvHolder, int i) {
        ctvHolder.filterVal.setText(data[i]);
        ctvHolder.cb.setChecked(fragment.isItemSelected(data[i]));
    }

    @Override
    public void onAttachedToRecyclerView (RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
