import java.util.HashMap;
// The API requests class information as an integer identifier instead of the class name, can use this to
// search the class and get the integer back

public class GuildChar {
    static final HashMap<Integer, String> PlayClasses = new HashMap<Integer, String>(){{

        put(1, "Warrior");
        put(2, "Paladin");
        put(3, "Hunter");
        put(4, "Rogue");
        put(5, "Priest");
        put(6, "Death Knight");
        put(7, "Shaman");
        put(8, "Mage");
        put(9, "Warlock");
        put(10, "Monk");
        put(11, "Druid");
        put(12, "Demon Hunter");

    }};
}
