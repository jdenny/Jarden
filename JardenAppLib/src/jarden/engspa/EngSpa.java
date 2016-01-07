package jarden.engspa;

import jarden.provider.engspa.EngSpaContract.Attribute;
import jarden.provider.engspa.EngSpaContract.Qualifier;
import jarden.provider.engspa.EngSpaContract.WordType;

/**
 * see jarden.android.provider.engspa.EngSpaContract
 * @author john.denny@gmail.com
 *
 */
public class EngSpa {
	private int id;
	private String english;
	private String spanish;
	private WordType wordType;
	private Qualifier qualifier;
	private Attribute attribute;
	private int level;
	private int wrongCt;
	private int consecutiveRightCt;
	private int levelsWrongCt;

	public EngSpa(int id, String english, String spanish,
			WordType wordType, Qualifier qualifier, Attribute attribute, int level) {
		this.id = id;
		this.english = english;
		this.spanish = spanish;
		this.wordType = wordType;
		this.qualifier = qualifier;
		this.attribute = attribute;
		this.level = level;
	}
	/**
	 * Record result of answering this question, right or wrong.
	 * Return true if word considered passed; see {@link #isPassed()}
	 */
	public boolean addResult(boolean correct) {
		if (correct) {
			++consecutiveRightCt;
		} else {
			++wrongCt;
			consecutiveRightCt = 0;
		}
		return isPassed();
	}
	/**
	 * Return true if user has answered correctly enough times,
	 * else false:<br>
	 *  if right first time then passed;<br>
	 *	if wrong once, need 2 consecutive rights;<br>
	 *	if wrong more than once, need 3 consecutive rights.
	 */
	public boolean isPassed() {
		return ((levelsWrongCt + wrongCt) == 0)?(consecutiveRightCt >= 1):
			((levelsWrongCt + wrongCt) == 1)?(consecutiveRightCt >= 2):
			consecutiveRightCt >= 3;
	}
	public boolean isNeedRevision() {
		return levelsWrongCt > 0;
	}
	/**
	 * if answered wrongly, carried to next level;
	 * if answer wrongly again, carried to next 2 levels (max of two);
	 * i.e. need to answer correctly first time to say okay at this level.
	 * @param userLevel
	 * @return
	 */
	public boolean onIncrementingLevel(int userLevel) {
		if (wrongCt > 0) {
			if (levelsWrongCt < 2) ++levelsWrongCt;
		} else {
			if (levelsWrongCt > 0) --levelsWrongCt;
		}
		wrongCt = 0;
		consecutiveRightCt = 0;
		return isNeedRevision();
	}
	public String toString() {
		return english + ":" + spanish + "(w=" + wrongCt +
			",r=" + consecutiveRightCt + ",l=" + levelsWrongCt + ")";
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getEnglish() {
		return english;
	}
	public void setEnglish(String english) {
		this.english = english;
	}
	public String getSpanish() {
		return spanish;
	}
	public void setSpanish(String spanish) {
		this.spanish = spanish;
	}
	public WordType getWordType() {
		return wordType;
	}
	public void setWordType(WordType wordType) {
		this.wordType = wordType;
	}
	public Qualifier getQualifier() {
		return qualifier;
	}
	public void setQualifier(Qualifier qualifier) {
		this.qualifier = qualifier;
	}
	public Attribute getAttribute() {
		return attribute;
	}
	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getWrongCt() {
		return wrongCt;
	}
	public void setWrongCt(int wrongCt) {
		this.wrongCt = wrongCt;
	}
	public int getConsecutiveRightCt() {
		return consecutiveRightCt;
	}
	public void setConsecutiveRightCt(int consecutiveRightCt) {
		this.consecutiveRightCt = consecutiveRightCt;
	}
	public int getLevelsWrongCt() {
		return levelsWrongCt;
	}
	public void setLevelsWrongCt(int levelsWrongCt) {
		this.levelsWrongCt = levelsWrongCt;
	}
}

