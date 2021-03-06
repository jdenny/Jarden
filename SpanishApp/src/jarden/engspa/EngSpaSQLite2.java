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
import jarden.provider.engspa.EngSpaContract.QAStyle;
import jarden.provider.engspa.EngSpaContract.WordType;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

// TODO: merge EngSpaUtils into here
public class EngSpaSQLite2 extends SQLiteOpenHelper implements EngSpaDAO {
	private static EngSpaSQLite2 instance;
	private static final String DB_NAME = "engspa.db";
	// Note: if we update DB_VERSION, also update res/raw/engspaversion.txt
	private static final int DB_VERSION =
			31; // updated 19 Feb 2016; now update engspaversion.txt!

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
		QA_STYLE + " TEXT NOT NULL);";
	private static final String CREATE_USER_WORD_TABLE =
		"CREATE TABLE " + USER_WORD_TABLE + " (" +
		USER_ID +   " INTEGER NOT NULL, " +
		WORD_ID +   " INTEGER NOT NULL, " +
		CONSEC_RIGHT_CT +  " INTEGER NOT NULL, " +
		QUESTION_SEQUENCE + " INTEGER NOT NULL, " +
		QA_STYLE + " TEXT NOT NULL, PRIMARY KEY (" +
		USER_ID + "," + WORD_ID + ") );";
	private static final String CREATE_ATTRIBUTE_INDEX =
		"CREATE INDEX attributeIndex ON " + TABLE + " (" + ATTRIBUTE + ");";
	private static final String CREATE_FAILED_WORD_VIEW =
		"CREATE VIEW " + FAILED_WORD_VIEW + " AS SELECT " +
				"es." + BaseColumns._ID +
				",es." + ENGLISH +
				",es." + SPANISH +
				",es." + WORD_TYPE +
				",es." + QUALIFIER +
				",es." + ATTRIBUTE +
				",es." + LEVEL +
				",uw." + USER_ID +
				",uw." + CONSEC_RIGHT_CT +
				",uw." + QUESTION_SEQUENCE +
				",uw." + QA_STYLE +
				" from " + TABLE + " AS es, " +
				USER_WORD_TABLE + " AS uw where es." + BaseColumns._ID +
				"=uw." + WORD_ID;
	private static final String DROP_TABLE =
			"DROP TABLE IF EXISTS " + TABLE;
	private static final String DROP_USER_TABLE =
			"DROP TABLE IF EXISTS " + USER_TABLE;
	private static final String DROP_USER_WORD_TABLE =
			"DROP TABLE IF EXISTS " + USER_WORD_TABLE;
	private static final String DROP_FAILED_WORD_VIEW =
			"DROP VIEW IF EXISTS " + FAILED_WORD_VIEW;
	private static final String RESET_SEQUENCE_NUMBER =
			"update SQLITE_SEQUENCE set SEQ = 0 where NAME = '" +
			TABLE + "'";
		// or: delete from SQLITE_SEQUENCE where NAME = '" + TABLE + "'"

	private static final String SELECTION = BaseColumns._ID + "=?";
	private static final String USER_WORD_SELECTION = USER_ID + "=? and " + WORD_ID + "=?";
	private static final String USER_SELECTION = USER_ID + "=?";

	private Context context;
	private final String TAG;
	private int dictionarySize = 0;
	private Random random = new Random();

	public static synchronized EngSpaSQLite2 getInstance(Context context, String debugTag) {
		if (instance == null) {
			instance = new EngSpaSQLite2(context, debugTag);
		}
		return instance;
	}
	private EngSpaSQLite2(Context context, String debugTag) {
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
		engSpaDB.execSQL(CREATE_FAILED_WORD_VIEW);
	}
	
	// TODO: preserve data from user table across upgrades
	@Override // SQLiteOpenHelper
	public void onUpgrade(SQLiteDatabase engSpaDB, int oldVersion, int newVersion) {
		Log.i(this.TAG,
				"EngSpaSQLite.onUpgrade(oldVersion=" + oldVersion +
				", newVersion=" + newVersion + ")");
		engSpaDB.execSQL(DROP_FAILED_WORD_VIEW);
		engSpaDB.execSQL(DROP_TABLE); // also removes any indexes
		engSpaDB.execSQL(DROP_USER_TABLE);
		engSpaDB.execSQL(DROP_USER_WORD_TABLE);
		onCreate(engSpaDB);
	}
	
	@Override // EngSpaDAO
	public int newDictionary() {
		SQLiteDatabase engSpaDB = getWritableDatabase();
		int delRowCt = delete(engSpaDB, null, null);
		Log.i(TAG, "EngSpaSQLite2.newDictionary(); rows deleted from database: " + delRowCt);
		return populateDatabase(engSpaDB);
	}
	
