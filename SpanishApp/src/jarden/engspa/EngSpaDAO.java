package jarden.engspa;

import java.util.List;

import android.content.ContentValues;

public interface EngSpaDAO {
	int getDictionarySize();
	int getMaxUserLevel();
	
	EngSpaUser getUser();
	long insertUser(EngSpaUser engSpaUser);
	int updateUser(EngSpaUser engSpaUser);
	int deleteUser(EngSpaUser engSpaUser); 
	
	/**
	 * delete all existing EngSpa rows and insert new
	 * rows from contentValues.
	 */
	int newDictionary(ContentValues[] contentValues);
	List<EngSpa> getCurrentWordList(int userLevel);
	EngSpa getRandomPassedWord(int userLevel);
	EngSpa getWordById(int id);
	/**
	 * get words starting with supplied Spanish
	 */
	List<EngSpa> getSpanishWord(String spanish);
	/**
	 * get words starting with supplied English
	 */
	List<EngSpa> getEnglishWord(String english);
	/**
	 * find words matching all non-null values of engSpa
	 * @param engSpa as search criteria
	 * @return
	 */
	List<EngSpa> findWords(EngSpa engSpa);
	
	List<EngSpa> getFailedWordList(int userId);
	List<UserWord> getUserWordList(int userId);
	long insertUserWord(UserWord userWord);
	int updateUserWord(UserWord userWord);
	int deleteUserWord(UserWord userWord);

}
