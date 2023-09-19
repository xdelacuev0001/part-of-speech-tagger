import java.io.*;
import java.util.*;

/**
 * @author Xenia Dela Cueva, Jeanmarcos Perez
 * PSET5 CS10 21S
 */

public class hmm {

    TreeMap<String, Map<String, Double>> observations;
    TreeMap<String, Map<String, Double>> transitions;

    String start;
    Set<String> ends;

    //creating POS training
    public hmm() {
        observations = new TreeMap<String, Map<String, Double>>();
        transitions = new TreeMap<String, Map<String, Double>>(); //map of keys as current states and its value map keys are next states
    }


    /**
     * Takes in the Sentence file and the tag file and inputs the observations of words for the states
     * in the observations map
     */
    public static TreeMap<String, Map<String, Double>> observations1(String sentenceFile, String tagFile) throws IOException {

        TreeMap<String, Map<String, Double>> observationsMap = new TreeMap<String, Map<String, Double>>();

        BufferedReader inSen = new BufferedReader(new FileReader(sentenceFile));
        BufferedReader inTag = new BufferedReader(new FileReader(tagFile));

        String line;
        String line2;
        int n = 0;

        while ((line = inSen.readLine()) != null && (line2 = inTag.readLine()) != null) {

            String[] eachLineSen = line.toLowerCase().split(" ");
            String[] eachLineTag = line2.split(" ");


            //each word in the sentence and its corresponding tag
            for (int i = 0; i < eachLineSen.length; i++) {
                String matchingTag = eachLineTag[i];
                String word = eachLineSen[i];

                if (!observationsMap.containsKey(matchingTag)) {
                    observationsMap.put(matchingTag, new TreeMap<String, Double>());

                    // then with the word, add that word as part of the tag's TreeMap value as a key w/ value of one
                    observationsMap.get(matchingTag).put(word, 1.0);

                    // add new key to value TreeMap called total with 1 as value
                    observationsMap.get(matchingTag).put("normalize by", 1.0);
                }

                //else (if tag is found in observations map, but does not have a key of that word),
                else if (!observationsMap.get(matchingTag).containsKey(word)) {
                    observationsMap.get(matchingTag).put(word, 1.0);

                    double oldValue = observationsMap.get(matchingTag).get("normalize by");
                    observationsMap.get(matchingTag).replace("normalize by", oldValue + 1);

                }
                //else (if state has key of that word already)
                else {
                    double wordValue = observationsMap.get(matchingTag).get(word);
                    double oldValue = observationsMap.get(matchingTag).get("normalize by");

                    observationsMap.get(matchingTag).replace(word, wordValue + 1);
                    observationsMap.get(matchingTag).replace("normalize by", oldValue + 1);
                }
            }
        }
        inSen.close();
        inTag.close();
        return observationsMap;
    }


    /**
     * Takes in the tag file (assuming that there is a matching sentence file)
     * and inputs the transitions of states in the transitions map
     */
    public static TreeMap<String, Map<String,Double>> transitions1(String tagFile) throws IOException {
        TreeMap<String, Map<String, Double>> transitionsMap = new TreeMap<String, Map<String, Double>>();


        //adding # as a key
        if (!transitionsMap.containsKey("#")) {
            transitionsMap.put("#", new TreeMap<String,Double>());
            transitionsMap.get("#").put("normalize by", (double) 0);
        }

        BufferedReader inTag = new BufferedReader(new FileReader(tagFile));
        String line;
        int n=0;

        while ((line = inTag.readLine()) != null) {
            String[] eachLineTag = line.split(" ");

            String firstTag = eachLineTag[0]; //specifically for the first index,get its tag

            //if the tag is not already one of the keys of #'s treemap value
            if (!transitionsMap.get("#").containsKey(firstTag)) {
                transitionsMap.get("#").put(firstTag, 1.0);

                //increment total by one
                double oldValue = transitionsMap.get("#").get("normalize by");
                transitionsMap.get("#").replace("normalize by", oldValue+1);
            }

            //if the tag is already part of the #'s treemap value
            else {
                double firstTagOccur =transitionsMap.get("#").get(firstTag);

                //increment the tag value and the total by one
                transitionsMap.get("#").replace(firstTag, firstTagOccur+1);
                double oldValue = transitionsMap.get("#").get("normalize by");
                transitionsMap.get("#").replace("normalize by", oldValue+1);
            }

            for (int i =0; i < eachLineTag.length -1; i++) {
                int j = i +1;

                String current = eachLineTag[i];

                String nextState = eachLineTag[j];

                //if current state not found in transition map keys, add that state in with TreeMap value
                if (!transitionsMap.containsKey(current)) {
                    transitionsMap.put(current, new TreeMap<String, Double>());
                    transitionsMap.get(current).put("normalize by", (double) 0);
                }

                //if next isn't a key in current
                if (!transitionsMap.get(current).containsKey(nextState)) {
                    transitionsMap.get(current).put(nextState, 1.0);

                    double oldValue = transitionsMap.get(current).get("normalize by");
                    transitionsMap.get(current).replace("normalize by", oldValue+1 );
                }

                //else, if next is already in current, increment next's value by one and total's value by one
                else {
                    double nextStateValue = transitionsMap.get(current).get(nextState);
                    double oldValue = transitionsMap.get(current).get("normalize by");

                    transitionsMap.get(current).replace(nextState, nextStateValue +1);
                    transitionsMap.get(current).replace("normalize by", oldValue + 1);
                }
                j++;
            }
        }
        inTag.close();
        return transitionsMap;
    }


