import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.sql.*;
import java.util.Objects;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;

public class Program {

    public static String AToken;
    public static final int MAXNUMCHARS = 1;
    public static HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    public static List<List<String>> MediaList = new ArrayList<>();
    public static List<String> DescriptionList = new ArrayList<>();

    /*
        Builds the url required for multiple API Calls
     */
    public static String urlBuilder(String baseUrl, String CName, String CServer) {
        return baseUrl.replace("{realmSlug}", CServer).replace("{characterName}", CName);
    }

    /*
        Takes the built URL for an API call and executes it.
        If rate limited, will sleep for a second and re-call the request.
        Returns the JSON for the response body
     */
    public static JsonObject getResponse(String url) throws IOException, InterruptedException {
        HttpRequest theResponse = HttpRequest.newBuilder().GET().uri(
                        URI.create(url))
                .build();

        HttpResponse<String> specResponse = client.send(theResponse, HttpResponse.BodyHandlers.ofString());
        System.out.println("ResponseCode=" + specResponse.statusCode());
        while (specResponse.statusCode() == 429) {
            Thread.sleep(1000);
            specResponse = client.send(theResponse, HttpResponse.BodyHandlers.ofString());
        }

        return new Gson().fromJson(specResponse.body(), JsonObject.class);

    }

    /*
        Gets the url for the picture of the requested Talent, adds it to the MediaTalentList
    */
    public static void MediaURLGet(CharacterProfile currentProfile, JsonObject getTalentInfo) throws IOException, InterruptedException {
        String idToPicture = getTalentInfo.getAsJsonObject().get("spell").getAsJsonObject().get("id").getAsString();
        String idToPictureURL = "https://us.api.blizzard.com/data/wow/media/spell/" + idToPicture+  "?namespace=static-us&locale=en_US&access_token=" + AToken;
        JsonObject getIdToPictureURL = getResponse(idToPictureURL);

        currentProfile.MediaTalentList.add(getIdToPictureURL.getAsJsonObject().get("assets").getAsJsonArray().get(0).getAsJsonObject().get("value").toString());
    }


    /*
    This generates the picture urls of the talents as well as the description for each talent and adds them to their
    corresponding static list
     */
    public static void ProfileSetter(CharacterProfile currentProfile, String getSpecUrlBase, int requestedSpec, HashMap<String, Integer> talentInfo) throws IOException, InterruptedException {
        try {
            String getSpecUrlUpdated = urlBuilder(getSpecUrlBase, currentProfile.getCharacterName(), currentProfile.getCharacterRealm());
            JsonObject SpecA = getResponse(getSpecUrlUpdated);
            JsonArray SpecAJarray = SpecA.getAsJsonArray("specializations");
            JsonArray SpecATalents = null;

            for (int i = 0; i < SpecAJarray.size(); i++) {
                if (SpecAJarray.get(i).getAsJsonObject().get("specialization").getAsJsonObject().get("id").getAsInt() == requestedSpec)
                    SpecATalents = SpecAJarray.get(i).getAsJsonObject().get("talents").getAsJsonArray();   
            }
            for (int i = 0; i < Objects.requireNonNull(SpecATalents).size(); i++) {
                currentProfile.TalentList.add(SpecATalents.get(i).getAsJsonObject().get("talent").getAsJsonObject().get("name").getAsString());
            }
            for (int i = 0; i < currentProfile.TalentList.size(); i++) {
                String mediaID;
                mediaID = (talentInfo.get(currentProfile.TalentList.get(i))).toString();
                String idToTalentURL = "https://us.api.blizzard.com/data/wow/talent/" + mediaID + "?namespace=static-us&locale=en_US&access_token=" + AToken;
                JsonObject getTalentInfo = getResponse(idToTalentURL);

                if ((currentProfile.getCharacterClass().equals(getTalentInfo.getAsJsonObject().get("playable_class").getAsJsonObject().get("name").getAsString()))) {
                    DescriptionList.add(getTalentInfo.getAsJsonObject().get("description").getAsString());
                }
                MediaURLGet(currentProfile, getTalentInfo);
            }
            MediaList.add(currentProfile.MediaTalentList);

        }catch (NullPointerException e){
            System.out.println("Null Pointer exception caught");
            ProfileSetter(currentProfile,getSpecUrlBase,requestedSpec,talentInfo);
        }
    }

