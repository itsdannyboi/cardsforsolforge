package com.solforge.carddbforsolforge;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.jar.Pack200;

/*
*
* id - identifier for card
* name - card name
* rarity - card rarity
* type - Creature or Spell
* faction - Alloyin, Nekrium, Tempys, or Uterra
* desc - string array of descriptions; level 1 -> index 0
* tribe - string array of tribes for creatures; level 1 -> index 0
* set - set number card is from
* atk - int array of atk values; level 1 -> index 0
* health - int array of health values; level 1 -> index 0
* draft - bool stating whether card is in draft pool
* levels - bool array of card levels; has level 2 -> index 0
*
*
* author: its_dannyboi
*/

class Card implements Comparable<Card> {
    private String id;
    private String name;
    private String rarity;
    private String type;
    private String type3 = "mt";
    private String faction;
    private String[] desc = new String[4];
    private String[] tribe = new String[4];
    private String[] images = new String[4];

    private int set;
    private int[] atk = new int[4];
    private int[] health = new int[4];

    private boolean draft;
    private boolean[] levels = new boolean[3];
    private static String error = "Trouble Loading";

    public Card (JSONObject obj) {
        // basic values every card has
        try {
            id = isValid(obj, "id") ? obj.getString("id") : error;
            name = parseApostrophes(parseStringEndingSpace(
                    isValid(obj, "name") ? obj.getString("name") : error));
            rarity = parseStringEndingSpace(isValid(obj, "rarity")
                    ? obj.getString("rarity") : error);
            type = parseStringEndingSpace(isValid(obj, "type") ? obj.getString("type") : error);
            faction = parseStringEndingSpace(isValid(obj, "faction")
                    ? obj.getString("faction") : error);
            set = isValid(obj, "set") ? obj.getInt("set") : 0;
            draft = isValid(obj, "draft") && obj.getBoolean("draft");

            // try for type 3, only needed for
            // Demara, Vaerus, Sulgrim, Chiron
            if (isValid(obj, "type3")) {
                type3 = obj.getString("type3");
            }


            // handle descriptions that creatures and spells both have
            // however, there can be between 1-4 desc, so logic tests
            // and fills in if they are there.
            // using .isNull instead of .has because isNull checks for
            // null AND has; true if Null or no mapping
            desc[0] = isValid(obj, "desc1") ? obj.getString("desc1") : error;
            if (isValid(obj, "desc2")) {
                desc[1] = obj.getString("desc2");
                levels[0] = true;

                if (isValid(obj, "desc3")) {
                    desc[2] = obj.getString("desc3");
                    levels[1] = true;

                    if (isValid(obj, "desc4")) {
                        desc[3] = obj.getString("desc4");
                        levels[2] = true;
                    } else {
                        levels[2] = false;
                        desc[3] = error;
                    }
                } else {
                    Arrays.fill(levels, 1, 2, false);
                    Arrays.fill(desc, 2, 3, error);
                }
            } else {
                Arrays.fill(levels, false);
                Arrays.fill(desc, 1, 3, error);
            } // end description handling

            // image handling
            if (type.equals("Spell") && type3.equals("mt")) {
                Arrays.fill(images, (name.replaceAll(" ", "_") + ".jpg"));
            } else {
                images[0] = name.replaceAll(" ", "_") + "_1" + ".jpg";
                for (int i=0; i < 3; i++) {
                    if (levels[i]) {
                        images[i+1] = name.replaceAll(" ", "_") + "_"
                                + Integer.toString(i+2) + ".jpg";
                    } else {
                        // this is here to stop evaluating the for loop if there is no more info
                        // ie. in the case of Swampmoss Ancient, there is no level 3 or 4 data
                        // so at i = 2 levels[2] = false; ->
                        // which makes i=4 and then the for doesn't continue
                        i = 4;
                    }
                }
            } // end image handling

            // if the card is a spell, it doesn't have these attributes
            if (type.equals("Creature")) {
                for (int i=0; i < 4; i++) {
                    if (i==0 || levels[i-1]) {
                        tribe[i] = obj.getString("tribe" + (i+1));
                        atk[i] = obj.getInt("atk" + (i+1));
                        health[i] = obj.getInt("health" + (i+1));

                    } else {
                        tribe[i] = null;
                        atk[i] = 0;
                        health[i] = 0;
                    }
                }
            } // end creature handling
        } catch (JSONException e) {
            id = null;
            name = null;
            rarity = null;
            type = null;
            faction = null;
            set = 0;
            draft = false;
            Arrays.fill(desc, null);
            Arrays.fill(levels, false);
            Arrays.fill(atk, 0);
            Arrays.fill(health, 0);
            Arrays.fill(tribe, null);
            Arrays.fill(images, null);
            e.printStackTrace();
        }
    }