    /**
     * Turning the numbers in a given map as natural log for proabability
     */
    public TreeMap<String, Map<String, Double>> getMapLogs(TreeMap<String, Map<String, Double>> map) {

        if (map.isEmpty()) {
            System.out.println("No tags taken in yet");
        }
        else {

            //for each key in the observations map
            for (String states: map.keySet()) {
                // for each key in the states' treeMap value
                for (String keysValue : map.get(states).keySet()){

                    double total = map.get(states).get("normalize by");

                    if (keysValue == "normalize by") {continue;}

                    else {
                        double times = map.get(states).get(keysValue);
                        double probablity = times / total;
                        //natural log
                        map.get(states).replace(keysValue, Math.log(probablity));
                    }
                }
            }
        }
        return map;
    }

    public static ArrayList<String> viterbiDecoding(String sentences,TreeMap<String, Map<String, Double>> observations, TreeMap<String, Map<String, Double>> transitions ) {
        // basically like bfs, its takes per word and puts it in backtrace map (dictionary)
        // given that these have weight in it from the proabability calculated by its trnasitions and observations
        // given the highest score for a word sequence, it will backtrace it for its part of speech

        double unforeseen = -100;

        Set<String> currentStates = new HashSet<String>();
        Map<String, Double> currentScores = new TreeMap<String, Double>();

        currentStates.add("#");
        currentScores.put("#", 0.0);

        String[] wordsInSentence = sentences.split(" ");
        ArrayList<Map<String,String>> backtraceList = new ArrayList<Map<String,String>>();

        for (int i = 0; i < wordsInSentence.length; i++) {
            Set nextStates = new HashSet(); //should be restarted
            Map<String, Double> nextScores = new TreeMap<String, Double>();
            Map<String, String> backStates = new TreeMap<String, String>();
            //per word, backstates is added to backtrace list

            for (String eachCurrState : currentStates) {

                //accounts for the punctuation at the end that have no transitions like period
                if (!transitions.containsKey(eachCurrState)) { break; }

                for (String nextState : transitions.get(eachCurrState).keySet()) {
                    //each keySet of current in the transitions map has a "normalize by". We don't want to use that
                    if (nextState == "normalize by") { break; }

                    double nScore;

                    //for the other transitions that are actually part of speech, add them to the next state
                    nextStates.add(nextState);

                    //if the treeMap value of key Next State in observations doens't have word, use unforseen
                    if (!observations.get(nextState).containsKey(wordsInSentence[i])) {
                        nScore = currentScores.get(eachCurrState) +
                                transitions.get(eachCurrState).get(nextState) + unforeseen; }

                    else {
                        nScore = currentScores.get(eachCurrState) +
                                transitions.get(eachCurrState).get(nextState) +
                                observations.get(nextState).get(wordsInSentence[i]); }

                    //making sure to use best values for states in next scores and back states map and that it's unique in map
                    if (!nextScores.containsKey(nextState) && !backStates.containsKey(nextState)) {
                        nextScores.put(nextState, nScore);
                        backStates.put(nextState, eachCurrState);
                    }

                    else if (nScore > nextScores.get(nextState) && backStates.containsKey(nextState)) {
                        nextScores.replace(nextState, nScore);
                        backStates.replace(nextState, eachCurrState);
                    }
                }
            }

            //after the end of observing one word
            backtraceList.add(backStates);
            currentScores = nextScores; //will have the end scores after the nested loops end
            currentStates = nextStates;
            backStates = new TreeMap<String, String>();
        }

        ArrayList<String> bestPathList = new ArrayList<>();
        ArrayList<Double> maxScoreList= new ArrayList<>();

        //getting greatest path
        for(Double score : currentScores.values()){
            maxScoreList.add(score);
        }
        Collections.sort(maxScoreList);
        Collections.reverse(maxScoreList);
        double maxScore = maxScoreList.get(0);

        for(String states : currentScores.keySet()){
            if (currentScores.get(states) == maxScore) {
                bestPathList.add(0,states);
            }
        }

        //this should run the same amount of times the length of the sentence is
        for(int i = backtraceList.size()-1; i >= 0; i--) {
            Map<String,String> mapatIndex = backtraceList.get(i); //for the array of maps with the backstates
            String stateFocus = bestPathList.get(0); //starting from the end, the current end of the sentence

            if (mapatIndex.containsKey(stateFocus)) {

                if (mapatIndex.get(stateFocus) == "#") {break;}

                else {
                    bestPathList.add(0,mapatIndex.get(stateFocus));
                    stateFocus = (String) mapatIndex.get(stateFocus);
                }
            }
        }

        return bestPathList;
    }

