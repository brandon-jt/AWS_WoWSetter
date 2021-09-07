import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/*
    Maps that hold the Class Names and their Specializations with their Specialization ID as an integer, needed
    in API calls for getting information pertaining to the spec.

    The maps need to be split up since some classes have same spec names.
 */
public class ClassSpecConvert {

    private static final ArrayList<String> Map1Classes = new ArrayList<String>(
            Arrays.asList("Mage", "Paladin", "Druid", "Hunter", "Rogue", "Warlock", "Monk", "Demon Hunter")
    );

    private static final Map<String, Integer> SpecNums1 = Map.ofEntries(
            new SimpleEntry<String, Integer>("Arcane", 62), new SimpleEntry<String, Integer>("Fire", 63), new SimpleEntry<String, Integer>("Frost", 64),
            new SimpleEntry<String, Integer>("Holy", 65), new SimpleEntry<String, Integer>("Protection", 66),new SimpleEntry<String, Integer>("Retribution", 70),
            new SimpleEntry<String, Integer>("Balance", 102), new SimpleEntry<String, Integer>("Feral", 103), new SimpleEntry<String, Integer>("Guardian", 104 ), new SimpleEntry<String, Integer>("Restoration", 105),
            new SimpleEntry<String, Integer>("Beast Mastery", 253), new SimpleEntry<String, Integer>("Marksmanship", 254), new SimpleEntry<String, Integer>("Survival", 255),
            new SimpleEntry<String, Integer>("Assassination", 259), new SimpleEntry<String, Integer>("Outlaw", 260), new SimpleEntry<String, Integer>("Subtlety", 261),
            new SimpleEntry<String, Integer>("Affliction", 265) ,new SimpleEntry<String, Integer>("Demonology", 266), new SimpleEntry<String, Integer>("Destruction", 267),
            new SimpleEntry<String, Integer>("Brewmaster", 268), new SimpleEntry<String, Integer>("Windwalker", 269), new SimpleEntry<String, Integer>("Mistweaver", 270),
            new SimpleEntry<String, Integer>("Havoc", 577), new SimpleEntry<String, Integer>("Vengeance", 581));


    private static final Map<String, Integer> SpecNums2 = Map.ofEntries(
            new SimpleEntry<String, Integer>("Arms", 71), new SimpleEntry<String, Integer>("Fury", 72), new SimpleEntry<String, Integer>("Protection", 73),
            new SimpleEntry<String, Integer>("Blood", 250), new SimpleEntry<String, Integer>("Frost", 251), new SimpleEntry<String, Integer>("Unholy", 252),
            new SimpleEntry<String, Integer>("Discipline", 256), new SimpleEntry<String, Integer>("Holy", 257), new SimpleEntry<String, Integer>("Shadow", 258),
            new SimpleEntry<String, Integer>("Elemental", 262), new SimpleEntry<String, Integer>("Enhancement", 263), new SimpleEntry<String, Integer>("Restoration", 264)
    );

    // Helper function to return the integer identifier of a specialization string
    public static int SpecToInt(String chosenClass, String chosenSpec) {
        if (Map1Classes.contains(chosenClass)) {
            return SpecNums1.get(chosenSpec);
        }
        else
            return SpecNums2.get(chosenSpec);


    }
}
