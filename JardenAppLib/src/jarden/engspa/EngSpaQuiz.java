package jarden.engspa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import jarden.engspa.VerbUtils.Person;
import jarden.engspa.VerbUtils.Tense;
import jarden.provider.engspa.EngSpaContract.Qualifier;
import jarden.provider.engspa.EngSpaContract.WordType;
import jarden.quiz.EndOfQuestionsException;
import jarden.quiz.Quiz;

public class EngSpaQuiz extends Quiz {
	static public interface QuizEventListener {
		void onNewLevel(int userLevel);
	}
	public enum QuestionType {
		WORD, PHRASE, SENTENCE;
	}
	private static int WORDS_PER_LEVEL = 10;
	private String spanish;
	private String english;
	
	private Random random;
	private static final Person[] persons;
	private static final Tense[] tenses;
	private static final int tenseSize;
	private static final int personSize;
	
	// NOTE: a word could be in all 4 lists
	private List<EngSpa> engSpaList; // all words
	private List<EngSpa> availableWordList; // difficulty <= userLevel
	private List<EngSpa> currentLevelWordList; // difficulty == userLevel or
					// difficulty < userLevel and word needs to be revised
	private List<EngSpa> failedWordList; // words wrong at this userLevel
	
	private HashMap<String, EngSpa> eng2SpaMap; // map from English to EngSpa
	private HashMap<String, EngSpa> spa2EngMap; // map from Spanish to EngSpa
	private int userLevel;
	private int availableIndex, currentIndex, failedIndex;
	private int questionSequenceCt;
	private boolean noShuffle; // for testing purposes
	// round robin on last 3 questions asked:
	private EngSpa[] recentWords;
	private EngSpa currentWord;
	private QuizEventListener quizEventListener;
	private int outstandingWordCount = -1;
	
	static {
		tenses = Tense.values();
		persons = Person.values();
		tenseSize = tenses.length;
		personSize = persons.length;
	}
	
	public EngSpaQuiz(List<EngSpa> engSpaList, int userLevel) {
		this(engSpaList, userLevel, false);
	}
	// for testing purposes, allow user to specify no shuffle
	public EngSpaQuiz(List<EngSpa> engSpaList, int userLevel,
			boolean noShuffle) {
		this.engSpaList = engSpaList;
		this.noShuffle = noShuffle;
		this.random = new Random();
		this.recentWords = new EngSpa[3];
		setUserLevel(userLevel);
	}
	public void setQuizEventListener(QuizEventListener listener) {
		this.quizEventListener = listener;
	}
	public int getUserLevel() {
		return this.userLevel;
	}
	public EngSpa getCurrentWord() {
		return this.currentWord;
	}
	public int getMaxUserLevel() {
		return this.engSpaList.size() / WORDS_PER_LEVEL;
	}
	private boolean incrementUserLevel() {
		int newUserLevel = this.userLevel + 1;
		// successful if there are questions at the next difficulty level
		boolean levelIncremented = (newUserLevel <= getMaxUserLevel());
		if (levelIncremented) {
			for (EngSpa word: availableWordList) {
				word.onIncrementingLevel(newUserLevel);
			}
			setUserLevel(newUserLevel);
			if (this.quizEventListener != null) {
				quizEventListener.onNewLevel(newUserLevel);
			}
		}
		return levelIncremented;
	}
	/*
	 * Radical change in design! Instead of each word having an attribute
	 * difficultyLevel, have words in engspa.txt - and thus words on DB -
	 * in difficulty order. A level is deemed to correspond to 10 words.
	 * So to get words of difficulty n, we get 10 words starting from
	 * position (n - 1) * 10
	 * To make it more flexible, we've replaced 10 with WORDS_PER_LEVEL.
	 */
	public void setUserLevel(int level) {
		this.userLevel = level;
		this.availableWordList = new ArrayList<EngSpa>();
		// LinkedLists, because we add and remove elements randomly:
		this.currentLevelWordList = new LinkedList<EngSpa>();
		this.failedWordList = new LinkedList<EngSpa>();
		failedIndex = availableIndex = currentIndex = -1;

		for (int i = 0; i < userLevel * WORDS_PER_LEVEL; i++) {
			EngSpa word = engSpaList.get(i);
			availableWordList.add(word);
			if (i >= (userLevel - 1) * WORDS_PER_LEVEL || word.isNeedRevision()) {
				currentLevelWordList.add(word);
			}
		}
		calculateOutstandingWordCount();
		if (!noShuffle) { // lo siento por la doble negación!
			Collections.shuffle(availableWordList);
			if (currentLevelWordList.size() > 0) {
				Collections.shuffle(currentLevelWordList);
			}
		}
	}
	public String getNextQuestion() {
		return getNextQuestion(QuestionType.WORD);
	}
	
