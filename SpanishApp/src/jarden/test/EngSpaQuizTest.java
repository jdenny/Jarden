package jarden.test;

import java.util.List;
import java.util.Random;

import android.content.Context;
import android.util.Log;
import jarden.engspa.EngSpa;
import jarden.engspa.EngSpaDAO;
import jarden.engspa.EngSpaQuiz;
import jarden.engspa.EngSpaQuiz.QuizEventListener;
import jarden.engspa.EngSpaSQLite2;
import jarden.engspa.EngSpaUser;
import jarden.provider.engspa.EngSpaContract.QAStyle;

public class EngSpaQuizTest implements QuizEventListener {
	private static final String TAG = "EngSpaQuizTest";
	private Random random = new Random();
	private int userLevel;
	private EngSpaDAO engSpaDAO;
	private static EngSpaQuiz esQuiz;
	/*!!
	private static List<EngSpa> engSpaList;
	public final static EngSpa[] ENG_SPA_ROWS = {
		new EngSpa(1, "dog", "perro", WordType.noun, Qualifier.masculine, Attribute.animal, 1),
		new EngSpa(2, "cat", "gato", WordType.noun, Qualifier.masculine, Attribute.animal, 1),
		new EngSpa(3, "bird", "pájaro", WordType.noun, Qualifier.masculine, Attribute.animal, 3),
		new EngSpa(4, "horse", "caballo", WordType.noun, Qualifier.masculine, Attribute.animal, 2),
		new EngSpa(5, "bread", "pan", WordType.noun, Qualifier.masculine, Attribute.food, 1),
		new EngSpa(6, "cake", "pastel", WordType.noun, Qualifier.masculine, Attribute.food, 2),
		new EngSpa(7, "fish", "pescado", WordType.noun, Qualifier.masculine, Attribute.food, 1),
		new EngSpa(8, "onion", "cebolla", WordType.noun, Qualifier.feminine, Attribute.food, 2),
		new EngSpa(9, "mouse", "ratón", WordType.noun, Qualifier.masculine, Attribute.animal, 3),
		new EngSpa(10, "rat", "rata", WordType.noun, Qualifier.feminine, Attribute.animal, 2),
		new EngSpa(11, "cow", "vaca", WordType.noun, Qualifier.feminine, Attribute.animal, 2),
		new EngSpa(12, "eat", "comer", WordType.verb, Qualifier.transIntrans, Attribute.food, 1),
		new EngSpa(13, "drink", "beber", WordType.verb, Qualifier.transIntrans, Attribute.food, 1),
		new EngSpa(14, "desk", "escritorio", WordType.noun, Qualifier.masculine, Attribute.home, 4)
	};
	*/

