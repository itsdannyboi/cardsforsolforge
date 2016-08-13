package com.solforge.carddbforsolforge;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

class Deck implements Parcelable {

    public static final String ALLOYIN = "Alloyin";
    public static final String NEKRIUM = "Nekrium";
    public static final String TEMPYS = "Tempys";
    public static final String UTERRA = "Uterra";
    public static final String ERROR = "Something Went Wrong (-~-)";

    private String deckName;
    private ArrayList<Card> deck;
    private HashMap<String, Integer> cardCount;
    private HashMap<String, Integer> factions;

    public Deck () {
        // initialize at 35 so that there can be overflow, limits resizing of deck
        this.deckName = null;
        this.deck = new ArrayList<>(35);
        this.cardCount = new HashMap<>();
        this.factions = new HashMap<>(4);

        factions.put(ALLOYIN, 0);
        factions.put(NEKRIUM, 0);
        factions.put(TEMPYS, 0);
        factions.put(UTERRA, 0);
    }

    public Deck (List<Card> input) {
        this();
        this.deck.addAll(input);
    }

    public Deck (List<Card> input, String deckName) {
        this(input);
        this.deckName = deckName;
    }

    public Deck (List<Card> input, HashMap<String, Integer> cardCounts) {
        this(input);
        this.cardCount = new HashMap<>(cardCounts);
    }

    public Deck (List<Card> input, HashMap<String, Integer> cardCounts, HashMap<String, Integer> factions) {
        this(input, cardCounts);
        this.factions = new HashMap<>(factions);
    }

    public Deck (List<Card> input, HashMap<String, Integer> cardCounts, HashMap<String, Integer> factions, String deckName) {
        this(input, cardCounts, factions);
        this.deckName = deckName;
    }

    protected Deck (Parcel in) {
        this.deckName = in.readString();
        in.readTypedList(this.deck, Card.CREATOR);
        this.cardCount = (HashMap<String, Integer>) in.readSerializable();
        this.factions = (HashMap<String, Integer>) in.readSerializable();
    }

    public void writeToParcel (Parcel out, int flags) {
        out.writeString(deckName);
        out.writeTypedList(deck);
        out.writeSerializable(cardCount);
        out.writeSerializable(factions);
    }

    public int describeContents () { return 0; }

    public static final Parcelable.Creator<Deck> CREATOR = new Parcelable.Creator<Deck> () {
        public Deck createFromParcel (Parcel in) {
            return new Deck(in);
        }

        public Deck[] newArray (int size) {
            return new Deck[size];
        }
    };

    @Override
    public boolean equals (Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (!(obj instanceof Deck)) { return false; }
        Deck other = (Deck) obj;

        if (this.getDeck().size() == other.getDeck().size()) {
            boolean runningEquals = this.getDeckName().equals(other.getDeckName());
            for (int i = 0; i < this.getDeck().size(); i++) {
                Card thisCard = this.getDeck().get(i);
                Card otherCard = other.getDeck().get(i);
                runningEquals = runningEquals && thisCard.equals(otherCard) &&
                        (this.getCardCount().get(thisCard.getName())
                                .equals(other.getCardCount().get(otherCard.getName())));
            }
            runningEquals = runningEquals &&
                    (this.getFactionCount(ALLOYIN) == other.getFactionCount(ALLOYIN)) &&
                    (this.getFactionCount(NEKRIUM) == other.getFactionCount(NEKRIUM)) &&
                    (this.getFactionCount(TEMPYS) == other.getFactionCount(TEMPYS)) &&
                    (this.getFactionCount(UTERRA) == other.getFactionCount(UTERRA));
            return runningEquals;
        }
        return false;
    }

    @Override
    public int hashCode () {
        int result = 17;
        result = 42 * result + this.deckName.hashCode();
        for (int i = 0; i < this.deck.size(); i++) {
            result = 42 + result + this.deck.get(i).hashCode();
            result = 42 + result + this.cardCount.get(deck.get(i).getName());
        }
        result = 42 * result + this.factions.get(ALLOYIN);
        result = 42 * result + this.factions.get(NEKRIUM);
        result = 42 * result + this.factions.get(TEMPYS);
        result = 42 * result + this.factions.get(UTERRA);
        return result;
    }

    // utility functions
    public void addCard (Card card) {
        // if the deck already has the card, increase the cards count in the deck
        // otherwise if the card is not in the deck, add it and initialize it's count
        if (deck.contains(card)) {
            cardCount.put(card.getName(), cardCount.get(card.getName()) + 1);
        } else {
            deck.add(card);
            cardCount.put(card.getName(), 1);
            factions.put(card.getFaction(), (factions.get(card.getFaction()) + 1));
        }
    }

    public boolean removeCard (Card card) {
        // upon being asked to remove a card, see if the deck has card
        // if the deck has the card, and the number is greater than 0, decrease number in deck by 1
        // if the deck has the card, and the number is 0 or less, remove entry from deck
        // if the card is not in the deck, do nothing and return false (card not removed)
        if (deck.contains(card)) {
            cardCount.put(card.getName(), cardCount.get(card.getName()) - 1);
            if (cardCount.get(card.getName()) <= 0) {
                cardCount.put(card.getName(), 0);
                factions.put(card.getFaction(), (factions.get(card.getFaction()) - 1));
                return deck.remove(card);
            }
            return true;
        }
        return false;
    }