	// get questions from Current, Available and Failed lists,
	// in sequence CACF - used to be AACAF, then CAF
	public String getNextQuestion(QuestionType questionType) {
		final char[] QUESTION_SEQUENCE = {'C', 'A', 'C', 'F'};
		char sequenceChar = QUESTION_SEQUENCE[questionSequenceCt];
		// if 'A' getAvailable
		// else if 'F' and some failed left: getFailed
		// else if some current left: getCurrent
		// else getAvailable
		if (sequenceChar == 'A') {
			this.currentWord = getNextAvailableWord();
		} else if (sequenceChar == 'F' && failedWordList.size() > 0) {
			this.currentWord = getNextFailedWord();
		} else if (currentLevelWordList.size() > 0) {
			this.currentWord = getNextCurrentLevelWord();
		} else this.currentWord = null;
		if (this.currentWord == null) {
			// 'F' but no fails left, or only recently asked fails
			// or 'C' but no current words left
			this.currentWord = getNextAvailableWord();
		}
		if (++this.questionSequenceCt >= QUESTION_SEQUENCE.length) {
			this.questionSequenceCt = 0;
		}
		
		recentWords[0] = recentWords[1];
		recentWords[1] = recentWords[2];
		recentWords[2] = currentWord;

		String spa = currentWord.getSpanish();
		String eng = currentWord.getEnglish();
		Qualifier qualifier = currentWord.getQualifier();
		WordType wordType = currentWord.getWordType();
		if (wordType == WordType.verb) {
			// choose tense based on user level:
			int verbLevel = (userLevel>tenseSize?tenseSize:userLevel);
			Tense tense = tenses[random.nextInt(verbLevel)];
			Person person = persons[random.nextInt(personSize)];
			String spaVerb = VerbUtils.conjugateSpanishVerb(
					spa, tense, person);
			String engVerb = VerbUtils.conjugateEnglishVerb(
					eng, tense, person);
			if (tense == Tense.imperative) {
				this.spanish = spaVerb + "!";
				this.english = engVerb + "!";
			} else if (tense == Tense.noImperative) {
				this.spanish = "no " + spaVerb + "!";
				this.english = "don't " + engVerb + "!";
			} else {
				this.spanish = person.getSpaPronoun() + " " + spaVerb;
				this.english = person.getEngPronoun() + " " + engVerb;
			}
		} else if (wordType == WordType.noun) {
			if (random.nextBoolean()) { // definite article?
				this.english = "the " + eng;
				if (qualifier == Qualifier.feminine) {
					this.spanish = "la " + spa;
				} else {
					this.spanish = "el " + spa;
				}
			} else {
				if ("AEIOUaeiou".indexOf(eng.charAt(0)) >= 0) {
					this.english = "an " + eng;
				} else {
					this.english = "a " + eng;
				}
				if (qualifier == Qualifier.feminine) {
					this.spanish = "una " + spa;
				} else {
					this.spanish = "un " + spa;
				}
			}
		} else {
			this.spanish = spa;
			this.english = eng;
		}
		return this.spanish;
	}
	private void calculateOutstandingWordCount() {
		this.outstandingWordCount = 0;
		for (EngSpa es: this.currentLevelWordList) {
			if (!es.isPassed()) {
				++outstandingWordCount;
			}
		}
	}
	/**
	 * @return count of number of words in currentWordList where
	 * word.isPassed() is false. Notes: currentWordList is list
	 * of words where word.difficultyLevel == userLevel; isPassed()
	 * returns true if user has correctly answered the word the
	 * required number of times.
	 */
	public int getOutstandingWordCount() {
		if (this.outstandingWordCount < 0) {
			calculateOutstandingWordCount();
		}
		return this.outstandingWordCount;
	}
	public int getFailedWordCount() {
		return this.failedWordList.size();
	}
	public boolean checkAnswer(String answer) {
		boolean correct = answer.equals(english); 
		setCorrect(correct);
		return correct;
	}
//	new word (from currentWords); if right first time: done;
//	if wrong once, need 2 consecutive rights
//	if wrong more than once, need 3 consecutive rights

