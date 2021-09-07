/*
    Holds multiple fields, as well as possible future ones if wanted in the future.
    Needed are the TalentList, MediaTalentList, Name, Class, and Spec.
 */

import java.util.ArrayList;
import java.util.List;
public class CharacterProfile {

        List<String> TalentList = new ArrayList<String>();
        List<String> MediaTalentList = new ArrayList<>();
        String CharacterName;
        String CharacterGender;
        String CharacterFaction;
        String CharacterClass;
        public String CharacterRealm;
        int CharacterSpec;

        void setCharacterName(String CName) {
            this.CharacterName = CName;
        }
        String getCharacterName() {
            return this.CharacterName;
        }

        void setCharacterRealm(String CServer) {
            this.CharacterRealm = CServer;
        }
        String getCharacterRealm() {
            return this.CharacterRealm;
        }

        void setCharacterClass(String CClass) {
            this.CharacterClass = CClass;
        }
        String getCharacterClass() {
            return this.CharacterClass;
        }

        void setCharacterSpec(int CSpec) {
            this.CharacterSpec = CSpec;
        }
        int getCharacterSpec() {
            return this.CharacterSpec;
        }

    }

