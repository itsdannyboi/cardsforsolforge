package com.solforge.carddbforsolforge;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_EX_STORAGE = "externalStorage";
    private static final String KEY_FILEPATH = "filePath";
    public static final String FRAG_IDENTIFIER = "currentFrag";

    private DrawerLayout drawer;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;

    private Class fragmentClass = null;
    private Fragment fragment = null;
    private static List<Card> cardsDatabase;
    private static List<Deck> deckList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (cardsDatabase == null) { cardsDatabase = initCardsDatabase(); }
        if (deckList == null) { deckList = initDeckList(); }

        if (savedInstanceState == null) {
            fragmentClass = CardSearchFrag.class;
            instantiateFrag(fragmentClass);
        }

        // Toolbar to replace the Action Braaaaa
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Drawer making time
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(drawerToggle);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        setupDrawerContent(navigationView);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.action_settings:
                // case for settings
                return true;
            default:
                // if we get here, the user's action was not recognized
                // using superclass
                return super.onOptionsItemSelected(item);
        }
    }

    // local functions
    private void setupDrawerContent (NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                }
        );
    }

    private void selectDrawerItem (MenuItem menuItem) {
        // Creates and shows frag to view content
        switch(menuItem.getItemId()) {
            case R.id.card_search:
                fragmentClass = CardSearchFrag.class;
                break;
            case R.id.deck_builder:
                fragmentClass = DeckViewerFrag.class;
                break;
            case R.id.action_settings:
                fragmentClass = SettingsFrag.class;
                break;
            case R.id.donate:
                fragmentClass = DonateFrag.class;
                break;
            default:
                fragmentClass = CardSearchFrag.class;
                break;
        }

        instantiateFrag(fragmentClass);

        // Highlight selection in nav drawer
        menuItem.setChecked(true);
        // Action bra title
        setTitle(menuItem.getTitle());
        // Close drawer
        drawer.closeDrawers();
    }

    private void instantiateFrag(Class fragmentClass) {
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment currFrag = getSupportFragmentManager().findFragmentByTag(FRAG_IDENTIFIER);
                if (currFrag instanceof DeckViewerFrag) {
                    ((DeckViewerFrag) currFrag).exitEditDeck();
                }
            }
        });
        fragmentManager.beginTransaction()
                .replace(R.id.frag_content, fragment, FRAG_IDENTIFIER)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    private List<Card> initCardsDatabase() {
        List<Card> temp = new ArrayList<>();
        try {
            InputStream cardsFromFile = this.getAssets().open("cardDB.json");
            temp = Card.readCardDatabaseFromJSON(loadJSON(cardsFromFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return temp;
    }

    private List<Deck> initDeckList () {
        List<Deck> temp = new ArrayList<>();
        try {
            FileInputStream decksFromFile = openFileInput(getString(R.string.saved_decks));
            temp = Deck.readDeckArrayFromJSON(loadJSON(decksFromFile), cardsDatabase);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return temp;
    }

    private String loadJSON(InputStream is) {
        String json = null;
        try {
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

    // Application Wide Utility Functions
    public static List<Card> getCardsDatabase () { return cardsDatabase; }
    public static List<Deck> getDeckList () { return deckList; }
    public static void setDeckList (List<Deck> input) { deckList = new ArrayList<>(input); }

    public static boolean isExStorageWritable () {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public static boolean isExStorageReadable () {
        String state = Environment.getExternalStorageState();
        return isExStorageWritable() || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

    // make sure to setDecks before using saveDecks
    public void saveDecks () {
        String deckListAsJSON = Deck.writeDecksToJSON(deckList);

        if (deckListAsJSON != null) {
            try {
                FileOutputStream fileOutputStream = openFileOutput(getString(R.string.saved_decks),
                        Context.MODE_PRIVATE);
                fileOutputStream.write(deckListAsJSON.getBytes());
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: implement error snackbar/toast
        }
    }

    public void saveDeck (Deck deckToSave) {
        if (deckList.contains(deckToSave)) {
            int selectedDeck = deckList.indexOf(deckToSave);
            deckList.set(selectedDeck, deckToSave);
        } else {
            deckList.add(deckToSave);
        }
        saveDecks();
    }
}
