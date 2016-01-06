package jarden.engspa;

import java.util.List;
import java.util.Random;

import jarden.engspa.VerbUtils.Person;
import jarden.engspa.VerbUtils.Tense;
import jarden.provider.engspa.EngSpaContract.Qualifier;
import jarden.provider.engspa.EngSpaContract.WordType;
import jarden.quiz.EndOfQuestionsException;
import jarden.quiz.Quiz;

/*
 * TODO: maybe make this more like Quiz, where the UI holds the level
 * and passes it to us.
 */
public class EngSpaQuiz2 extends Quiz {
	static public interface QuizEventListener {
		void onNewLevel(int userLevel);
	}
	public static final int WORDS_PER_LEVEL = 10;

	private static final char[] QUESTION_SEQUENCE = {'C', 'F', 'P', 'F'};
	
	private String spanish;
	private String english;
	
	private Random random = new Random();
	private static final Person[] persons;
	private static final Tense[] tenses;
	private static final int tenseSize;
	private static final int personSize;
	
	/*!!
	// NOTE: a word could be in all 4 lists
	private List<EngSpa> engSpaList; // all words
	private List<EngSpa> availableWordList; // difficulty <= userLevel
	 */
	private List<EngSpa> currentWordList; // difficulty == userLevel or
					// difficulty < userLevel and word needs to be revised
	private List<EngSpa> failedWordList; // words wrong at this userLevel
	
	/*!!
	private HashMap<String, EngSpa> eng2SpaMap; // map from English to EngSpa
	private HashMap<String, EngSpa> spa2EngMap; // map from Spanish to EngSpa
	 */
	private EngSpaUser engSpaUser;
	private int availableIndex, currentIndex, failedIndex;
	private int questionSequenceCt;
	private boolean noShuffle; // for testing purposes
	// cache of last 3 questions asked:
	private static final int RECENTS_CT = 3;
	private EngSpa[] recentWords = new EngSpa[RECENTS_CT];
	private EngSpa currentWord;
	private QuizEventListener quizEventListener;
	private int outstandingWordCount = -1;
	private EngSpaDAO engSpaDAO;
	private char sequenceChar; // P=passed, C=current, F=failed
	
	static {
		tenses = Tense.values();
		persons = Person.values();
		tenseSize = tenses.length;
		personSize = persons.length;
	}
	
	/*!!
	public EngSpaQuiz2(List<EngSpa> engSpaList, int userLevel) {
		this(engSpaList, userLevel, false);
	}
	// for testing purposes, allow user to specify no shuffle
	public EngSpaQuiz2(List<EngSpa> engSpaList, int userLevel,
			boolean noShuffle) {
		this.engSpaList = engSpaList;
		this.noShuffle = noShuffle;
		this.recentWords = new EngSpa[3];
		setUserLevel(userLevel);
	}
	*/
	public EngSpaQuiz2(EngSpaDAO engSpaDAO, EngSpaUser engSpaUser) {
		this.engSpaUser = engSpaUser;
		this.engSpaDAO = engSpaDAO;
		setUserLevel(engSpaUser.getUserLevel());
	} 
	public void setQuizEventListener(QuizEventListener listener) {
		this.quizEventListener = listener;
	}
	public int getUserLevel() {
		return this.engSpaUser.getUserLevel();
	}
	public EngSpa getCurrentWord() {
		return this.currentWord;
	}
	private boolean incrementUserLevel() {
		int newUserLevel = this.engSpaUser.getUserLevel() + 1;
		// successful if there are questions at the next difficulty level
		boolean levelIncremented =
				(newUserLevel <= engSpaDAO.getMaxUserLevel());
		if (levelIncremented) {
			/*!!
			for (EngSpa word: currentWordList) {
				word.onIncrementingLevel(newUserLevel);
			}
			for (EngSpa word: failedWordList) {
				word.onIncrementingLevel(newUserLevel);
			}
			*/
			/* TODO:
			 getFailList;
			 for each word:
			  	onIncrementingLevel
			  	if needRevision updateonDB
			  	else delete
			 */
			List<UserWord> failedList = engSpaDAO.getUserWordList(engSpaUser.getUserId());
			for (UserWord userWord: failedList) {
				if (userWord.onIncrementingLevel(newUserLevel)) {
					engSpaDAO.updateUserWord(userWord);
				} else {
					engSpaDAO.deleteUserWord(userWord);
				}
			}
			setUserLevel(newUserLevel);
			if (this.quizEventListener != null) {
				quizEventListener.onNewLevel(newUserLevel);
			}
		}
		return levelIncremented;
	}
	/*
	 * Words on DB should be in difficulty order. A level is deemed to correspond
	 * to 10 words. So to get words of difficulty n, we get 10 words starting from
	 * position (n - 1) * 10.
	 * To make it more flexible, we've replaced 10 with WORDS_PER_LEVEL.
	 */
	public void setUserLevel(int level) {
		this.engSpaUser.setUserLevel(level);
		this.engSpaDAO.updateUser(engSpaUser);
		this.currentWordList = this.engSpaDAO.getCurrentWordList(level);
		this.failedWordList = this.engSpaDAO.getFailedWordList(engSpaUser.getUserId());
		/*!!
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
		*/
	}
	
