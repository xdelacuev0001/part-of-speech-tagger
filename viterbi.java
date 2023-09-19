import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class viterbi {
    //private int nScore;
    private int currentScore;
    private int transitionScore;
    private int observationScore;
    private int u;

    TreeMap<String, Map<String,Double>> observations;
    TreeMap<String, Map<String,Double>> transitions;

    public viterbi(){
        //nScore = 0;
        currentScore = 0;
        transitionScore = 0;
        observationScore = 0;
        u = -10;

        observations = new TreeMap<String, Map<String,Double>>();
        transitions = new TreeMap<String, Map<String,Double>>();
    }

//    currStates = { start }
//    currScores = map { start=0 }
//for i from 0 to # observations - 1
//    nextStates = {}
//    nextScores = empty map
//  for each currState in currStates
//    for each transition currState -> nextState
//    add nextState to nextStates
//    nextScore = currScores[currState] +                       // path to here
//    transitionScore(currState -> nextState) +     // take a step to there
//    observationScore(observations[i] in nextState) // make the observation there
//      if nextState isn't in nextScores or nextScore > nextScores[nextState]
//    set nextScores[nextState] to nextScore
//    remember that pred of nextState @ i is curr
//            currStates = nextStates
//    currScores = nextScores

    //this takes in a sentence since the hmmm predicts the states
    public ArrayList<TreeMap> viterbiDecoding(String sentences, String tags){
        //observations = example.observations1(sentences,tags);
        //transitions = example.transitions1(tags);

        double unforseen = -0.5;

        Set<String> currStates = new HashSet();
        currStates.add("start");

        Map<String,Integer> currScores = new TreeMap();
        currScores.put("start", 0);

        String[] wordsInSentence = sentences.split(" ");

        for(int i = 0; i <= wordsInSentence.length -1; i++){
            Set nextStates = new HashSet();
            Map nextScores = new TreeMap();

            for(String currState : currStates){
                for(String nextState : transitions.get(currState).keySet()){
                    double nScore;
                    nextStates.add(nextState);

                    if (!observations.get(nextState).containsKey(wordsInSentence[i])) {
                        nScore = currScores.get(currState) +
                                transitions.get(currState).get(nextState) +
                                unforseen;
                    }
                    else {
                        nScore = currScores.get(currState) +
                                transitions.get(currState).get(nextState) +
                                observations.get(nextState).get(wordsInSentence[i]);
                    }

                }
            }
            currStates = nextStates; //when the sentence ends, this current states refer to the set of end probabilities of viterbi
            //paths
            currScores = nextScores; //this map refers to the current states and their path totals, so when it reaches the end,
            //this refers to the end scores, where we have to choose the greatest value and backtrack a path from there
        }
        return null;
    }

    public static void main(String[] args) {
//        HashSet currStates = new HashSet();
//        currStates.add("start");
//
//        HashMap currScores = new HashMap();
//        currScores.put("start", 0);
//        System.out.println(currStates);
//        System.out.println(currScores);
    }











}