    @Override
    public int compareTo (Card other) {
        return ((other != null) ? this.getName().compareTo(((Card) other).getName()) : -1);
    }

    private String parseApostrophes(String input) {
        return (input.contains("’") ? input.replaceAll("’", "'") : input);
    }

    private String parseStringEndingSpace (String input) {

        if (input.endsWith(" ")) {
            return input.substring(0, input.length() - 1);
        }
        return input;

    }

    private boolean isValid(JSONObject object, String desc) {
        return (!(object.isNull(desc)));
    }

    // getters
    public String getId () { return id; }
    public String getName () { return name; }
    public String getRarity () { return rarity; }
    public String getType () { return type; }
    public String getFaction () { return faction; }
    public String[] getDesc () { return desc; }
    public String getDesc (int i) { return desc[i]; }
    public String[] getTribe () { return tribe; }
    public String getTribe (int i) { return tribe[i]; }
    public String[] getImage () { return images; }
    public String getImage (int i) { return images[i]; }
    public int getSet () { return set; }
    public int[] getAtk () { return atk; }
    public int getAtk (int i) { return atk[i]; }
    public int[] getHealth () { return health; }
    public int getHealth (int i) { return health[i]; }
    public boolean getDraft () { return draft; }
    public boolean[] getLevels () { return levels; }
    public boolean getLevels (int i) { return levels[i]; }

    // setters
    public void setId (String val) { this.id = val; }
    public void setName (String val) { this.name = val; }
    public void setRarity (String val) { this.rarity = val; }
    public void setType (String val) { this.type = val; }
    public void setFaction (String val) { this.faction = val; }
    public void setDesc (String[] val) { this.desc = val; }
    public void setDesc (String val, int i) { this.desc[i] = val; }
    public void setTribe (String[] val) { this.tribe = val; }
    public void setTribe (String val, int i) { this.tribe[i] = val; }
    public void setImage (String[] val) { this.images = val; }
    public void setImage (String val, int i) { this.images[i] = val; }
    public void setSet (int val) { this.set = val; }
    public void setAtk (int[] val) { this.atk = val; }
    public void setAtk (int val, int i) { this.atk[i] = val; }
    public void setHealth (int[] val) { this.health = val; }
    public void setHealth (int val, int i) { this.health[i] = val; }
    public void setDraft (boolean val) { this.draft = val; }
    public void setLevels (boolean[] val) { this.levels = val; }
    public void setLevels (boolean val, int i) { this.levels[i] = val; }

    // Comparators

    static class BySet implements Comparator<Card> {
        public int compare (Card card1, Card card2) {
            if (card1.getSet() == card2.getSet()) {
                return card1.getName().compareTo(card2.getName());
            } else {
                return card1.getSet() - card2.getSet();
            }
        }
    }

    static class ByRarity implements Comparator<Card> {
        public int compare (Card card1, Card card2) {
            if (card1.getRarity().equals(card2.getRarity())) {
                return card1.getName().compareTo(card2.getName());
            } else {
                return cardRarity(card2) - cardRarity(card1);
            }
        }

        private int cardRarity (Card input) {
            switch (input.getRarity()) {
                case "Legendary":
                    return 4;
                case "Heroic":
                    return 3;
                case "Rare":
                    return 2;
                case "Common":
                    return 1;
                default:
                    return 0;
            }
        }
    }

    static class ByFaction implements Comparator<Card> {
        public int compare (Card card1, Card card2) {
            if (card1.getFaction().equals(card2.getFaction())) {
                return card1.getName().compareTo(card2.getName());
            } else {
                return card1.getFaction().compareTo(card2.getFaction());
            }
        }
    }

    static class ByType implements Comparator<Card> {
        public int compare (Card card1, Card card2) {
            // compare types (Creature/Spell)
            if (card1.getType().equals(card2.getType())) {
                // if both are Creature, sort by Tribe: ie Human, Zombie, etc
                if (card1.getType().equals("Creature")) {
                    if (card1.getTribe(0).equals(card2.getTribe(0))) {
                        return card1.getName().compareTo(card2.getName());
                    } else {
                        return card1.getTribe(0).compareTo(card2.getTribe(0));
                    }
                }
                return card1.getName().compareTo(card2.getName());
            } else {
                return card1.getType().compareTo(card2.getType());
            }
        }
    }
}