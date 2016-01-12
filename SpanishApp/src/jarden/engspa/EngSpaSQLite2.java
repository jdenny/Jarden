package jarden.engspa;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.jardenconsulting.spanishapp.BuildConfig;
import com.jardenconsulting.spanishapp.R;

import static jarden.engspa.EngSpaQuiz.WORDS_PER_LEVEL;
import static jarden.provider.engspa.EngSpaContract.*;
import jarden.provider.engspa.EngSpaContract.Attribute;
import jarden.provider.engspa.EngSpaContract.Qualifier;
import jarden.provider.engspa.EngSpaContract.QuestionStyle;
import jarden.provider.engspa.EngSpaContract.WordType;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

// TODO: merge EngSpaUtils into here
public class EngSpaSQLite2 extends SQLiteOpenHelper implements EngSpaDAO {
	private static final String DB_NAME = "engspa.db";
	private static final int DB_VERSION = 26; // updated 4 Jan 2016
	private static final int DATA_FILE_ID = // R.raw.engspatest;
									R.raw.engspa;

	private static final String CREATE_TABLE =
		"CREATE TABLE " + TABLE + " (" +
		BaseColumns._ID +          " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
		ENGLISH +   " TEXT NOT NULL, " +
		SPANISH +   " TEXT NOT NULL, " +
		WORD_TYPE + " TEXT NOT NULL, " +
		QUALIFIER + " TEXT NOT NULL, " +
		ATTRIBUTE + " TEXT NOT NULL, " +
		LEVEL +     " INTEGER NOT NULL);";
	private static final String CREATE_USER_TABLE =
		"CREATE TABLE " + USER_TABLE + " (" +
		BaseColumns._ID +          " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
		NAME +      " TEXT NOT NULL, " +
		LEVEL +     " INTEGER, " +
		QUESTION_STYLE + " TEXT NOT NULL);";
	private static final String CREATE_USER_WORD_TABLE =
		"CREATE TABLE " + USER_WORD_TABLE + " (" +
		USER_ID +   " INTEGER NOT NULL, " +
		WORD_ID +   " INTEGER NOT NULL, " +
		CONSEC_RIGHT_CT +  " INTEGER NOT NULL, " +
		WRONG_CT +  " INTEGER NOT NULL, " +
		LEVELS_WRONG_CT + " INTEGER NOT NULL, PRIMARY KEY (" +
		USER_ID + "," + WORD_ID + ") );";
	private static final String CREATE_ATTRIBUTE_INDEX =
		"CREATE INDEX attributeIndex ON " + TABLE + " (" + ATTRIBUTE + ");";
	private static final String DROP_TABLE =
			"DROP TABLE IF EXISTS " + TABLE;
	private static final String DROP_USER_TABLE =
			"DROP TABLE IF EXISTS " + USER_TABLE;
	private static final String DROP_USER_WORD_TABLE =
			"DROP TABLE IF EXISTS " + USER_WORD_TABLE;
	private static final String SELECT_FAILS =
			"select EngSpa." + BaseColumns._ID +
			",EngSpa." + ENGLISH +
			",EngSpa." + SPANISH +
			",EngSpa." + WORD_TYPE +
			",EngSpa." + QUALIFIER +
			",EngSpa." + ATTRIBUTE +
			",EngSpa." + LEVEL +
			",UserWord." + CONSEC_RIGHT_CT +
			",UserWord." + WRONG_CT +
			",UserWord." + LEVELS_WRONG_CT +
			" from EngSpa, UserWord where EngSpa." + BaseColumns._ID +
			"= UserWord." + WORD_ID +
			" and UserWord." + USER_ID + "=?";
	private static final String USER_WORD_SELECTION = USER_ID + "=? and " + WORD_ID + "=?";

	private Context context;
	private final String TAG;
	private int dictionarySize = 0;
	private Random random = new Random();