    /*
        This is the initial getGuildCharacter call, it attempts to grab a level 60 character that matches the
        class that is being requested.
     */
    public static void getGuildCharacter(JsonObject GuildResponse, String lookingClass, ArrayList<String> MatchingCharacters, ArrayList<String> RejectList) {
        int currClass;
        String FCharacter;

        for (int i = 0 ; i < GuildResponse.getAsJsonArray("members").size(); i++) {
            currClass = GuildResponse.getAsJsonArray("members").get(i).getAsJsonObject().get("character")
                    .getAsJsonObject().get("playable_class").getAsJsonObject().get("id").getAsInt();
            if (Objects.equals(GuildChar.PlayClasses.get(currClass), lookingClass)) {
                FCharacter = GuildResponse.getAsJsonArray("members").get(i).getAsJsonObject().get("character")
                        .getAsJsonObject().get("name").toString().replace("\"", "");

                if (GuildResponse.getAsJsonArray("members").get(i).getAsJsonObject().get("character")
                        .getAsJsonObject().get("level").getAsInt() == 60 && !RejectList.contains(FCharacter)) {
                    MatchingCharacters.add(FCharacter);
                    break;
                }
            }
        }
    }
    /*
        This version of the function returns the actual response for the guild character after a bad one is found
     */
    public static JsonElement getGuildCharacter(JsonObject GuildResponse, ArrayList<String> MatchingCharacters, RaidGuildLB theRGLB, String getSpecUrlBase, String lookingClass, ArrayList<String> RejectList, CharacterProfile PlayerChar) throws IOException, InterruptedException {
        System.out.println("Overloaded");
        MatchingCharacters.clear();
        int currClass;
        String FCharacter;

        for (int i = 0 ; i < GuildResponse.getAsJsonArray("members").size(); i++) {
            currClass = GuildResponse.getAsJsonArray("members").get(i).getAsJsonObject().get("character")
                    .getAsJsonObject().get("playable_class").getAsJsonObject().get("id").getAsInt();

            if (Objects.equals(GuildChar.PlayClasses.get(currClass), lookingClass)) {
                FCharacter = GuildResponse.getAsJsonArray("members").get(i).getAsJsonObject().get("character")
                        .getAsJsonObject().get("name").toString().replace("\"", "");

                if ((GuildResponse.getAsJsonArray("members").get(i).getAsJsonObject().get("character")
                        .getAsJsonObject().get("level").getAsInt() == 60) && !RejectList.contains(FCharacter)) {
                    MatchingCharacters.add(FCharacter);
                    break;
                }

            }
        }
        // Overloaded functionality
        String nextCharUrlBase = getSpecUrlBase.replace("{realmSlug}", theRGLB.realm).replace("{characterName}", MatchingCharacters.get(0).toLowerCase());
        JsonObject nextCharResponse = getResponse(nextCharUrlBase);

        for (JsonElement spec: nextCharResponse.getAsJsonArray("specializations")) {
            if (spec.getAsJsonObject().get("specialization").getAsJsonObject().get("id").getAsInt() == PlayerChar.getCharacterSpec()) {
                return spec;
            }
        }

        return nextCharResponse;
    }

    /*
        The following functions take a character from the guild and gets the response body for the character.
        If a problem is found with the character profile, it has a number
        of checks to go through and validate the next character.

        If the character profile and it's specializations are all filled out (all talents are taken, character is level
        60, and it's the correct class) it will stay in the matchingCharacters list and will be used to acquire
        talent information from.
     */

    // Called from checkSpecializations, adds to the RejectList, clears the MatchingCharacters list, and grabs a new character from the guild
    public static JsonElement getNewSpecFailover(ArrayList<String> RejectList, ArrayList<String> MatchingCharacters,
                                                 JsonObject GuildResponse, String lookingClass, String getSpecUrlBase, RaidGuildLB theRGLB,CharacterProfile PlayerChar ) throws IOException, InterruptedException {

        RejectList.add(MatchingCharacters.get(0));
        MatchingCharacters.clear();
        return getGuildCharacter(GuildResponse, MatchingCharacters, theRGLB, getSpecUrlBase, lookingClass, RejectList, PlayerChar);
    }