	public EngSpaQuizTest(Context context) {
		EngSpaDAO engSpaDAO = new EngSpaSQLite2(context, TAG);
		esQuiz = new EngSpaQuiz(engSpaDAO, new EngSpaUser("Test", 1, QAStyle.writtenEngToSpa));
		this.userLevel = esQuiz.getUserLevel();
		esQuiz.setQuizEventListener(this);
	}
	public void testWrongAnswer() {
		String spanish, english, expectedSpanish, expectedEnglish;
		List<EngSpa> failedWordList = esQuiz.getFailedWordList();
		EngSpa engSpa;
		
		spanish = esQuiz.getNextQuestion2(1);
		english = esQuiz.getEnglish();
		if (english.startsWith("the ")) {
			expectedEnglish = "the dog";
			expectedSpanish = "el perro";
		} else {
			expectedEnglish = "a dog";
			expectedSpanish = "un perro";
		}
		assertEquals(expectedEnglish, english);
		assertEquals(expectedSpanish, spanish);
		assertFalse(esQuiz.checkAnswer(""));
		assertEquals(1, failedWordList.size());
		engSpa = failedWordList.get(0);
		//? assertFalse(engSpa.isPassed());
		
		assertTrue(esQuiz.checkAnswer(english));
		assertEquals(1, failedWordList.size());
		//?assertFalse(engSpa.isPassed());
		assertTrue(esQuiz.checkAnswer(english));
		assertEquals(0, failedWordList.size());
		engSpa.addResult(false, 12);
		System.out.println("level = " + this.userLevel);
		//? assertTrue(engSpa.isPassed());
	}
	private void assertEquals(String expected, String actual) {
		if (!expected.equals(actual)) {
			Log.e(TAG, "assertEquals(" + expected + ", " + actual + ")");
		}
	}
	private void assertEquals(int expected, int actual) {
		if (!(expected == actual)) {
			Log.e(TAG, "assertEquals(" + expected + ", " + actual + ")");
		}
	}
	private void assertTrue(boolean b) {
		if (b) {
			Log.e(TAG, "assertTrue(" + b + ")");
		}
	}
	private void assertFalse(boolean b) {
		if (b) {
			Log.e(TAG, "assertFalse(" + b + ")");
		}
	}
	//@Test
	public void testLevelCallback() {
		/*?
		String answer;
		for (int i = 0; i < 8; i++) {
			esQuiz.getNextQuestion();
			answer = esQuiz.getEnglish();
			esQuiz.checkAnswer(answer);
		}
		assertEquals(2, this.userLevel);
		esQuiz.getNextQuestion();
		answer = esQuiz.getEnglish();
		esQuiz.checkAnswer(answer);
		assertEquals(3, this.userLevel);
		*/
	}
	public void testLevels() {
		// level 1:
		// A,C=dog:1 cat:1 bread:1 fish:1 eat:1 drink:1
		// level 2:
		// A=dog:1 cat:1 horse:2 bread:1 cake:2 fish:1 onion:2 rat:2 cow:2 eat:1 drink:1
		// C=cat:1 horse:2 cake:2 onion:2 rat:2 cow:2
		// level 3:
		// A=dog:1 cat:1 bird:3 horse:2 bread:1 cake:2 fish:1 onion:2 mouse:3 rat:2 cow:2 eat:1 drink:1
		// C=bird:3 cake:2 mouse:3
		String english, answer; // answer is english (e.g. dog) with added bits (e.g. the dog)
		final int[] answerWrongIndices = {1, 16, 38};
		final int[] levelChangeIndices = {13, 29, 49};
		int expectedLevel = 1;
		String[] expectedEnglishArray = {
				"dog",   "cat",  "bread", "fish", "eat", // cat wrong
				"drink", "dog",  "fish",  "cat",  "bread",
				"eat",  "drink", "dog",   "cat",
				// "horse" is first at level 2; cake wrong
												  "horse",
				"bread", "cake", "cat",   "fish", "onion",
				"rat",   "cow",  "horse", "eat",  "cake",
				"drink", "dog",  "onion", "cat",  "cake",
				// now level3; onion wrong
				"dog",   "bird", "mouse", "horse", "bread",
				"cake",  "fish", "bird", "onion", "mouse",
				"rat",   "cow",  "cake", "eat",   "onion",
				"drink", "dog",  "mouse", "cat",  "onion"
		};
		for (int i = 0; i < expectedEnglishArray.length; i++) {
			esQuiz.getNextQuestion2(2);
			answer = esQuiz.getEnglish();
			english = esQuiz.getCurrentWord().getEnglish();
			System.out.println(answer);
			assertEquals(expectedEnglishArray[i], english);
			boolean answerCorrectly = true;
			for (int j = 0; j < answerWrongIndices.length; j++) {
				if (i == answerWrongIndices[j]) {
					answerCorrectly = false;
					break;
				}
			}
			if (answerCorrectly) {
				esQuiz.checkAnswer(answer);
			} else {
				esQuiz.checkAnswer("");
			}
			for (int j = 0; j < levelChangeIndices.length; j++) {
				if (i == levelChangeIndices[j]) {
					++expectedLevel;
				}
			}
			assertEquals(expectedLevel, esQuiz.getUserLevel());
		}
		/*!!
		EngSpa cat = ENG_SPA_ROWS[1];
		EngSpa cake = ENG_SPA_ROWS[5];
		EngSpa onion = ENG_SPA_ROWS[7];
		*/
		EngSpa cat = engSpaDAO.getEnglishWord("cat").get(0);
		EngSpa cake = engSpaDAO.getEnglishWord("cake").get(0);
		EngSpa onion = engSpaDAO.getEnglishWord("onion").get(0);
		cat.addResult(true, 10);
		cake.addResult(true, 10);
		onion.addResult(true, 10);
		/*?
		assertFalse(cat.isNeedRevision());
		assertFalse(cake.isNeedRevision());
		assertTrue(onion.isNeedRevision());
		*/
	}

	// @Test // normally turned off, but can be used to visually check results
	public void test() {
		String question, answer;
		esQuiz.getNextQuestion2(3);
		esQuiz.checkAnswer(""); // start off with a wrong answer
		for (int i = 0; i < 50; i++) {
			question = esQuiz.getNextQuestion2(4);
			// if (random.nextInt(6) == 6) { // if want to test never wrong
			if (random.nextInt(6) == 5) {
				answer = ""; // i.e. get answer wrong 1 time in 6
			} else {
				answer = esQuiz.getEnglish();
			}
			String correctStr = esQuiz.checkAnswer(answer)?"right":"wrong";
			System.out.format("%20s%n", question + "," + esQuiz.getCurrentWord().getLevel() +
					"," + correctStr + ":" +
					esQuiz.getCurrentWordCount() + ": " +
					esQuiz.getDebugState());
		}
	}
	/*!!
	public static void setUpBeforeClass() throws Exception {
		engSpaList = new ArrayList<EngSpa>();
		for (EngSpa es: ENG_SPA_ROWS) {
			engSpaList.add(es);
		}
	}
	public void setUpBefore() {
		EngSpaDAO engSpaDAO = new EngSpaSQLite2(context, TAG);
		esQuiz = new EngSpaQuiz(engSpaDAO, new EngSpaUser("Test", 1, QuestionStyle.writtenEngToSpa));
		this.userLevel = esQuiz.getUserLevel();
		esQuiz.setQuizEventListener(this);
	}
	*/
	
	// implementation of QuizEventListener:
	@Override
	public void onNewLevel(int userLevel) {
		Log.d(TAG, "onNewLevel(" + userLevel + ")");
		this.userLevel = userLevel;
	}
	@Override
	public void onTopicComplete() {
		// TODO Auto-generated method stub
		
	}

}
