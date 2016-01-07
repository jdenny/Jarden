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
 * and passes it to us. Also note that Quiz already has
 * questionStyle & answerStyle: printed or spoken; see EngSpaFragment.currentQuestionStyle
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
	
	private List<EngSpa> currentWordList; // difficulty == userLevel
	// words wrong at this userLevel or carried over from previous levels
	private List<EngSpa> failedWordList;
	
	private EngSpaUser engSpaUser;
	private int questionSequenceCt;
	// cache of last 3 questions asked:
	private static final int RECENTS_CT = 3;
	private EngSpa[] recentWords = new EngSpa[RECENTS_CT];
	private EngSpa currentWord;
	private QuizEventListener quizEventListener;
	//! private int outstandingWordCount = -1;
	private EngSpaDAO engSpaDAO;
	private char sequenceChar; // P=passed, C=current, F=failed
	
	static {
		tenses = Tense.values();
		persons = Person.values();
		tenseSize = tenses.length;
		personSize = persons.length;
	}
	
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
				this.currentWord = this.currentWordList.remove(0);
			} else if (sequenceChar == 'P') {
				this.currentWord = getPassedWord();
			} else {
				this.currentWord = getNextFailedWord();
			}
		}
		if (currentWord == null) {
			// running out of words; this can only happen at level 1
			// (so no passed words) and when currentWordList is empty
			// and when words in failed list are also in recents
			this.sequenceChar = 'F';
			this.currentWord = failedWordList.get(0);
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
			int verbLevel = this.engSpaUser.getUserLevel() / 3;
			if (verbLevel > tenseSize) verbLevel = tenseSize;
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
		if (!correct) addToFailed();
		if (this.sequenceChar == 'F' && passed) {
			this.failedWordList.remove(currentWord);
		}
	}
	private void addToFailed() {
		UserWord userWord = new UserWord(
				engSpaUser.getUserId(),
				currentWord.getId(),
				currentWord.getWrongCt(),
				currentWord.getConsecutiveRightCt(),
				currentWord.getLevelsWrongCt());
		if (failedWordList.contains(this.currentWord)) {
			engSpaDAO.updateUserWord(userWord);
		} else {
			this.failedWordList.add(currentWord);
			engSpaDAO.insertUserWord(userWord);
		}
	}
	private EngSpa getPassedWord() {
		// TODO: check not recently used
		return engSpaDAO.getRandomPassedWord(engSpaUser.getUserLevel());
	}
	/*
	 * Return first word in failed list that is not also in recent list.
	 */
	private EngSpa getNextFailedWord() {
		for (EngSpa es: failedWordList) {
			if (!isRecentWord(es)) return es;
		}
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
		return this.engSpaDAO.getEnglishWord(eng);
	}
	public List<EngSpa> spa2Eng(String spa) {
		return this.engSpaDAO.getSpanishWord(spa);
	}
	// these 3 methods for testing purposes:
	public String getDebugState() {
		StringBuilder sb = new StringBuilder("EngSpaQuiz2 fails: "); 
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
