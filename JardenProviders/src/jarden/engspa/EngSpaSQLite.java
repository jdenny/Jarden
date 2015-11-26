package jarden.engspa;

import java.io.IOException;
import java.io.InputStream;

import com.jardenconsulting.providerdemoapp.R;

import jarden.provider.engspa.EngSpaContract;
import jarden.provider.engspa.EngSpaContract.Attribute;
import jarden.provider.engspa.EngSpaContract.Qualifier;
import jarden.provider.engspa.EngSpaContract.WordType;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class EngSpaSQLite extends SQLiteOpenHelper {
	private final static String DB_NAME = "engspa.db";
	private final static int DB_VERSION = 25; // updated 23 Nov 2015
	private static final int DATA_FILE_ID = // R.raw.engspatest;
									R.raw.engspa;

	private final static String CREATE_TABLE =
		"CREATE TABLE " + EngSpaContract.TABLE + " (" +
		BaseColumns._ID +          " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
		EngSpaContract.ENGLISH +   " TEXT NOT NULL, " +
		EngSpaContract.SPANISH +   " TEXT NOT NULL, " +
		EngSpaContract.WORD_TYPE + " TEXT NOT NULL, " +
		EngSpaContract.QUALIFIER + " TEXT NOT NULL, " +
		EngSpaContract.ATTRIBUTE + " TEXT NOT NULL, " +
		EngSpaContract.LEVEL +     " INTEGER NOT NULL);";
	private final static String CREATE_USER_TABLE =
		"CREATE TABLE " + EngSpaContract.USER_TABLE + " (" +
		BaseColumns._ID +          " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
		EngSpaContract.NAME +      " TEXT NOT NULL, " +
		EngSpaContract.LEVEL +     " INTEGER, " +
		EngSpaContract.QUESTION_STYLE + " TEXT NOT NULL);";
	private final static String CREATE_USER_WORD_TABLE =
		"CREATE TABLE " + EngSpaContract.USER_WORD_TABLE + " (" +
		EngSpaContract.USER_ID +   " INTEGER NOT NULL PRIMARY KEY, " +
		EngSpaContract.WORD_ID +   " INTEGER NOT NULL, " +
		EngSpaContract.CONSEC_RIGHT_CT +  " INTEGER NOT NULL, " +
		EngSpaContract.WRONG_CT +  " INTEGER NOT NULL, " +
		EngSpaContract.LEVELS_WRONG_CT + " INTEGER NOT NULL);";
	private final static String DROP_TABLE =
			"DROP TABLE IF EXISTS " + EngSpaContract.TABLE;
	private final static String DROP_USER_TABLE =
			"DROP TABLE IF EXISTS " + EngSpaContract.USER_TABLE;
	private final static String DROP_USER_WORD_TABLE =
			"DROP TABLE IF EXISTS " + EngSpaContract.USER_WORD_TABLE;

	private Context context;
	private final String TAG;

	public EngSpaSQLite(Context context, String debugTag) {
		super(context, DB_NAME, null, DB_VERSION);
		this.context = context;
		this.TAG = debugTag;
	}

	// methods from SQLiteOpenHelper
	@Override
	public void onCreate(SQLiteDatabase engSpaDB) {
		Log.i(this.TAG, "EngSpaSQLite.onCreate()");
		engSpaDB.execSQL(CREATE_TABLE);
		engSpaDB.execSQL(CREATE_USER_TABLE);
		engSpaDB.execSQL(CREATE_USER_WORD_TABLE);
		populateDatabase(engSpaDB);
	}

	// TODO: preserve data from user table across upgrades
	@Override
	public void onUpgrade(SQLiteDatabase engSpaDB, int oldVersion, int newVersion) {
		Log.i(this.TAG,
				"EngSpaSQLite.onUpgrade(oldVersion=" + oldVersion +
				", newVersion=" + newVersion + ")");
		engSpaDB.execSQL(DROP_TABLE);
		engSpaDB.execSQL(DROP_USER_TABLE);
		engSpaDB.execSQL(DROP_USER_WORD_TABLE);
		onCreate(engSpaDB);
	}
	// end of methods from SQLiteOpenHelper
	
	private int populateDatabase(SQLiteDatabase engSpaDB) {
		Resources resources = context.getResources();
		InputStream is = resources.openRawResource(DATA_FILE_ID);
		ContentValues[] contentValuesArray;
		try {
			contentValuesArray = EngSpaUtils.getContentValuesArray(is);
			return this.bulkInsert(engSpaDB, contentValuesArray);
		} catch (IOException e) {
			Log.e(TAG, "exception in EngSpaSQLite.populateDatabase(): ", e);
			return 0;
		}
	}
	private boolean validateValues (ContentValues values) {
		try {
			WordType.valueOf((String) values.get(EngSpaContract.WORD_TYPE));
			Qualifier.valueOf((String) values.get(EngSpaContract.QUALIFIER));
			Attribute.valueOf((String) values.get(EngSpaContract.ATTRIBUTE));
			return true;
		} catch(Exception ex) {
			Log.e(TAG, "exception in EngSpaSQLite.validateValues(): ", ex);
			return false;
		}
	}
	public long insert(ContentValues values) {
		return insert(getWritableDatabase(), values);
	}
	private long insert(SQLiteDatabase engSpaDB, ContentValues values) {
		if (validateValues(values)) {
			return engSpaDB.insert(EngSpaContract.TABLE, null, values);
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
				EngSpaContract.TABLE, columns, selection, selectionArgs,
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
			return getWritableDatabase().update(EngSpaContract.TABLE, values,
					selection, selectionArgs);
		} else return 0;
	}
	public int delete(String selection, String[] selectionArgs) {
		return delete(getWritableDatabase(), selection, selectionArgs);
	}
	private int delete(SQLiteDatabase engSpaDB, String selection, String[] selectionArgs) {
		int rows = engSpaDB.delete(EngSpaContract.TABLE, selection, selectionArgs);
		if (selection == null) {
			// if deleting all the rows, reset the sequence number
			engSpaDB.execSQL("update SQLITE_SEQUENCE set SEQ = 0 where NAME = '" +
					EngSpaContract.TABLE + "'");
			// or: delete from SQLITE_SEQUENCE where NAME = '" + EngSpaContract.TABLE + "'"
		}
		return rows;
	}
	
	// methods for User table:
	public Cursor getUserCursor(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		Cursor cursor = getReadableDatabase().query(
				EngSpaContract.USER_TABLE, columns, selection, selectionArgs,
				groupBy, having, orderBy);
		return cursor;
	}
	public long insertUser(ContentValues values) {
		return getWritableDatabase().insert(
			EngSpaContract.USER_TABLE, null, values);
	}
	public int updateUser(ContentValues values, String selection,
			String[] selectionArgs) {
		return getWritableDatabase().update(EngSpaContract.USER_TABLE,
			values, selection, selectionArgs);
	}
	public int deleteUser(String selection, String[] selectionArgs) {
		return getWritableDatabase().delete(EngSpaContract.USER_TABLE,
			selection, selectionArgs);
	}
	// methods for UserWord table:
	public Cursor getUserWordCursor(String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy) {
		Cursor cursor = getReadableDatabase().query(
				EngSpaContract.USER_WORD_TABLE, columns, selection, selectionArgs,
				groupBy, having, orderBy);
		return cursor;
	}
	public long insertUserWord(ContentValues values) {
		return getWritableDatabase().insert(
			EngSpaContract.USER_WORD_TABLE, null, values);
	}
	public int updateUserWord(ContentValues values, String selection,
			String[] selectionArgs) {
		return getWritableDatabase().update(EngSpaContract.USER_WORD_TABLE,
			values, selection, selectionArgs);
	}
	public int deleteUserWord(String selection, String[] selectionArgs) {
		return getWritableDatabase().delete(EngSpaContract.USER_WORD_TABLE,
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

}