    // Called from checkSpecialization, performs similarly to the above and also grabs a new URL to extract character data from
    public static JsonObject getNewCharacterFailover(ArrayList<String> RejectList, ArrayList<String> MatchingCharacters,
                                                     JsonObject GuildResponse, String lookingClass, String getSpecUrlBase, RaidGuildLB theRGLB) throws IOException, InterruptedException {

        RejectList.add(MatchingCharacters.get(0));
        MatchingCharacters.clear();
        getGuildCharacter(GuildResponse, lookingClass, MatchingCharacters, RejectList);
        var newCharURLBase = getSpecUrlBase.replace("{realmSlug}", theRGLB.realm).replace("{characterName}", MatchingCharacters.get(0).toLowerCase());
        return getResponse(newCharURLBase);

    }
    // Checks that there are 7 talents, returns if true for every spec
    public static JsonObject TalentCheck(JsonObject nextCharResponse, ArrayList<String> RejectList, ArrayList<String> MatchingCharacters,
                                         JsonObject GuildResponse, String lookingClass, String getSpecUrlBase, RaidGuildLB theRGLB, int threeCounter) throws IOException, InterruptedException{
        for (var Specs : nextCharResponse.getAsJsonArray("specializations").getAsJsonArray()) {
            try {
                if (Specs.getAsJsonObject().get("talents").getAsJsonArray().size() < 7) {

                    nextCharResponse = getNewCharacterFailover(RejectList, MatchingCharacters,
                            GuildResponse, lookingClass,  getSpecUrlBase,theRGLB);

                } else {
                    threeCounter++;
                }

            } catch (java.lang.NullPointerException e) {
                nextCharResponse = getNewCharacterFailover(RejectList, MatchingCharacters,
                        GuildResponse, lookingClass,  getSpecUrlBase,theRGLB);
            }
        }
        return nextCharResponse;
    }

