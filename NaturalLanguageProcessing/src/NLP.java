import java.io.*;
import java.util.*;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Index;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;


public class NLP {
    private static int notexist;

    /**
     * @param args
     */
    public static void main(String[] args) throws IOException {

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
//        String text = "An exceptionally astute politician deeply involved with power issues in each state, Lincoln reached out to the War Democrats and managed his own re-election campaign in the 1864 presidential election.";
//        String text = "Pick up that block";
//        String text = "In 1921, Einstein received the Nobel Prize for his original work on the photoelectric effect.";
//        String text = "Does Einstein ride a bike?";
//        String text = "Mary saw a ring through the window and has five kang.";
//        String text = "Einstein rode a bike, played violin and received Nobel Prize";
        //Read the target file
        String text = readFiletoString("lincoln.txt");
        if (text == null) {
            System.exit(-1);
        }
//        System.out.println(text);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        BufferedWriter writer1 = new BufferedWriter(new FileWriter("src/lincolnOutputwithSentence.txt"));
        BufferedWriter writer2 = new BufferedWriter(new FileWriter("src/lincolnOutput.txt"));
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);

        // run all Annotators on this text
        pipeline.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        int count = 0;
        HashMap<CoreMap, List<Triple<String, String, String>>> sentenceStructureMap = new HashMap<>();
        for (CoreMap sentence : sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);
//                System.out.println("word " + word + " ,pos: " + pos + " ,ne: " + ne);
            }
            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeAnnotation.class);
//            System.out.println();
//            System.out.println(tree);

//          // this is the Stanford dependency graph of the current sentence
            SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
//            System.out.println();
//            System.out.println(dependencies);
            // get root of parse graph
            IndexedWord root = dependencies.getFirstRoot();
//            System.out.println("ROOT: " + root);
            //Generate
//            for (Pair pair : s) {
//                System.out.println(pair.first + " " + pair.second);
//                System.out.println(dependencies.childPairs((IndexedWord) pair.second));
//            }
//            System.out.println();

            // type of root
            String type = root.tag();
            switch (type.substring(0, 2)) {
                case "JJ":
                    sentenceStructureMap.put(sentence, processNounPhrase(dependencies, root));
                    break;
                case "VB":
                    sentenceStructureMap.put(sentence, processVerbPhrase(dependencies, root));
                    break;
//                case "VBZ":
//                    processVerbPhrase(dependencies, root);
//                    break;
//                case "VBD":
//                    processVerbPhrase(dependencies, root);
//                    break;
//                case "VBP":
//                    processVerbPhrase(dependencies, root);
//                    break;
//                case "VBN":
//                    processVerbPhrase(dependencies, root);
//                    break;
                case "NN":
                    sentenceStructureMap.put(sentence, processNounPhrase(dependencies, root));
                    break;
//                case "NNS":
//                    processNounPhrase(dependencies, root);
//                    break;
//                case "NNP":
//                    processNounPhrase(dependencies, root);
//                    break;
//                case "NNPS":
//                    processNounPhrase(dependencies, root);
//                    break;
//                case "DT":
//                    processDeterminer(dependencies, root);
//                    break;
                default:
                    count++;
//                    System.out.println("Cannot identify sentence structure.");
            }

        }
        System.out.println("Failed to parse:" + count);
        System.out.println("Not found:" + notexist);
        System.out.println("Total:" + sentences.size());
        for (CoreMap sentence : sentences) {
            System.out.println(sentence);
            writer1.write(sentence.toString());
            writer1.write("\n");
            System.out.println(sentenceStructureMap.get(sentence));
            if (sentenceStructureMap.containsKey(sentence)) {
                writer1.write(sentenceStructureMap.get(sentence).toString());
                writer2.write(sentenceStructureMap.get(sentence).toString());
            }
            writer1.write("\n");
            writer2.write("\n");
        }
        writer1.close();
        writer2.close();
        System.out.println();
        System.out.println("Please enter one question");
        Scanner s = new Scanner(System.in);
        String question = s.nextLine();
        s.close();
        Annotation questionAnno = new Annotation(question);
        pipeline.annotate(questionAnno);
        List<CoreMap> questions = questionAnno.get(SentencesAnnotation.class);
        if (questions.size() > 1) {
            System.out.println("I can only answer one question at a time");
            return;
        }
        HashMap<CoreMap, List<Triple<String, String, String>>> questionsStructureMap = new HashMap<>();
        for (CoreMap sentence : questions) {
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                String word = token.get(TextAnnotation.class);
                String pos = token.get(PartOfSpeechAnnotation.class);
                String ne = token.get(NamedEntityTagAnnotation.class);
            }
            SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
            IndexedWord root = dependencies.getFirstRoot();
            String type = root.tag();
            switch (type.substring(0, 2)) {
                case "JJ":
                    questionsStructureMap.put(sentence, processNounPhrase(dependencies, root));
                    break;
                case "VB":
                    questionsStructureMap.put(sentence, processVerbPhrase(dependencies, root));
                    break;
                case "NN":
                    questionsStructureMap.put(sentence, processNounPhrase(dependencies, root));
                    break;
            }
        }
        for (CoreMap q : questions) {
            if (!sentenceStructureMap.containsKey(q)) {
                //The question is not valid;
                System.out.println(false);
                return;
            }
            List<Triple<String, String, String>> list = questionsStructureMap.get(q);
            int[] answered = new int[list.size()];
            int index = 0;
            for (CoreMap sentence : sentences) {
                if (!sentenceStructureMap.containsKey(sentence)) {
                    continue;
                }
                List<Triple<String, String, String>> slist = sentenceStructureMap.get(sentence);
                for (Triple<String, String, String> qt : list) {
                    for (Triple<String, String, String> lt : slist) {
                        if (qt.first.contains(lt.first) || lt.first.contains(qt.first)) {
                            if (qt.second.contains(lt.second) || lt.second.contains(qt.second)) {
                                if (qt.third.contains(lt.third) || lt.third.contains(qt.third)) {
                                    answered[index] = 1;
                                }
                            }
                        }
                    }
                }
                index += 1;
            }
            for (int i : answered) {
                if (i == 0) {
                    System.out.println(false);
                    return;
                }
            }
            System.out.println(true);
        }
    }


    /**
     * Read the content of file into a string.
     * Credit to https://stackoverflow.com/questions/326390/how-do-i-create-a-java-string-from-the-contents-of-a-file
     *
     * @param fileName The name of the file to be read. The target file must be in the same directory.
     * @return The string that contains the content of file.
     */
    static public String readFiletoString(String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/" + fileName));
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sb.toString();
    }

    // Processes: {This, that} one?
    // Didn't happen
    static public void processDeterminer(SemanticGraph dependencies, IndexedWord root) {
        List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);