	@Override // EngSpaDAO
	public int updateDictionary(List<String> updateLines) {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "EngSpaSQLite2.updateDictionary(); updateLines.size=" +
					updateLines.size());
		int rowCount = 0;
		for (String line: updateLines) {
			if (BuildConfig.DEBUG) Log.d(TAG, "  " + line);
			try {
				if (line.startsWith("u,")) {
					int index = line.indexOf(',', 2); // get position of 2nd comma
					String idStr = line.substring(2, index);
					int id = Integer.parseInt(idStr);
					ContentValues contentValues = EngSpaUtils.getContentValues(line.substring(index + 1));
					contentValues.put(BaseColumns._ID, id);
					update(contentValues, SELECTION, new String[] { idStr });
					rowCount++;
					if (BuildConfig.DEBUG) Log.d(TAG, "id of updated row: " + id);
				} else if (line.startsWith("c,")) {
					ContentValues contentValues = EngSpaUtils.getContentValues(line.substring(2));
					long id = insert(getWritableDatabase(), contentValues);
					rowCount++;
					if (BuildConfig.DEBUG) Log.d(TAG, "id of new row: " + id);
				}
			} catch (Exception e) {
				Log.e(TAG, "EngSpaSQLite2.updateDictionary(); exception: " + e);
			}
		}
		return rowCount;
	}

	private int populateDatabase(SQLiteDatabase engSpaDB) {
		try {
			InputStream is = context.getResources().openRawResource(R.raw.engspa);
			ContentValues[] contentValuesArray = EngSpaUtils.getContentValuesArray(is);
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
	private long insert(SQLiteDatabase engSpaDB, ContentValues values) {
		if (validateValues(values)) {
			return engSpaDB.insert(TABLE, null, values);
		} else return -1;
	}
	private int bulkInsert(SQLiteDatabase engSpaDB, ContentValues[] valuesArray) {
		int rows = 0;
		if (valuesArray == null || valuesArray.length == 0) {
			// restoreDB from local file
			rows = delete(engSpaDB, null, null);
			Log.i(TAG, "EngSpaSQLite2.bulkInsert(); rows deleted from database: " + rows);
			rows = populateDatabase(engSpaDB);
		} else {
			for (ContentValues contentValues: valuesArray) {
				if (insert(engSpaDB, contentValues) > 0) ++rows;
			}
		}
		Log.i(TAG, "EngSpaSQLite2.bulkInsert(); rows added to database: " + rows);
		return rows;
	}
	private Cursor getCursor(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		Cursor cursor = getReadableDatabase().query(
				TABLE, columns, selection, selectionArgs,
				groupBy, having, orderBy);
		return cursor;
	}
	private Cursor getCursor(String sql, String[] selectionArgs) {
		return getReadableDatabase().rawQuery(sql, selectionArgs);
	}
	private int update(ContentValues values, String selection,
			String[] selectionArgs) {
		if (validateValues(values)) {
			return getWritableDatabase().update(TABLE, values,
					selection, selectionArgs);
		} else return 0;
	}
	private int delete(SQLiteDatabase engSpaDB, String selection, String[] selectionArgs) {
		int rows = engSpaDB.delete(TABLE, selection, selectionArgs);
		if (selection == null) {
			// if deleting all the rows, reset the sequence number
			engSpaDB.execSQL(RESET_SEQUENCE_NUMBER);
		}
		return rows;
	}
	
	// methods for User table: ****************************************
	@Override // EngSpaDAO
	public long insertUser(EngSpaUser engSpaUser) {
		return getWritableDatabase().insert(
				USER_TABLE, null, getContentValues(engSpaUser));
	}
	private static ContentValues getContentValues(EngSpaUser engSpaUser) {
		ContentValues contentValues = new ContentValues();
		// TODO: currently only supports one user, so this doesn't matter
		//?? contentValues.put(BaseColumns._ID, engSpaUser.getUserId());
		contentValues.put(NAME, engSpaUser.getUserName());
		contentValues.put(LEVEL, engSpaUser.getUserLevel());
		contentValues.put(QA_STYLE, engSpaUser.getQAStyle().toString());
		return contentValues;
	}
	@Override // EngSpaDAO
	public int updateUser(EngSpaUser engSpaUser) {
		return getWritableDatabase().update(
				USER_TABLE,
				getContentValues(engSpaUser),
				"_id=?",
				getSelectionArgs(engSpaUser) );
	}
	private static String[] getSelectionArgs(EngSpaUser engSpaUser) {
		return new String[] {Integer.toString(engSpaUser.getUserId())};
	}
	@Override // EngSpaDAO
	public int deleteUser(EngSpaUser engSpaUser) {
		return getWritableDatabase().delete(
				USER_TABLE,
				"_id=?", // selection
				getSelectionArgs(engSpaUser) );
	}

	// methods for UserWord table: **********************************************
	@Override // EngSpaDAO
	public List<EngSpa> getFailedWordList(int userId) {
		if (BuildConfig.DEBUG) Log.d(TAG,
				"EngSpaSQLite2.getFailedWordList(userId=" + userId + ")");
		Cursor cursor = null;
		try {
			cursor = getReadableDatabase().query(
					FAILED_WORD_VIEW,
					PROJECTION_ALL_FAILED_WORD_FIELDS,
					USER_SELECTION, // selection
					getUserSelectionArgs(userId), // selectionArgs
					null, // groupBy
					null, // having
					null); // orderBy
			List<EngSpa> wordList = new LinkedList<EngSpa>();
			while (cursor.moveToNext()) {
				EngSpa engSpa = engSpaFromCursor(cursor);
				engSpa.setUserId(userId);
				engSpa.setConsecutiveRightCt(cursor.getInt(7));
				engSpa.setQuestionSequence(cursor.getInt(8));
				String qaStyleStr = cursor.getString(9); 
				engSpa.setQaStyle(QAStyle.valueOf(qaStyleStr));
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
	public long insertUserWord(UserWord userWord) {
		return getWritableDatabase().insert(
				USER_WORD_TABLE, null, getContentValues(userWord));
	}
	private static ContentValues getContentValues(UserWord userWord) {
		ContentValues userWordValues = new ContentValues();
		userWordValues.put(USER_ID, userWord.getUserId());
		userWordValues.put(WORD_ID, userWord.getWordId());
		userWordValues.put(CONSEC_RIGHT_CT, userWord.getConsecutiveRightCt());
		userWordValues.put(QUESTION_SEQUENCE, userWord.getQuestionSequence());
		userWordValues.put(QA_STYLE, userWord.getQaStyle().toString());
		return userWordValues;
	}

	@Override // EngSpaDAO
	public int updateUserWord(UserWord userWord) {
		return getWritableDatabase().update(
				USER_WORD_TABLE,
				getContentValues(userWord),
				USER_WORD_SELECTION,
				getSelectionArgs(userWord));
	}
	@Override // EngSpaDAO
	public long replaceUserWord(UserWord userWord) {
		return getWritableDatabase().replace(
				USER_WORD_TABLE, null, getContentValues(userWord));
	}

	private static String[] getSelectionArgs(UserWord userWord) {
		return new String[] {
			Integer.toString(userWord.getUserId()),
			Integer.toString(userWord.getWordId())
		};
	}
	private String[] getUserSelectionArgs(int userId) {
		return new String[] { Integer.toString(userId) };
	}

	@Override // EngSpaDAO
	public int deleteUserWord(UserWord userWord) {
		return getWritableDatabase().delete(
				USER_WORD_TABLE,
				USER_WORD_SELECTION,
				getSelectionArgs(userWord) );
	}
	
	@Override // EngSpaDAO
	public int deleteAllUserWords(int userId) {
		String selection;
		String[] selectionArguments;
		if (userId < 1) {
			selection = null;
			selectionArguments = null;
		} else {
			selection = USER_SELECTION;
			selectionArguments = getUserSelectionArgs(userId);
		}
		return getWritableDatabase().delete(
				USER_WORD_TABLE,
				selection,
				selectionArguments );
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
			cursor = getCursor(sql, null);
			List<EngSpa> wordList = new LinkedList<EngSpa>();
			while (cursor.moveToNext()) {
				wordList.add(engSpaFromCursor(cursor));
			}
			if (BuildConfig.DEBUG) Log.d(TAG,
					"EngSpaSQLite2.getCurrentWordList(); words obtains: " +
					wordList.size());
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
		if (userLevel < 2) return null;
		int max = (userLevel - 1) * WORDS_PER_LEVEL;
		int dbSize = getDictionarySize();
		if (max > dbSize) max = dbSize;
		int id = random.nextInt(max) + 1; // id starts from 1
		return getWordById(id);
	}
	@Override // EngSpaDAO
	public EngSpa getWordById(int id) {
		if (BuildConfig.DEBUG) Log.d(TAG, "EngSpaSQLite2.getWordById(" + id + ")");
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
				Log.w(TAG, "EngSpaSQLite2.getWordById; moveToFirst() was false!");
				return null;
			}
		} finally {
			if (cursor != null) cursor.close();
		}
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
		if (this.dictionarySize == 0) {
			this.dictionarySize = (int) // getRowCount(TABLE);
				DatabaseUtils.queryNumEntries(getReadableDatabase(), TABLE);
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
		Cursor cursor = null;
		EngSpaUser user = null;
		try {
			cursor = getReadableDatabase().query(
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
				String qaStyleStr = cursor.getString(3);
				QAStyle qaStyle = QAStyle.valueOf(qaStyleStr);
				user = new EngSpaUser(userId, userName, userLevel,
						qaStyle);
				Log.i(TAG, "EngSpaSQLite2.getUser() retrieved from database: " + user);
			}
			return user;
		} finally {
			if (cursor != null) cursor.close();
		}
	}
}
