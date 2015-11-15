package jarden.engspa;

public class EngSpaUser {
	private int userId;
	private String userName;
	private int userLevel;
	private int questionStyleIndex;
	public EngSpaUser(int userId, String userName, int userLevel,
			int questionStyleIndex) {
		this(userName, userLevel, questionStyleIndex);
		this.userId = userId;
	}
	public EngSpaUser(String userName, int userLevel,
			int questionStyleIndex) {
		this.userName = userName;
		this.userLevel = userLevel;
		this.questionStyleIndex = questionStyleIndex;
	}
	@Override
	public String toString() {
		return "EngSpaUser [userId=" + userId + ", userName=" + userName
				+ ", userLevel=" + userLevel + ", questionStyleIndex="
				+ questionStyleIndex + "]";
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
	public int getQuestionStyleIndex() {
		return questionStyleIndex;
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
	public void setQuestionStyleIndex(int questionStyleIndex) {
		this.questionStyleIndex = questionStyleIndex;
	}
}