//        System.out.println("DTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT\n\n");
    }

    //Processes: {That, this, the} {block, sphere}

    /**
     * @param dependencies
     * @param root
     * @return List of triple(s) of subject(s), predicate(s), object(s)
     */
    static public List<Triple<String, String, String>> processNounPhrase(SemanticGraph dependencies, IndexedWord root) {
        List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
        List<Triple<String, String, String>> result = new ArrayList<>();
        String sub = "";
        String pre = "";
        String obj = root.originalText().toLowerCase();
        for (Pair<GrammaticalRelation, IndexedWord> pair : s) {
            if (pair.first.toString().contains("nsubj") && sub.equals("")) {
                List<Pair<GrammaticalRelation, IndexedWord>> subs = dependencies.childPairs(pair.second);
                sub = pair.second.originalText().toLowerCase();
                for (Pair<GrammaticalRelation, IndexedWord> subpair : subs) {
                    if (subpair.first.toString().contains("conj:and")) {
                        sub += " and " + subpair.second.originalText().toLowerCase();
                    }
                }
            } else if (pair.first.toString().contains("cop")) {
                pre = pair.second.originalText().toLowerCase();
            }
        }
        if (sub.equals("") || pre.equals("")) {
            notexist++;
        }
        result.add(new Triple<>(sub, pre, obj));
        return result;
    }

    /**
     * @param dependencies
     * @param root
     * @return List of triple(s) of subject(s), predicate(s), object(s)
     */
    static public List<Triple<String, String, String>> processVerbPhrase(SemanticGraph dependencies, IndexedWord root) {
        List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
        List<Triple<String, String, String>> result = new ArrayList<>();
        String sub = "";
        String pre = root.originalText().toLowerCase();
        String obj = "";
        String sobj = "";
        String ssub = "";
        String spre = "";
        for (Pair<GrammaticalRelation, IndexedWord> pair : s) {
            if (pair.first.toString().contains("nsubj") && sub.equals("")) {
                List<Pair<GrammaticalRelation, IndexedWord>> subs = dependencies.childPairs(pair.second);
                sub = pair.second.originalText().toLowerCase();
                for (Pair<GrammaticalRelation, IndexedWord> subpair : subs) {
                    if (subpair.first.toString().contains("conj:and")) {
                        sub += " and " + subpair.second.originalText().toLowerCase();
                    }
                }
            } else if (pair.first.toString().contains("dobj")) {
                obj = pair.second.originalText().toLowerCase();
            } else if (pair.first.toString().contains("conj")) {
                //Assuming there is only one conjunction in the sentence
                if (!pair.second.tag().contains("VB")) {
                    continue;
                }
                List<Pair<GrammaticalRelation, IndexedWord>> subs = dependencies.childPairs(pair.second);
                spre = pair.second.originalText().toLowerCase();
                for (Pair<GrammaticalRelation, IndexedWord> subpair : subs) {
                    if (subpair.first.toString().contains("nsubj")) {
                        ssub = subpair.second.originalText().toLowerCase();
                    } else if (subpair.first.toString().contains("dobj")) {
                        sobj = subpair.second.originalText().toLowerCase();
                    }
                }
                if (sobj.equals("")) {
                    for (Pair<GrammaticalRelation, IndexedWord> subpair : subs) {
                        if (subpair.first.toString().contains("xcomp") || subpair.first.toString().contains("ccomp")) {
                            sobj = subpair.second.originalText().toLowerCase();
                        }
                    }
                }
                if (!ssub.equals("")) {
                    result.add(new Triple<>(ssub, spre, sobj));
                } else if (!spre.equals("")) {
                    result.add(new Triple<>(sub, spre, sobj));
                }
            }
        }
        boolean found = false;
        if (obj.equals("")) {
            for (Pair<GrammaticalRelation, IndexedWord> pair : s) {
                if (pair.first.toString().contains("xcomp") || pair.first.toString().contains("ccomp")) {
                    result.add(new Triple<>(sub, pre, pair.second.originalText().toLowerCase()));
                    found = true;
                }
            }
        }
        if (!found) {
            result.add(new Triple<>(sub, pre, obj));
        }
        if (sub.equals("") || obj.equals("")) {
            notexist++;
        }
        return result;
    }

}