package jarden.engspa;

import jarden.provider.engspa.EngSpaContract.QuestionStyle;

public class EngSpaUser {
	private int userId;
	private String userName;
	private int userLevel;
	//! private int questionStyleIndex;
	private QuestionStyle questionStyle;
	public EngSpaUser(int userId, String userName, int userLevel,
			//!! int questionStyleIndex) {
			QuestionStyle questionStyle) {
		this(userName, userLevel, questionStyle/*!!Index*/);
		this.userId = userId;
	}
	public EngSpaUser(String userName, int userLevel,
			//!! int questionStyleIndex) {
			QuestionStyle questionStyle) {
		this.userName = userName;
		this.userLevel = userLevel;
		//! this.questionStyleIndex = questionStyleIndex;
		this.questionStyle = questionStyle;
	}
	@Override
	public String toString() {
		return "EngSpaUser [userId=" + userId + ", userName=" + userName
				+ ", userLevel=" + userLevel +
				//!! ", questionStyleIndex=" + questionStyleIndex + "]";
				", questionStyle=" + questionStyle + "]";
	}
	public int getUserId() {
		return userId;
	}
	public String getUserName() {
		return userName;
	}
	public int getUserLevel() {
		return userLevel;
	}
	/*!!
	public int getQuestionStyleIndex() {
		return questionStyleIndex;
	}
	public void setQuestionStyleIndex(int questionStyleIndex) {
		this.questionStyleIndex = questionStyleIndex;
	}
	*/
	public QuestionStyle getQuestionStyle() {
		return questionStyle;
	}
	public void setQuestionStyle(QuestionStyle questionStyle) {
		this.questionStyle = questionStyle;
	}
	public void setId(int userId) {
		this.userId = userId;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public void setUserLevel(int userLevel) {
		this.userLevel = userLevel;
	}
}