    /**
     * Tests the Viterbi on its accuracy on two test files, sentences and its tags, also taking in
     * prior observations and transitions map (trained)
     */
    public static void testingViterbi(String sentenceFile,String tagFile, TreeMap<String, Map<String,Double>> observations, TreeMap<String, Map<String,Double>> transitions) throws IOException {

        BufferedReader inSen = new BufferedReader(new FileReader(sentenceFile));
        BufferedReader inTag = new BufferedReader(new FileReader(tagFile));

        String line;
        String line2;
        int n = 0;
        double correct = 0;
        double incorrect = 0;
        double total = 0;


        while ((line = inSen.readLine()) != null && (line2 = inTag.readLine()) != null) {

            //making the line of the sentence into our own tags
            List<String> viterbiTagList = viterbiDecoding(line, observations, transitions);

            //turning each line of tags in tagFile into array list
            String[] tagSet = line2.split(" ");
            List<String> tagList = new ArrayList<String>();
            tagList = Arrays.asList(tagSet);

            for (int i = 0; i < tagList.size(); i++ ) {

                if (tagList.get(i).equals(viterbiTagList.get(i)) ) {
                    correct += 1;
                }
                else {
                    incorrect += 1;
                }
            }

        }
        System.out.println(correct + " tags correct");
        System.out.println(incorrect + " tags incorrect");
        total = correct + incorrect;
        double accuracy = (correct / total) * 100;
        System.out.println("Viterbi is " + accuracy + "% accurate");

        inSen.close();
        inTag.close();

    }

    /**
     * Console that takes in what the user typed and prints out the parts of speech
     */

    public static void consoleTestViterbi(TreeMap<String, Map<String,Double>> observations, TreeMap<String, Map<String,Double>> transitions) {

        Scanner in = new Scanner(System.in);
        String line;
        String intro = "Type a sentence, phrase, or even word to get its part of speech tags";
        System.out.println(intro);

        while (!(line = in.nextLine()).equals("q")) {
            String input = line;

            //turning each line of tags in tagFile into array list
            String[] inputs = line.split(" ");
            List<String> wordList = new ArrayList<String>();
            wordList = Arrays.asList(inputs);
            ArrayList<String> wordTags = viterbiDecoding(line, observations, transitions);

            System.out.println("The part of speech tags for this text are ");
            for (int i=0; i< wordList.size(); i++) {
                System.out.print(wordList.get(i) + "/" + wordTags.get(i) + " ");
            }
            System.out.print("\n");
            System.out.println("Try another line");
        }

        if (line.equals("q")) {
            System.out.println("Quitting");
        }
    }



    public static void main(String[] args) throws IOException {
        hmm example = new hmm();

        example.observations = observations1("PSET5/brown-test-sentences.txt", "PSET5/brown-test-tags.txt" );
        example.transitions = transitions1("PSET5/brown-test-tags.txt");


        example.getMapLogs(example.transitions);
        example.getMapLogs(example.observations);



        //System.out.println(viterbiDecoding("I love to fish and eat and do anything everyday but this is a sample text and it tests code works",
                //example.observations, example.transitions));
        //System.out.println(viterbiDecoding("I fish", example.observations, example.transitions));

        //testingViterbi("PSET5/brown-test-sentences.txt", "PSET5/brown-test-tags.txt",
                //example.observations, example.transitions);

        consoleTestViterbi(example.observations, example.transitions);


    }
}