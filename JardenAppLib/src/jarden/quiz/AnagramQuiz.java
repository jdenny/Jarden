package jarden.quiz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

/**
 * Read words from a file or database, and create anagram from each one.
 * @author john.denny@gmail.com
 */
public class AnagramQuiz extends Quiz {
	private ArrayList<QuestionAnswer> qaList;
	private ArrayList<Integer> outstandingIndexList;
	private int index = -1;
	private String questionTemplate = null;

	public AnagramQuiz(InputStream is, String encoding) throws IOException {
		this(new InputStreamReader(is, encoding));
	}
	public AnagramQuiz(InputStreamReader isReader) throws IOException {
		BufferedReader reader = new BufferedReader(isReader);
		qaList = new ArrayList<QuestionAnswer>();
		String question = null;
		String answer;
		while (true) {
			String line = reader.readLine();
			if (line == null) break; // end of file
			if (line.startsWith("#")) continue; // comment
			if (line.startsWith("$T: ")) {
				questionTemplate = line.substring(4);
			} else if (line.startsWith("$IO: ")) {
				char questionStyle = line.charAt(5);
				char answerStyle = line.charAt(6);
				setQuestionStyle(questionStyle);
				setAnswerStyle(answerStyle);
			} else {
				answer = line;
				question = randomise(answer);
				qaList.add(new QuestionAnswer(question, answer));
			}
		}
		reader.close();
		common();
	}
	private String randomise(String word) {
		StringBuilder builder = new StringBuilder(word);
		int size = word.length();
		int index;
		for (int i = 0; i < size; i++) {
			index = randomNum.nextInt(size);
			char ch = builder.charAt(i);
			builder.setCharAt(i, builder.charAt(index));
			builder.setCharAt(index, ch);
		}
		return builder.toString();
	}
	private void common() {
		/*! if okay, replace calls to common() with reset()
		if (questionTemplate == null) questionTemplate = "";
		else if (!questionTemplate.endsWith(" ")) {
			questionTemplate += " ";
		}
		*/
		reset();
	}
	public AnagramQuiz(Properties properties) throws IOException {
		Set<String> names = properties.stringPropertyNames();
		qaList = new ArrayList<QuestionAnswer>();
		for (String name: names) {
			String value = properties.getProperty(name);
			if (name.equals(Quiz.TEMPLATE_KEY)) {
				questionTemplate = value;
			} else if (name.equals(Quiz.IO_KEY)) {
				setQuestionStyle(value.charAt(0));
				setAnswerStyle(value.charAt(1));
			} else {
				qaList.add(new QuestionAnswer(name, value));
			}
		}
		common();
	}
	/*!
	public PresetQuiz(String[] qaLines) {
		qaList = new ArrayList<QuestionAnswer>();
		for (String qaLine: qaLines) {
			if (qaLine.startsWith("$T: ")) {
				questionTemplate = qaLine.substring(4);
			} else {
				int index = qaLine.indexOf('=');
				if (index > 0) {
					String question = qaLine.substring(0, index);
					String answer = qaLine.substring(index+1);
					qaList.add(new QuestionAnswer(question, answer));
				}
			}
		}
		common();
	}
	*/
	public AnagramQuiz(ArrayList<QuestionAnswer> qaList) {
		this(qaList, null);
	}
	public AnagramQuiz(ArrayList<QuestionAnswer> qaList, String questionTemplate) {
		this.qaList = qaList;
		/*!
		if (questionTemplate.length() > 0 && !questionTemplate.endsWith(": ") ) {
			questionTemplate += ": ";
		}
		*/
		this.questionTemplate = questionTemplate;
		common();
	}
	public ArrayList<QuestionAnswer> getQuestionAnswerList() {
		return qaList;
	}
	public String getQuestionTemplate() {
		return questionTemplate;
	}
	@Override
	public void reset() {
		super.reset();
		outstandingIndexList = new ArrayList<Integer>();
		for (int i = 0; i < qaList.size(); i++) {
			outstandingIndexList.add(Integer.valueOf(i));
		}
		Collections.shuffle(outstandingIndexList);
	}
	@Override
	public String getNextQuestion(int level) throws EndOfQuestionsException {
		if (outstandingIndexList.size() == 0) {
			throw new EndOfQuestionsException("No more questions");
		}
		index++;
		if (index >= outstandingIndexList.size()) {
			index = 0;
		}
		QuestionAnswer qa = qaList.get(outstandingIndexList.get(index));
		String question;
		if (questionTemplate == null) {
			question = qa.question;
		} else {
			question = MessageFormat.format(questionTemplate, qa.question);
		}
		super.setQuestionAnswer(question, qa.answer);
		return question;
	}
	@Override
	public void notifyRightFirstTime() {
		outstandingIndexList.remove(index);
		index--; // otherwise we would miss out a question this time round
	}
	@Override
	public int getAnswerType() {
		return Quiz.ANSWER_TYPE_STRING;
	}
	@Override
	public String getHint() {
		int len = getAttempts();
		String answer = super.getAnswer();
		if (len > answer.length()) {
			len = answer.length();
		}
		return answer.substring(0, len);
	}
}