    /* Called from getMatchingCharacter, this function calls getNewCharacterFailover and TalentCheck to ensure
       the required specifications from the comment block prior.

       The idea here is that a majority of classes have 3 specializations (with consideration for druids having 4
       and demon hunters having 2). Once the talent checks have been confirmed for all the specializations,
       the loops will break and return the valid character.
     */
    public static JsonObject checkSpecializations(JsonObject nextCharResponse, ArrayList<String> RejectList, ArrayList<String> MatchingCharacters,
                                                  JsonObject GuildResponse, String lookingClass, String getSpecUrlBase, RaidGuildLB theRGLB) throws IOException, InterruptedException {

        int threeCounter = 0;
        while (threeCounter != 3) {
            for (var special : nextCharResponse.getAsJsonArray("specializations")) {
                if (threeCounter == 3) break;

                if (special.getAsJsonObject().size() < 3) {
                    int breakCounter = 0;
                    nextCharResponse = getNewCharacterFailover(RejectList, MatchingCharacters,
                            GuildResponse, lookingClass,  getSpecUrlBase,theRGLB);

                    {
                        for (var Specs : nextCharResponse.getAsJsonArray("specializations").getAsJsonArray()) {
                            try {
                                if (Specs.getAsJsonObject().get("talents").getAsJsonArray().size() >= 7) {
                                    breakCounter++;
                                }
                                if (breakCounter == 3) {
                                    System.out.println("Breaking");
                                    threeCounter = 3;
                                    break;
                                }

                            } catch (java.lang.NullPointerException e) {
                                nextCharResponse = getNewCharacterFailover(RejectList, MatchingCharacters,
                                        GuildResponse, lookingClass,  getSpecUrlBase,theRGLB);
                            }
                        }

                        if (threeCounter < 3) {
                            nextCharResponse = TalentCheck(nextCharResponse,  RejectList,  MatchingCharacters,
                                    GuildResponse, lookingClass,  getSpecUrlBase, theRGLB, threeCounter);
                        }
                    }
                }
            }
        }
        return nextCharResponse;
    }
    /*
        Helper of getMatchingCharacters, starts the checking of proper talent structure
     */
    public static void talentChecker(JsonElement spec,ArrayList<String> MatchingCharacters,ArrayList<String> MatchingSpec,
                                     String getSpecUrlBase, RaidGuildLB theRGLB, CharacterProfile PlayerChar,
                                     JsonObject GuildResponse, String lookingClass, ArrayList<String> RejectList,
                                     boolean invalidChar, JsonArray Spec2AJarray, JsonObject nextCharResponse, String FCharacters ){
        try {
            while (spec.getAsJsonObject().get("specialization").getAsJsonObject().get("id").getAsInt() == PlayerChar.getCharacterSpec()
                    && spec.getAsJsonObject().get("talents").getAsJsonArray().size() != 7 && invalidChar) {
                System.out.println("The requested spec is invalid");
                spec = getGuildCharacter(GuildResponse, MatchingCharacters, theRGLB, getSpecUrlBase, lookingClass, RejectList, PlayerChar);

                for (var special : nextCharResponse.getAsJsonArray("specializations")) {
                    if (special.getAsJsonObject().size() < 3) {
                        spec = getNewSpecFailover(RejectList,  MatchingCharacters,
                                GuildResponse, lookingClass, getSpecUrlBase, theRGLB, PlayerChar);
                    }
                }

                if (spec.getAsJsonObject().get("talents").getAsJsonArray().size() != 7) {
                    spec = getNewSpecFailover(RejectList,  MatchingCharacters,
                            GuildResponse, lookingClass, getSpecUrlBase, theRGLB, PlayerChar);
                }

                else {
                    invalidChar = false;
                    System.out.println("Suitable person found");
                }
            }
        } catch (NullPointerException | IOException | InterruptedException e) {
            RejectList.add(MatchingCharacters.get(0));
            MatchingCharacters.clear();
        }

        try {
            Spec2AJarray.get(0).getAsJsonObject().get("talents").getAsJsonArray();
            if (Spec2AJarray.get(0).getAsJsonObject().get("specialization").getAsJsonObject().get("id").getAsInt() == PlayerChar.getCharacterSpec()
                    && Spec2AJarray.get(0).getAsJsonObject().get("specialization").getAsJsonObject() != null) {
                MatchingSpec.add(FCharacters);
            }
        } catch (NullPointerException e) {
            System.out.println("Bad Character Profile");
        }

    }