    public String getWarning () {
        // use return string as warning message
        // if return is null, no warning
        String warning = null;
        if (deck.size() > 30) {
            warning = "Deck Exceeds 30 Cards";
        }
        if (deck.size() < 30) {
            warning = "Deck Does Not Contain 30 Cards";
        }
        if (hasMoreThanThree()) {
            if (warning != null) {
                warning = warning + "/n" + "Deck Contains More Than 3 Of A Card";
            } else {
                warning = "Deck Contains More Than 3 Of A Card";
            }
        }
        if (hasTooManyFactions()) {
            if (warning != null) {
                warning = warning + "/n" + "Deck Exceeds 2 Factions";
            } else {
                warning = "Deck Exceeds 2 Factions";
            }
        }

        return warning;
    }

    public boolean hasWarning () {
        return getWarning() != null;
    }

    public static JSONObject writeDeckToJSONObject (Deck input) {
        JSONObject output = new JSONObject();
        JSONArray deckOutput = new JSONArray();
        try {
            for (Card card : input.getDeck()) {
                JSONObject cardOutput = new JSONObject();
                cardOutput.put("cardName", card.getName());
                cardOutput.put("cardCount", input.getCardCount().get(card.getName()));
                deckOutput.put(cardOutput);
            }
            output.put("deckName", input.getDeckName());
            output.put("deck", deckOutput);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static String writeDeckToJSON (Deck input) {
        try {
            return writeDeckToJSONObject(input).toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray writeDecksToJSONArray (List<Deck> input) {
        JSONArray decksAsJSONArray = new JSONArray();
        for (Deck deck : input) {
            JSONObject deckAsJSON = writeDeckToJSONObject(deck);
            if (deckAsJSON != null) {
                decksAsJSONArray.put(deckAsJSON);
            }
        }
        return decksAsJSONArray;
    }

    public static String writeDecksToJSON (List<Deck> input) {
        try {
            return writeDecksToJSONArray(input).toString(4);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Deck readDeckFromJSONObject (JSONObject obj, List<Card> cardDatabase) {
        try {
            String deckName = Card.isValid(obj, "deckName") ? obj.getString("deckName") : ERROR;

            JSONArray deckJSON = Card.isValid(obj, "deck") ? obj.getJSONArray("deck") : null;
            ArrayList<Card> deck = new ArrayList<>(35);
            HashMap<String, Integer> count = new HashMap<>();
            HashMap<String, Integer> factions = new HashMap<>(4);
            factions.put(ALLOYIN, 0);
            factions.put(NEKRIUM, 0);
            factions.put(TEMPYS, 0);
            factions.put(UTERRA, 0);

            int length;
            if (deckJSON == null) {
                length = 0;
            } else {
                length = deckJSON.length();
            }

            for (int i = 0; i < length; i++) {
                JSONObject temp = deckJSON.getJSONObject(i);
                String cardName = Card.isValid(temp, "cardName") ?
                        temp.getString("cardName") : ERROR;
                int cardCount = Card.isValid(temp, "cardCount") ?
                        temp.getInt("cardCount") : 0;

                Card currCard = cardLookup(cardName, cardDatabase);
                if (currCard != null) {
                    deck.add(currCard);
                    count.put(cardName, cardCount);
                    factions.put(currCard.getFaction(), (factions.get(currCard.getFaction())
                            + cardCount));
                }
            }
            return new Deck(deck, count, factions, deckName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Deck readDeckFromJSON (String input, List<Card> cardDatabase) {
        try {
            JSONObject obj = new JSONObject(input);
            return readDeckFromJSONObject(obj, cardDatabase);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Deck> readDeckArrayFromJSON (String decksFileData,
                                                         List<Card> cardDatabase) {
        ArrayList<Deck> parsedDecks = new ArrayList<>();
        try {
            JSONArray decks = new JSONArray(decksFileData);
            for (int i = 0; i < decks.length(); i++) {
                JSONObject obj = decks.getJSONObject(i);
                Deck tempDeck = readDeckFromJSONObject(obj, cardDatabase);
                if (tempDeck != null) {
                    parsedDecks.add(tempDeck);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parsedDecks;
    }


    // local functions
    private boolean hasMoreThanThree () {
        Collection<Integer> values = cardCount.values();
        for (Integer val : values) {
            if (val > 3) {
                return true;
            }
        }
        return false;
    }

    private boolean hasTooManyFactions () {
        int factionNum = 0;

        if (factions.get(ALLOYIN) > 0) { factionNum++; }
        if (factions.get(NEKRIUM) > 0) { factionNum++; }
        if (factions.get(TEMPYS) > 0) { factionNum++; }
        if (factions.get(UTERRA) > 0) { factionNum++; }

        return factionNum > 2;
    }

    private static Card cardLookup (String cardName, List<Card> cardDatabase) {
        for (Card card : cardDatabase) {
            if (card.getName().equals(cardName)) {
                return card;
            }
        }
        return null;
    }

    // getters/setters
    public String getDeckName () { return  this.deckName; }
    public List<Card> getDeck () { return this.deck; }
    public Card getCard (int position) { return this.deck.get(position); }
    public HashMap<String, Integer> getCardCount () { return this.cardCount; }
    public int getCardCount (Card card) { return this.cardCount.get(card.getName()); }
    public int getCardCount (String name) { return this.cardCount.get(name); }
    public HashMap<String, Integer> getFactionCount () { return this.factions; }
    public int getFactionCount (String factionName) { return this.factions.get(factionName); }
    public void setDeckName (String val) { this.deckName = val; }
    public void setDeck (List<Card> vals) { this.deck = new ArrayList<>(vals); }
    public void setCardCount (HashMap<String, Integer> vals) { this.cardCount = new HashMap<>(vals); }
    public void setCardCount (Card card, int val) { this.cardCount.put(card.getName(), val); }
    public void setCardCount (String name, int val) { this.cardCount.put(name, val); }

}