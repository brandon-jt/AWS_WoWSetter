import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
    Used as a reference for the main loops to ensure every class and every spec is iterated through
*/
public class ClassAndSpec
{
    public HashMap<String, ArrayList<String>> ClassAndSpecList = new HashMap<>();
    public ArrayList<String> DeathKnightSpecs = new ArrayList<>(List.of("Frost", "Unholy", "Blood"));
    public ArrayList<String> DemonHunterSpecs = new ArrayList<>(List.of("Havoc", "Vengeance"));
    public ArrayList<String> DruidSpecs = new ArrayList<>(List.of("Feral", "Balance", "Guardian", "Restoration"));
    public ArrayList<String> HunterSpecs = new ArrayList<>(List.of("Survival", "Beast Mastery", "Marksmanship"));
    public ArrayList<String> MageSpecs = new ArrayList<>(List.of("Arcane", "Fire", "Frost"));
    public ArrayList<String> MonkSpecs = new ArrayList<>(List.of("Windwalker", "Brewmaster", "Mistweaver"));
    public ArrayList<String> PaladinSpecs = new ArrayList<>(List.of("Retribution", "Holy", "Protection"));
    public ArrayList<String> PriestSpecs = new ArrayList<>(List.of("Holy", "Discipline", "Shadow"));
    public ArrayList<String> RogueSpecs = new ArrayList<>(List.of("Subtlety", "Outlaw", "Assassination"));
    public ArrayList<String> ShamanSpecs = new ArrayList<>(List.of("Elemental", "Enhancement", "Restoration"));
    public ArrayList<String> WarlockSpecs = new ArrayList<>(List.of("Affliction", "Destruction", "Demonology"));
    public ArrayList<String> WarriorSpecs = new ArrayList<>(List.of("Fury", "Arms", "Protection"));

    ClassAndSpec(){

        this.ClassAndSpecList.put("Death Knight", DeathKnightSpecs);
        this.ClassAndSpecList.put("Demon Hunter", DemonHunterSpecs);
        this.ClassAndSpecList.put("Druid", DruidSpecs);
        this.ClassAndSpecList.put("Hunter", HunterSpecs);
        this.ClassAndSpecList.put("Mage", MageSpecs );
        this.ClassAndSpecList.put("Monk", MonkSpecs );
        this.ClassAndSpecList.put("Paladin", PaladinSpecs);
        this.ClassAndSpecList.put("Priest", PriestSpecs);
        this.ClassAndSpecList.put("Rogue", RogueSpecs);
        this.ClassAndSpecList.put("Shaman", ShamanSpecs);
        this.ClassAndSpecList.put("Warlock", WarlockSpecs);
        this.ClassAndSpecList.put("Warrior", WarriorSpecs);

    }
}