	/*
	Get questions from Current, Passed and Failed lists.
	Note: all fails kept in sync with database using userWordTable (i.e.
	fails are per user!)
	Logic:
	if no currents and no fails: endOfQuestions
	in sequence CFPF; if list empty, go to next list in sequence
	get Current
		if no currents (can't be recent): move on
	 	if wrong: move to Failed; if right: move to Passed
	get Failed; start from 1st
		if no non-recent fails: move on
	 	if passed() move to Passed
	get Passed; check not recent; have 2 attempts, then move on
		if wrong, move to Failed
	 */
	public String getNextQuestion() {
		if (this.currentWordList.size() == 0 &&
				this.failedWordList.size() == 0) {
			incrementUserLevel();
		}
		// check each of the question types; there should be at least one available
		this.currentWord = null;
		for (int i = 0; i < QUESTION_SEQUENCE.length && currentWord == null; i++) {
			this.sequenceChar = QUESTION_SEQUENCE[questionSequenceCt];
			incrementQuestionSequenceCt();
			if (sequenceChar == 'C' && this.currentWordList.size() > 0) {
				this.currentWord = this.currentWordList.get(0);
			} else if (sequenceChar == 'F') {
				this.currentWord = getNextFailedWord();
			} else {
				this.currentWord = getPassedWord();
			}
		}
		if (currentWord == null) {
			// running out of words, so use recently used one
			if (this.failedWordList.size() > 0) {
				currentWord = failedWordList.get(0);
			} else {
				currentWord = currentWordList.get(0);
			}
		}

		/*!!
		else if (currentLevelWordList.size() > 0) {
			this.currentWord = getNextCurrentLevelWord();
		} else this.currentWord = null;
		if (this.currentWord == null) {
			// 'F' but no fails left, or only recently asked fails
			// or 'C' but no current words left
			this.currentWord = getNextAvailableWord();
		}
		*/
		
		recentWords[0] = recentWords[1];
		recentWords[1] = recentWords[2];
		recentWords[2] = currentWord;

		String spa = currentWord.getSpanish();
		String eng = currentWord.getEnglish();
		Qualifier qualifier = currentWord.getQualifier();
		WordType wordType = currentWord.getWordType();
		if (wordType == WordType.verb) {
			// choose tense based on user level:
			int userLevel = this.engSpaUser.getUserLevel();
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
	private void incrementQuestionSequenceCt() {
		if (++this.questionSequenceCt >= QUESTION_SEQUENCE.length) {
			this.questionSequenceCt = 0;
		}
	}
	/*!!
	private void calculateOutstandingWordCount() {
		this.outstandingWordCount = 0;
		for (EngSpa es: this.currentLevelWordList) {
			if (!es.isPassed()) {
				++outstandingWordCount;
			}
		}
	}
	*/
	/**
	 * @return count of number of words in currentWordList where
	 * word.isPassed() is false. Notes: currentWordList is list
	 * of words where word.difficultyLevel == userLevel; isPassed()
	 * returns true if user has correctly answered the word the
	 * required number of times.
	 */
	public int getCurrentWordCount() {
		return this.currentWordList.size();
	}
	public int getFailedWordCount() {
		return this.failedWordList.size();
	}
	public boolean checkAnswer(String answer) {
		boolean correct = answer.equals(english); 
		setCorrect(correct);
		return correct;
	}

	/**
	 * if word is from current list
	 * 		if wrong: add to Failed
	 * else if word from failed list
	 * 		if passed() remove from failed list
	 * else if word from passed list
	 * 		if wrong, add to failed list
	 * @param correct
	 */
	public void setCorrect(boolean correct) {
		boolean passed = currentWord.addResult(correct);
		if (this.sequenceChar == 'C') {
			if (!correct) addToFailed();
			this.currentWordList.remove(0);
		} else if (this.sequenceChar == 'F') {
			if (passed) {
				this.failedWordList.remove(currentWord);
				// note: don't need to update DB until onIncrementingLevel
			}
		} else { // must be 'P'
			if (!correct) addToFailed();
		}
		/*!! calculateOutstandingWordCount();
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
		*/
	}
	private void addToFailed() {
		UserWord userWord = new UserWord(
				engSpaUser.getUserId(),
				currentWord.getId(),
				currentWord.getWrongCt(),
				currentWord.getConsecutiveRightCt(),
				currentWord.getLevelsWrongCt());
		for (EngSpa es2: failedWordList) {
			if (es2 == this.currentWord) {
				engSpaDAO.updateUserWord(userWord);
				return;
			}
		}
		this.failedWordList.add(currentWord);
		engSpaDAO.insertUserWord(userWord);
	}
	/*!!
	private static boolean findEngSpaInList(EngSpa es, List<EngSpa> historyList) {
		for (EngSpa es2: historyList) {
			if (es == es2) {
				return true;
			}
		}
		return false;
	}
	*/
	private EngSpa getPassedWord() {
		return engSpaDAO.getRandomPassedWord(engSpaUser.getUserLevel());
	}
	/*!!
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
	*/
	/*!!
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
	*/
	private EngSpa getNextFailedWord() {
		// TODO: consider having no failedIndex, but rather always
		// start from beginning of failedWordList
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
		for (int i = 0; i < RECENTS_CT; i++) {
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
	public List<EngSpa> eng2Spa(String eng) {
		/*!!
		if (this.eng2SpaMap == null) {
			// i.e. lazy initialisation of map;
			// not used by main part of SpanishApp
			eng2SpaMap = new HashMap<String, EngSpa>();
			for (EngSpa engSpa: engSpaList) {
				eng2SpaMap.put(engSpa.getEnglish(), engSpa);
			}
		}
		return eng2SpaMap.get(eng);
		*/
		return this.engSpaDAO.getEnglishWord(eng);
	}
	public List<EngSpa> spa2Eng(String spa) {
		/*!!
		if (this.spa2EngMap == null) {
			// i.e. lazy initialisation of map;
			// not used by main part of SpanishApp
			spa2EngMap = new HashMap<String, EngSpa>();
			for (EngSpa engSpa: engSpaList) {
				spa2EngMap.put(engSpa.getSpanish(), engSpa);
			}
		}
		return spa2EngMap.get(spa);
		*/
		return this.engSpaDAO.getSpanishWord(spa);
	}
	// these 3 methods for testing purposes:
	public String getDebugState() {
		StringBuilder sb = new StringBuilder("Fails: "); 
		for (EngSpa word: this.failedWordList) {
			sb.append(word + ",");
		}
		return sb.toString();
	}
	public List<EngSpa> getFailedWordList() {
		return this.failedWordList;
	}
	public List<EngSpa> getCurrentWordList() {
		return this.currentWordList;
	}
	@Override // Quiz
	public int getAnswerType() {
		return Quiz.ANSWER_TYPE_STRING;
	}
	@Override // Quiz
	public String getNextQuestion(int level) throws EndOfQuestionsException {
		return getNextQuestion();
	}
}