	public EngSpaSQLite2(Context context, String debugTag) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
		this.TAG = debugTag;
	}

	@Override // SQLiteOpenHelper
	public void onCreate(SQLiteDatabase engSpaDB) {
		Log.i(this.TAG, "EngSpaSQLite.onCreate()");
		engSpaDB.execSQL(CREATE_TABLE);
		engSpaDB.execSQL(CREATE_ATTRIBUTE_INDEX);
		engSpaDB.execSQL(CREATE_USER_TABLE);
		engSpaDB.execSQL(CREATE_USER_WORD_TABLE);
		populateDatabase(engSpaDB);
	}
	
	public long getRowCount(String tableName) {
		return DatabaseUtils.queryNumEntries(getReadableDatabase(), tableName);
	}

	// TODO: preserve data from user table across upgrades
	@Override // SQLiteOpenHelper
	public void onUpgrade(SQLiteDatabase engSpaDB, int oldVersion, int newVersion) {
		Log.i(this.TAG,
				"EngSpaSQLite.onUpgrade(oldVersion=" + oldVersion +
				", newVersion=" + newVersion + ")");
		engSpaDB.execSQL(DROP_TABLE); // also removes any indexes
		engSpaDB.execSQL(DROP_USER_TABLE);
		engSpaDB.execSQL(DROP_USER_WORD_TABLE);
		onCreate(engSpaDB);
	}
	
	@Override // EngSpaDAO
	public int newDictionary(ContentValues[] contentValues) {
		SQLiteDatabase engSpaDB = getWritableDatabase();
		int rowCt = delete(engSpaDB, null, null);
		Log.i(TAG, "rows deleted from database: " + rowCt);
		rowCt = bulkInsert(engSpaDB, contentValues);
		Log.i(TAG, "rows inserted to database: " + rowCt);
		return rowCt;
	}

	private int populateDatabase(SQLiteDatabase engSpaDB) {
		Resources resources = context.getResources();
		InputStream is = resources.openRawResource(DATA_FILE_ID);
		ContentValues[] contentValuesArray;
		try {
			contentValuesArray = EngSpaUtils.getContentValuesArray(is);
			this.dictionarySize = this.bulkInsert(engSpaDB, contentValuesArray);
			return this.dictionarySize; 
		} catch (IOException e) {
			Log.e(TAG, "exception in EngSpaSQLite.populateDatabase(): " + e);
			return 0;
		}
	}
	private boolean validateValues (ContentValues values) {
		try {
			WordType.valueOf((String) values.get(WORD_TYPE));
			Qualifier.valueOf((String) values.get(QUALIFIER));
			Attribute.valueOf((String) values.get(ATTRIBUTE));
			return true;
		} catch(Exception ex) {
			Log.e(TAG, "exception in EngSpaSQLite2.validateValues(" + values + "): " + ex);
			return false;
		}
	}
	public long insert(ContentValues values) {
		return insert(getWritableDatabase(), values);
	}
	private long insert(SQLiteDatabase engSpaDB, ContentValues values) {
		if (validateValues(values)) {
			return engSpaDB.insert(TABLE, null, values);
		} else return -1;
	}
	public int bulkInsert(ContentValues[] values) {
		return bulkInsert(getWritableDatabase(), values);
	}
	private int bulkInsert(SQLiteDatabase engSpaDB, ContentValues[] valuesArray) {
		int rows = 0;
		if (valuesArray == null || valuesArray.length == 0) {
			// restoreDB from local file
			rows = delete(engSpaDB, null, null);
			Log.i(TAG, rows + " rows deleted from database");
			rows = populateDatabase(engSpaDB);
		} else {
			for (ContentValues contentValues: valuesArray) {
				if (insert(engSpaDB, contentValues) > 0) ++rows;
			}
		}
		Log.i(TAG, rows + " rows added to database");
		return rows;
	}
	public Cursor getCursor(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		Cursor cursor = getReadableDatabase().query(
				TABLE, columns, selection, selectionArgs,
				groupBy, having, orderBy);
		return cursor;
	}
	public Cursor getCursor(String sql, String[] selectionArgs) {
		return getReadableDatabase().rawQuery(sql, selectionArgs);
	}
	public void close() {
		super.close();
	}
	public int update(ContentValues values, String selection,
			String[] selectionArgs) {
		if (validateValues(values)) {
			return getWritableDatabase().update(TABLE, values,
					selection, selectionArgs);
		} else return 0;
	}
	public int delete(String selection, String[] selectionArgs) {
		return delete(getWritableDatabase(), selection, selectionArgs);
	}
	private int delete(SQLiteDatabase engSpaDB, String selection, String[] selectionArgs) {
		int rows = engSpaDB.delete(TABLE, selection, selectionArgs);
		if (selection == null) {
			// if deleting all the rows, reset the sequence number
			engSpaDB.execSQL("update SQLITE_SEQUENCE set SEQ = 0 where NAME = '" +
					TABLE + "'");
			// or: delete from SQLITE_SEQUENCE where NAME = '" + TABLE + "'"
		}
		return rows;
	}
	
	// methods for User table: ****************************************
	public Cursor getUserCursor(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		Cursor cursor = getReadableDatabase().query(
				USER_TABLE, columns, selection, selectionArgs,
				groupBy, having, orderBy);
		return cursor;
	}
	public long insertUser(ContentValues values) {
		return getWritableDatabase().insert(
			USER_TABLE, null, values);
	}
	public int updateUser(ContentValues values, String selection,
			String[] selectionArgs) {
		return getWritableDatabase().update(USER_TABLE,
			values, selection, selectionArgs);
	}
	public int deleteUser(String selection, String[] selectionArgs) {
		return getWritableDatabase().delete(USER_TABLE,
			selection, selectionArgs);
	}
	@Override // EngSpaDAO
	public long insertUser(EngSpaUser engSpaUser) {
		return insertUser(getContentValues(engSpaUser));
	}
	private static ContentValues getContentValues(EngSpaUser engSpaUser) {
		ContentValues contentValues = new ContentValues();
		//?? contentValues.put(BaseColumns._ID, engSpaUser.getUserId());
		contentValues.put(NAME, engSpaUser.getUserName());
		contentValues.put(LEVEL, engSpaUser.getUserLevel());
		contentValues.put(QUESTION_STYLE, engSpaUser.getQuestionStyle().toString());
		return contentValues;
	}
	@Override // EngSpaDAO
	public int updateUser(EngSpaUser engSpaUser) {
		return updateUser(getContentValues(engSpaUser),
				"_id=?",
				getSelectionArgs(engSpaUser) );
	}
	private static String[] getSelectionArgs(EngSpaUser engSpaUser) {
		return new String[] {Integer.toString(engSpaUser.getUserId())};
	}
	@Override // EngSpaDAO
	public int deleteUser(EngSpaUser engSpaUser) {
		return deleteUser("_id=?",
				getSelectionArgs(engSpaUser) );
	}

	// methods for UserWord table: **********************************************
	public Cursor getUserWordCursor(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		Cursor cursor = getReadableDatabase().query(
				USER_WORD_TABLE, columns, selection, selectionArgs,
				groupBy, having, orderBy);
		return cursor;
	}
	public long insertUserWord(ContentValues values) {
		return getWritableDatabase().insert(
			USER_WORD_TABLE, null, values);
	}
	public int updateUserWord(ContentValues values, String selection,
			String[] selectionArgs) {
		return getWritableDatabase().update(USER_WORD_TABLE,
			values, selection, selectionArgs);
	}
	public int deleteUserWord(String selection, String[] selectionArgs) {
		return getWritableDatabase().delete(USER_WORD_TABLE,
			selection, selectionArgs);
	}
	public int bulkInsertUserWord(ContentValues[] valuesArray) {
		SQLiteDatabase engSpaDB = getWritableDatabase();
		int rows = 0;
		for (ContentValues contentValues: valuesArray) {
			if (insert(engSpaDB, contentValues) > 0) ++rows;
		}
		Log.i(TAG, rows + " rows added to UserWord table");
		return rows;
	}

	@Override // EngSpaDAO
	public List<EngSpa> getCurrentWordList(int userLevel) {
		if (userLevel < 1) userLevel = 1;
		int firstId = (userLevel - 1) * WORDS_PER_LEVEL + 1;
		int dbSize = getDictionarySize();
		if (firstId > (dbSize - WORDS_PER_LEVEL)) {
			firstId = random.nextInt(dbSize - WORDS_PER_LEVEL);
		}
		int lastId = firstId + WORDS_PER_LEVEL;
		String sql = "select * from " + TABLE +
				" where _id >= " + firstId + " and _id < " + lastId;
		if (BuildConfig.DEBUG) Log.d(TAG,
			"EngSpaSQLite2.getCurrentWordList(" + userLevel +
			"); about to get words from " + firstId + " to " + (lastId-1));
		Cursor cursor = null;
		try {
			cursor = getCursor(sql, null); // TODO: it properly!
			List<EngSpa> wordList = new LinkedList<EngSpa>();
			while (cursor.moveToNext()) {
				wordList.add(engSpaFromCursor(cursor));
			}
			if (BuildConfig.DEBUG) Log.d(TAG, "getCurrentWordList got " + wordList.size() + " words");
			return wordList;
		} finally {
			if (cursor != null) cursor.close();
		}
	}
	private EngSpa engSpaFromCursor(Cursor cursor) {
		int id = cursor.getInt(0);
		String english = cursor.getString(1);
		String spanish = cursor.getString(2);
		String wordTypeStr = cursor.getString(3);
		String qualifierStr = cursor.getString(4);
		String attributeStr = cursor.getString(5);
		WordType wordType = WordType.valueOf(wordTypeStr);
		Qualifier qualifier = Qualifier.valueOf(qualifierStr);
		Attribute attribute = Attribute.valueOf(attributeStr);
		int level = cursor.getInt(6);
		return new EngSpa(id, english, spanish,
				wordType, qualifier, attribute, level);
	}

	@Override // EngSpaDAO
	public EngSpa getRandomPassedWord(int userLevel) {
		if (userLevel <= 1) return null;
		int max = (userLevel - 1) * WORDS_PER_LEVEL;
		int dbSize = getDictionarySize();
		if (max > dbSize) max = dbSize;
		int id = random.nextInt(max) + 1; // id starts from 1
		return getWordById(id);
	}
	@Override // EngSpaDAO
	public EngSpa getWordById(int id) {
		if (BuildConfig.DEBUG) Log.d(TAG, "about to get word " + id);
		Cursor cursor = null;
		try {
			cursor = getCursor(PROJECTION_ALL_FIELDS,
					BaseColumns._ID + "=?", // selection
					new String[] { Integer.toString(id)}, // selectionArgs
					null, // groupBy
					null, // having,
					null); // orderBy

			if (cursor.moveToFirst()) {
				return engSpaFromCursor(cursor);
			} else {
				Log.w(TAG, "moveToFirst was false!");
				return null;
			}
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	@Override // EngSpaDAO
	public List<EngSpa> getFailedWordList(int userId) {
		if (BuildConfig.DEBUG) Log.d(TAG,
				"about to get fails for user id " + userId);
		Cursor cursor = null;
		try {
			cursor = getCursor(SELECT_FAILS,
					new String[] {Integer.toString(userId)});
			List<EngSpa> wordList = new LinkedList<EngSpa>();
			while (cursor.moveToNext()) {
				EngSpa engSpa = engSpaFromCursor(cursor);
				engSpa.setConsecutiveRightCt(cursor.getInt(7));
				engSpa.setWrongCt(cursor.getInt(8));
				engSpa.setLevelsWrongCt(cursor.getInt(9));
				wordList.add(engSpa);
			}
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "EngSpaSQLite2.getFailedWordList got " +
						wordList.size() + " words");
			}
			return wordList;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	@Override // EngSpaDAO
	public List<UserWord> getUserWordList(int userId) {
		Cursor cursor = null;
		try {
			cursor = getUserWordCursor(
				PROJECTION_ALL_USER_WORD_FIELDS,
				USER_ID + "=?", //selection
				new String[] {Integer.toString(userId)}, // selectionArgs
				null, // groupBy,
				null, // having,
				null); // orderBy
			List<UserWord> userWordList = new ArrayList<UserWord>();
			while (cursor.moveToNext()) {
				int wordId = cursor.getInt(1);
				int consecRightCt = cursor.getInt(2);
				int wrongCt = cursor.getInt(3);
				int levelsWrongCt = cursor.getInt(4);
				UserWord userWord = new UserWord(
						userId, wordId, wrongCt, consecRightCt, levelsWrongCt);
				userWordList.add(userWord);
			}
			if (BuildConfig.DEBUG) Log.d(TAG, "getUserWordList got " + userWordList.size() + " words");
			return userWordList;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	@Override // EngSpaDAO
	public long insertUserWord(UserWord userWord) {
		return insertUserWord(getContentValues(userWord));
	}
	private static ContentValues getContentValues(UserWord userWord) {
		ContentValues userWordValues = new ContentValues();
		userWordValues.put(USER_ID, userWord.getUserId());
		userWordValues.put(WORD_ID, userWord.getWordId());
		userWordValues.put(CONSEC_RIGHT_CT, userWord.getConsecutiveRightCt());
		userWordValues.put(WRONG_CT, userWord.getWrongCt());
		userWordValues.put(LEVELS_WRONG_CT, userWord.getLevelsWrongCt());
		return userWordValues;
	}

	@Override // EngSpaDAO
	public int updateUserWord(UserWord userWord) {
		return updateUserWord(getContentValues(userWord),
				USER_WORD_SELECTION,
				getSelectionArgs(userWord));
	}
	private static String[] getSelectionArgs(UserWord userWord) {
		return new String[]{
				Integer.toString(userWord.getUserId()),
				Integer.toString(userWord.getWordId())
			};
	}

	@Override // EngSpaDAO
	public int deleteUserWord(UserWord userWord) {
		return deleteUserWord(USER_WORD_SELECTION,
				getSelectionArgs(userWord) );
	}

	@Override // EngSpaDAO
	public List<EngSpa> getSpanishWord(String spanish) {
		Cursor cursor = null;
		try {
			cursor = getCursor(PROJECTION_ALL_FIELDS,
					SPANISH + "=?", // selection
					new String[] { spanish}, // selectionArgs
					null, // groupBy
					null, // having,
					null); // orderBy
			List<EngSpa> matchList = new ArrayList<EngSpa>();
			while (cursor.moveToNext()) {
				matchList.add(engSpaFromCursor(cursor));
			}
			return matchList;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	@Override // EngSpaDAO
	public List<EngSpa> getEnglishWord(String english) {
		Cursor cursor = null;
		try {
			cursor = getCursor(PROJECTION_ALL_FIELDS,
					ENGLISH + "=?", // selection
					new String[] { english }, // selectionArgs
					null, // groupBy
					null, // having,
					null); // orderBy
			List<EngSpa> matchList = new ArrayList<EngSpa>();
			while (cursor.moveToNext()) {
				matchList.add(engSpaFromCursor(cursor));
			}
			return matchList;
		} finally {
			if (cursor != null) cursor.close();
		}
	}

	@Override // EngSpaDAO
	public List<EngSpa> findWords(EngSpa engSpa) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override // EngSpaDAO
	public List<EngSpa> findWordsByTopic(String topic) {
		Cursor cursor = null;
		try {
			cursor = getCursor(PROJECTION_ALL_FIELDS,
					ATTRIBUTE + "=?", // selection
					new String[] { topic }, // selectionArgs
					null, // groupBy
					null, // having,
					null); // orderBy
			List<EngSpa> matchList = new ArrayList<EngSpa>();
			while (cursor.moveToNext()) {
				matchList.add(engSpaFromCursor(cursor));
			}
			return matchList;
		} finally {
			if (cursor != null) cursor.close();
		}
		
	}

	@Override // EngSpaDAO
	public int getDictionarySize() {
		// TODO: check this is always up to date, e.g. after bulk insert
		if (this.dictionarySize == 0) {
			this.dictionarySize = (int) getRowCount(TABLE);
		}
		return this.dictionarySize;
	}
	@Override // EngSpaDAO
	public int getMaxUserLevel() {
		return getDictionarySize() / WORDS_PER_LEVEL;
	}

	// TODO: maybe pass user name as param, rather than 1st user
	@Override // EngSpaDAO
	public EngSpaUser getUser() {
		Cursor cursor = getReadableDatabase().query(
				USER_TABLE,
				PROJECTION_ALL_USER_FIELDS,
				null, // selection
				null, // selectionArgs
				null, // groupBy
				null, // having
				null); // orderBy
		if (cursor.moveToFirst()) {
			int userId = cursor.getInt(0);
			String userName = cursor.getString(1);
			int userLevel = cursor.getInt(2);
			String questionStyleStr = cursor.getString(3);
			QuestionStyle questionStyle = QuestionStyle.valueOf(questionStyleStr);
			EngSpaUser user = new EngSpaUser(userId, userName, userLevel,
					questionStyle);
			Log.i(TAG, "retrieved from database: " + user);
			return user;
		} else return null;
	}

}
