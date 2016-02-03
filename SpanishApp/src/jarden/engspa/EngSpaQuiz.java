package jarden.engspa;

import java.util.Collections;
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
public class EngSpaQuiz extends Quiz {
	static public interface QuizEventListener {
		void onNewLevel(int userLevel);
		void onTopicComplete();
	}
	public static final int WORDS_PER_LEVEL = 10;

	/*
	 * controls which list of words to choose the next question from,
	 * one of current, fails and passed
	 */
	private static final char[] CFP_LIST = {'C', 'F', 'P', 'C', 'F'};
	
	private String spanish;
	private String english;
	private String topic = null;
	
	private Random random = new Random();
	private static final Person[] persons;
	private static final Tense[] tenses;
	private static final int tenseSize;
	private static final int personSize;
	
	/*
	 * if in topic mode (this.topic != null):
	 * 		currentWordList holds words of this topic
	 * else (in level mode):
	 * 		currentWordList holds words where difficulty == userLevel
	 */
	private List<EngSpa> currentWordList;
	/*
	 * when the user has got to the end of all the topic words
	 * we revert to levelWordList (then prompt user to choose
	 * another topic if she wants to)
	 */
	private List<EngSpa> levelWordList;
	/*
	 * words wrong at this userLevel or carried over from
	 * previous levels
	 */
	private List<EngSpa> failedWordList;
	
	private EngSpaUser engSpaUser;
	/*
	 * index to CFP_LIST
	 */
	private int cfpListIndex;
	// cache of last 3 questions asked:
	private static final int RECENTS_CT = 3;
	private EngSpa[] recentWords = new EngSpa[RECENTS_CT];
	private EngSpa currentWord;
	private QuizEventListener quizEventListener;
	private EngSpaDAO engSpaDAO;
	private char cfpChar; // C=current, F=failed, P=passed
	private int questionSequence;

	static {
		tenses = Tense.values();
		persons = Person.values();
		tenseSize = tenses.length;
		personSize = persons.length;
	}
	
	public EngSpaQuiz(EngSpaDAO engSpaDAO, EngSpaUser engSpaUser) {
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
		Collections.shuffle(currentWordList);
		this.failedWordList = this.engSpaDAO.getFailedWordList(engSpaUser.getUserId());
	}
	
	/*
	Get questions from Current, Passed and Failed lists.
	Note: all fails kept in sync with database using userWordTable (i.e.
	fails are per user!)
	Logic:
	if no currents and no fails: endOfQuestions
	in sequence defined by CFP_LIST and cfpListIndex:
	 	if list empty, go to next list in sequence
	get Current; start from 1st
		can't be recent word
	get Failed; start from 1st
		if consecRights < 2
			can't be recent word
		else
			can't be one of previous 10 words
	get random Passed
		can't be recent word
	 */
	public String getNextQuestion2(int questionSequence) {
		this.questionSequence = questionSequence;
		if (this.currentWordList.size() == 0 &&
				this.failedWordList.size() == 0) {
			if (topic == null) incrementUserLevel();
			else {
				endOfTopic();
				if (this.quizEventListener != null) {
					quizEventListener.onTopicComplete();
				}
			}
		}
		// check each of the question types; there should be at least one available
		this.currentWord = null;
		for (int i = 0; i < CFP_LIST.length && currentWord == null; i++) {
			this.cfpChar = CFP_LIST[cfpListIndex];
			incrementCfpListIndex();
			if (cfpChar == 'C' && this.currentWordList.size() > 0) {
				this.currentWord = getCurrentLevelWord();
			} else if (cfpChar == 'P') {
				this.currentWord = getPassedWord();
			} else {
				this.currentWord = getNextFailedWord();
			}
		}
		if (currentWord == null) {
			// running out of words; this can only happen at level 1
			// (so no passed words) and when currentWordList is empty
			// and when words in failed list are also in recentWords
			this.cfpChar = 'F';
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
			int verbLevel = this.engSpaUser.getUserLevel() / 5 + 1;
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
	private void incrementCfpListIndex() {
		if (++this.cfpListIndex >= CFP_LIST.length) {
			this.cfpListIndex = 0;
		}
	}
	/**
	 * @return count of number of words in currentWordList where
	 * word.isPassed() is false. Notes: currentWordList is list
	 * of words where word.difficultyLevel == userLevel;
	 * isPassed() returns true if user has correctly answered the
	 * word the required number of times.
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

	public void setCorrect(boolean correct) {
		boolean inFailedList = this.failedWordList.contains(currentWord);
		int consecRights = currentWord.addResult(correct, questionSequence);
		if (correct) {
			if (inFailedList) {
				if (consecRights > 1) {
					this.failedWordList.remove(currentWord);
				}
				if (consecRights > 2) {
					engSpaDAO.deleteUserWord(currentWord);
				} else {
					engSpaDAO.updateUserWord(currentWord);
				}
			}
			currentWordList.remove(currentWord); // remove if in list
		} else { // not correct
			if (!inFailedList) {
				currentWord.setUserId(this.engSpaUser.getUserId());
				this.failedWordList.add(currentWord);
			}
			// could be on DB.userWord but not in failed list
			// because it's removed from failed list after 2 right
			// but not from DB.userWord until 3 right
			engSpaDAO.replaceUserWord(currentWord);
		}
	}
	
	private EngSpa getCurrentLevelWord() {
		EngSpa es;
		for (int i = 0; i < currentWordList.size(); i++) {
			es = currentWordList.get(i);
			if (!isRecentWord(es)) {
				return es;
			}
		}
		return null;
	}
	
	/*
	 * get random word from previous level (i.e. previously got right);
	 * being random, it may be recently used, so have up to 3 attempts
	 */
	private EngSpa getPassedWord() {
		EngSpa es;
		int level = engSpaUser.getUserLevel();
		if (level < 2) return null;
		for (int i = 0; i < 3; i++) {
			es = engSpaDAO.getRandomPassedWord(level);
			if (!isRecentWord(es)) return es;
		}
		return null;
	}
	/*
	 * Return first word in failed list that was not recently used.
	 */
	private EngSpa getNextFailedWord() {
		for (EngSpa es: failedWordList) {
			if (!es.isRecentlyUsed(questionSequence) && !isRecentWord(es)) return es;
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
		StringBuilder sb = new StringBuilder("EngSpaQuiz.questionSequence=" +
				questionSequence + "; cfpChar=" + cfpChar + "; failedWordList: "); 
		for (EngSpa word: this.failedWordList) {
			sb.append(word + ",");
		}
		List<EngSpa> dbFailedWordList = this.engSpaDAO.getFailedWordList(engSpaUser.getUserId());
		sb.append("\ndbFailedWordList: ");
		for (EngSpa word: dbFailedWordList) {
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
		return "who wants to know?";
	}
	private void endOfTopic() {
		if (this.topic != null) {
			this.currentWordList = this.levelWordList;
			this.topic = null;
		}
	}
	public void setTopic(String topic) {
		if (topic == null) endOfTopic();
		else {
			this.topic = topic;
			this.levelWordList = currentWordList; // to go back to later if necessary
			this.currentWordList = engSpaDAO.findWordsByTopic(topic);
			Collections.shuffle(this.currentWordList);
		}
	}
}