    /*
        Begins to search for a character of the correct Class and Specialization that can be used as a model. Calls
        check specializations to see if the first character can be used as a model, if not uses helper functions
        getNewSpecFailover and talentChecker to attempt to get a proper character.
     */
    public static void getMatchingCharacters(ArrayList<String> MatchingCharacters,ArrayList<String> MatchingSpec, String getSpecUrlBase, RaidGuildLB theRGLB, CharacterProfile PlayerChar, JsonObject GuildResponse, String lookingClass) throws IOException, InterruptedException {
        boolean invalidChar = true;
        ArrayList<String> RejectList = new ArrayList<>();
        for (String FCharacters: MatchingCharacters) {

            String nextCharUrlBase = getSpecUrlBase.replace("{realmSlug}", theRGLB.realm).replace("{characterName}", FCharacters.toLowerCase());
            JsonObject nextCharResponse = getResponse(nextCharUrlBase);
            JsonArray Spec2AJarray = nextCharResponse.getAsJsonArray("specializations");

            int threeCheck = 0;
            for (var special : nextCharResponse.getAsJsonArray("specializations")) {
                if (special.getAsJsonObject().size() >= 3) {
                    threeCheck++;
                }
            }

            boolean checkNeeded = false;
            if (!lookingClass.equals("Demon Hunter")) {
                if (threeCheck < 3) {
                    checkNeeded = true;
                }
            }

            if (checkNeeded){
                nextCharResponse = checkSpecializations(nextCharResponse,  RejectList,  MatchingCharacters,
                        GuildResponse, lookingClass, getSpecUrlBase, theRGLB);
            }

            for (JsonElement spec: nextCharResponse.getAsJsonArray("specializations")) {
                if (nextCharResponse.getAsJsonArray("specializations").size() < 3 || spec.getAsJsonObject().size() < 3) {
                    spec = getNewSpecFailover(RejectList,  MatchingCharacters,
                            GuildResponse, lookingClass, getSpecUrlBase, theRGLB, PlayerChar);
                }

                try {
                    talentChecker(spec, MatchingCharacters, MatchingSpec, getSpecUrlBase, theRGLB,  PlayerChar,
                            GuildResponse, lookingClass, RejectList, invalidChar, Spec2AJarray, nextCharResponse, FCharacters );
                }
                catch (NullPointerException e) {
                    System.out.println("Bad Character Profile");
                }
            }
        }
    }

    
    public static void main(String[] args)throws IOException, InterruptedException, SQLException {

        // Initialize variables to be used to establish a database connection, then reads from the config file to
        // get proper credentials.
        String dburl = null;
        String dbName = null;
        String port = null;
        String username = null;
        String password = null;
        try (InputStream input = new FileInputStream("resources/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            AToken = prop.getProperty("AToken");
            dburl = prop.getProperty("dburl");
            dbName = prop.getProperty("dbName");
            port = prop.getProperty("port");
            username = prop.getProperty("username");
            password = prop.getProperty("password");

        } catch (IOException e){
            System.out.println("File could not be found/loaded");
        }

        // These urls will persist between loops, gets the information for the guild that will be iterated through.
        String getRaidGuildLB = "https://us.api.blizzard.com/data/wow/leaderboard/hall-of-fame/castle-nathria"
                + "/horde?namespace=dynamic-us&locale=en_US&access_token=" + AToken;
        String getGuild = "https://us.api.blizzard.com/data/wow/guild/{realmSlug}/{nameSlug}/"
                + "roster?namespace=profile-us&locale=en_US&access_token=" + AToken;
        ClassAndSpec CSlist = new ClassAndSpec();
        String ConnectDB = "jdbc:mysql://" + dburl + ":" + port + "/" + dbName + "?user=" + username + "&password=" + password;

        Connection conn = DriverManager.getConnection(ConnectDB);

        //Outer loop grabs a class and inner loop processes each specialization of the class
        for (var CClass : CSlist.ClassAndSpecList.keySet()) {


            for (var spec : CSlist.ClassAndSpecList.get(CClass)) {

                CharacterProfile PlayerChar = new CharacterProfile();
                String getSpecUrlBase = "https://us.api.blizzard.com/profile/wow/character/{realmSlug}/{characterName}/"
                        + "specializations?namespace=profile-us&locale=en_US&access_token=" + AToken;
                RaidGuildLB theRGLB = new RaidGuildLB();

                // Reinitialize and reset the static variables between each loop of a specialization
                ArrayList<String> MatchingCharacters = new ArrayList<>();
                ArrayList<String> MatchingSpec = new ArrayList<>();
                if (MediaList.size() != 0) {
                    MediaList.clear();
                    DescriptionList.clear();
                }

                // The class and spec are selected as strings to be easier, API calls want spec as an int
                int convertedSpec = ClassSpecConvert.SpecToInt(CClass, spec);
                PlayerChar.setCharacterSpec(convertedSpec);


                JsonObject UrlLB = getResponse(getRaidGuildLB);
                theRGLB.name = UrlLB.getAsJsonArray("entries").get(0).getAsJsonObject().get("guild").getAsJsonObject().get("name").getAsString();
                theRGLB.realm = UrlLB.getAsJsonArray("entries").get(0).getAsJsonObject().get("guild").getAsJsonObject().get("realm").getAsJsonObject().get("slug").getAsString();

                theRGLB.Fname = theRGLB.name.toLowerCase().replace(" ", "-");
                String getGuildFixed = getGuild.replace("{realmSlug}", theRGLB.realm).replace("{nameSlug}", theRGLB.Fname);


                // Gets the guild with a list of members
                JsonObject GuildResponse = getResponse(getGuildFixed);
                //Gets a max level character of the class that is being looked for
                getGuildCharacter(GuildResponse, CClass, MatchingCharacters, new ArrayList<>());  /*&& foundClass == false*/
                // Takes the character above and "approves" it, checks for the correct and valid specialization that matches
                getMatchingCharacters(MatchingCharacters, MatchingSpec, getSpecUrlBase, theRGLB, PlayerChar, GuildResponse, CClass);

                MatchingSpec.add(MatchingCharacters.get(0));

                // Keeping this, theThree holds 3 possible configurations, keeping it at one at the moment.
                ArrayList<String> theThree = new ArrayList<>();
                for (int i = 0; i < MAXNUMCHARS; i++) {
                    try {
                        theThree.add(MatchingSpec.get(i));
                    } catch (NullPointerException e) {
                        System.out.println("No characters in MatchingSpec");
                    }
                }

                // Prepares the talent names and id's.
                ArrayList<CharacterProfile> theThreeProfiles = new ArrayList<>();
                HashMap<String, Integer> TalentInfo = new HashMap<>();

                String talentsIndexURL = "https://us.api.blizzard.com/data/wow/talent/index?namespace=static-us&locale=en_US&access_token=" + AToken;
                JsonObject getTalentInfo = getResponse(talentsIndexURL);

                for (JsonElement TalentData : getTalentInfo.getAsJsonObject().get("talents").getAsJsonArray()) {
                    TalentInfo.put(
                            TalentData.getAsJsonObject().get("name").getAsString(),
                            TalentData.getAsJsonObject().get("id").getAsInt());
                }


                for (int i = 0; i < MAXNUMCHARS; i++) {
                    // Initializes profile
                    theThreeProfiles.add(new CharacterProfile());
                    theThreeProfiles.get(i).setCharacterName(theThree.get(i).toLowerCase());
                    theThreeProfiles.get(i).setCharacterRealm(theRGLB.realm);
                    theThreeProfiles.get(i).setCharacterClass(CClass);
                   // Fills out the profile with the picture and description for each talent
                    ProfileSetter(theThreeProfiles.get(i), getSpecUrlBase, PlayerChar.getCharacterSpec(), TalentInfo);

                    
                    PreparedStatement stmt = conn.prepareStatement("INSERT INTO CharacterTalents VALUES (?,?,?,?,?,?," +
                            "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                    stmt.setString(1, CClass);
                    stmt.setString(2, spec);
                    stmt.setString(3, theThreeProfiles.get(0).TalentList.get(0));
                    stmt.setString(4, theThreeProfiles.get(0).TalentList.get(1));
                    stmt.setString(5, theThreeProfiles.get(0).TalentList.get(2));
                    stmt.setString(6, theThreeProfiles.get(0).TalentList.get(3));
                    stmt.setString(7, theThreeProfiles.get(0).TalentList.get(4));
                    stmt.setString(8, theThreeProfiles.get(0).TalentList.get(5));
                    stmt.setString(9, theThreeProfiles.get(0).TalentList.get(6));
                    stmt.setString(10,theThreeProfiles.get(0).MediaTalentList.get(0));
                    stmt.setString(11,theThreeProfiles.get(0).MediaTalentList.get(1));
                    stmt.setString(12,theThreeProfiles.get(0).MediaTalentList.get(2));
                    stmt.setString(13,theThreeProfiles.get(0).MediaTalentList.get(3));
                    stmt.setString(14,theThreeProfiles.get(0).MediaTalentList.get(4));
                    stmt.setString(15,theThreeProfiles.get(0).MediaTalentList.get(5));
                    stmt.setString(16,theThreeProfiles.get(0).MediaTalentList.get(6));
                    stmt.setString(17, DescriptionList.get(0));
                    stmt.setString(18, DescriptionList.get(1));
                    stmt.setString(19, DescriptionList.get(2));
                    stmt.setString(20, DescriptionList.get(3));
                    stmt.setString(21, DescriptionList.get(4));
                    stmt.setString(22, DescriptionList.get(5));
                    stmt.setString(23, DescriptionList.get(6));

                    stmt.executeUpdate();


                }

            }

        }
        conn.close();
    }

}