	public void setCorrect(boolean correct) {
		boolean passed = currentWord.addResult(correct);
		calculateOutstandingWordCount();
		if (correct) {
			if (passed) {
				this.failedWordList.remove(currentWord); // remove from failed list if it is in there
				if ((this.failedWordList.size() == 0) && (this.outstandingWordCount == 0)) {
					incrementUserLevel();
				}
			}
		} else { // i.e. wrong answer
			if (!findEngSpaInList(this.currentWord, this.failedWordList)) {
				this.failedWordList.add(this.currentWord);
			}
		}
	}
	private static boolean findEngSpaInList(EngSpa es, List<EngSpa> historyList) {
		for (EngSpa es2: historyList) {
			if (es == es2) {
				return true;
			}
		}
		return false;
	}
	private EngSpa getNextAvailableWord() {
		EngSpa es;
		// in case can't find one not used recently:
		int oldIndex = this.availableIndex;
		int availableWordListSize = availableWordList.size();
		// for loop is to make sure we don't iterate forever
		for (int i = 0; i < availableWordListSize; i++) {
			if (++this.availableIndex >= availableWordListSize) {
				availableIndex = 0;
			}
			es = this.availableWordList.get(availableIndex);
			if (!isRecentWord(es)) return es;
		}
		// failed to find a word that hasn't been used recently
		// so let's fall back on next one whatever:
		this.availableIndex = oldIndex + 1;
		if (availableIndex >= availableWordListSize) {
			availableIndex = 0;
		}
		return this.availableWordList.get(availableIndex);
	}
	private EngSpa getNextCurrentLevelWord() {
		EngSpa es;
		int oldIndex = this.currentIndex;
		int currentLevelWordListSize = currentLevelWordList.size();
		for (int i = 0; i < currentLevelWordListSize; i++) {
			if (++this.currentIndex >= currentLevelWordListSize) {
				currentIndex = 0;
			}
			es = this.currentLevelWordList.get(currentIndex);
			if (!isRecentWord(es)) return es;
		}
		this.currentIndex = oldIndex + 1;
		if (currentIndex >= currentLevelWordListSize) {
			currentIndex = 0;
		}
		return this.availableWordList.get(currentIndex);
	}
	private EngSpa getNextFailedWord() {
		EngSpa es;
		int oldIndex = this.failedIndex;
		int failedWordListSize = failedWordList.size();
		// for loop is to make sure we don't iterate forever
		for (int i = 0; i < failedWordListSize; i++) {
			if (++this.failedIndex >= failedWordListSize) {
				failedIndex = 0;
			}
			es = this.failedWordList.get(failedIndex);
			if (!isRecentWord(es)) return es;
		}
		// behaviour different from the above 2 cases, in that
		// there may no words in this list;
		// if all the failed words used recently, then don't worry
		// about showing one this time round
		this.failedIndex = oldIndex; // leave everything as it was
		return null;
	}
	private boolean isRecentWord(EngSpa word) {
		for (int i = 0; i < recentWords.length; i++) {
			if (word == recentWords[i]) return true;
		}
		return false;
	}
	public String getSpanish() {
		return spanish;
	}
	public String getEnglish() {
		return english;
	}
	public EngSpa eng2Spa(String eng) {
		if (this.eng2SpaMap == null) {
			// i.e. lazy initialisation of map;
			// not used by main part of SpanishApp
			eng2SpaMap = new HashMap<String, EngSpa>();
			for (EngSpa engSpa: engSpaList) {
				eng2SpaMap.put(engSpa.getEnglish(), engSpa);
			}
		}
		return eng2SpaMap.get(eng);
	}
	public EngSpa spa2Eng(String spa) {
		if (this.spa2EngMap == null) {
			// i.e. lazy initialisation of map;
			// not used by main part of SpanishApp
			spa2EngMap = new HashMap<String, EngSpa>();
			for (EngSpa engSpa: engSpaList) {
				spa2EngMap.put(engSpa.getSpanish(), engSpa);
			}
		}
		return spa2EngMap.get(spa);
	}
	// these 3 methods for testing purposes:
	public String getDebugState() {
		StringBuilder sb = new StringBuilder(); 
		for (EngSpa word: this.failedWordList) {
			sb.append(word + ",");
		}
		return sb.toString();
	}
	public List<EngSpa> getFailedWordList() {
		return this.failedWordList;
	}
	public List<EngSpa> getCurrentWordList() {
		return this.currentLevelWordList;
	}
	@Override
	public int getAnswerType() {
		return Quiz.ANSWER_TYPE_STRING;
	}
	@Override
	public String getNextQuestion(int level) throws EndOfQuestionsException {
		return getNextQuestion();
	}
}
