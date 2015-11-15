package jarden.provider;

import java.io.FileNotFoundException;

import jarden.provider.engspa.EngSpaContract;
import jarden.engspa.EngSpaSQLite;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;

/*
 * syntax of Uri:
 * <scheme name> : // <authority>/[<path>]/<? <query>] [ # <fragment> ]
 * e.g. content://jarden.consulting.com/user/5
 * getPath() -> /user/5
 */
public class EngSpaProvider extends ContentProvider {
	private EngSpaSQLite engSpaSQLite;

	private static final String SELECT_BY_KEY = BaseColumns._ID + "=?";
	private String selection;
	private String[] selectionArgs;
	
	public EngSpaProvider() {
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		setSelection(uri, selection, selectionArgs);
		String path = uri.getPath();
		int rows;
		if (path.startsWith("/User")) {
			rows = this.engSpaSQLite.deleteUser(this.selection, this.selectionArgs);
		} else if (path.startsWith("/UserWord")) {
			rows = this.engSpaSQLite.deleteUserWord(this.selection, this.selectionArgs);
		} else {
			rows = this.engSpaSQLite.delete(this.selection, this.selectionArgs);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rows;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public boolean onCreate() {
		this.engSpaSQLite = new EngSpaSQLite(getContext(), "EngSpaProvider");
		return true;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String path = uri.getPath();
		Uri uri2;
		if (path.startsWith("/User")) {
			long newId = this.engSpaSQLite.insertUser(values);
			uri2 = Uri.parse(EngSpaContract.CONTENT_URI_USER_STR + "/" + newId);
		} else if (path.startsWith("/UserWord")) {
			long newId = this.engSpaSQLite.insertUserWord(values);
			uri2 = Uri.parse(EngSpaContract.CONTENT_URI_USER_WORD_STR + "/" + newId);
		} else {
			long newId = this.engSpaSQLite.insert(values);
			uri2 = Uri.parse(EngSpaContract.CONTENT_URI_STR + "/" + newId);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return uri2;
	}

	@Override
	public Cursor query(Uri uri, String[] columns, String selection,
			String[] selectionArgs, String sortOrder) {
		setSelection(uri, selection, selectionArgs);
		String path = uri.getPath();
		Cursor cursor;
		if (path.startsWith("/User")) {
			cursor = this.engSpaSQLite.getUserCursor(columns, this.selection,
					this.selectionArgs, null, null, sortOrder);
		} else if (path.startsWith("/UserWord")) {
			cursor = this.engSpaSQLite.getUserWordCursor(columns, this.selection,
					this.selectionArgs, null, null, sortOrder);
		} else {
			cursor = this.engSpaSQLite.getCursor(columns, this.selection,
					this.selectionArgs, null, null, sortOrder);
		}
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		setSelection(uri, selection, selectionArgs);
		String path = uri.getPath();
		int rows;
		if (path.startsWith("/User")) {
			rows = this.engSpaSQLite.updateUser(values, this.selection, this.selectionArgs);
		} else if (path.startsWith("/UserWord")) {
			rows = this.engSpaSQLite.updateUserWord(values, this.selection, this.selectionArgs);
		} else {
			rows = this.engSpaSQLite.update(values, this.selection, this.selectionArgs);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rows;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		String path = uri.getPath();
		int rows;
		if (path.startsWith("/UserWord")) {
			rows = this.engSpaSQLite.bulkInsertUserWord(values);
		} else {
			rows = this.engSpaSQLite.bulkInsert(values);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rows;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		return super.openFile(uri, mode);
	}

	// Convenience method; it allows clients to append id to uri,
	// instead of setting up selection and selectionArgs.
	private void setSelection(Uri uri, String selection, String[] selectionArgs) {
		String key = getKeyFromUri(uri);
		if (key != null) {
			this.selection = SELECT_BY_KEY; 
			this.selectionArgs = new String[] {key};
		} else {
			this.selection = selection;
			this.selectionArgs = selectionArgs;
		}
	}
	private static String getKeyFromUri(Uri uri) {
		String path = uri.getPath();
		if (path != null) {
			String[] tokens = path.split("/");
			if (tokens.length >= 3) {
				return tokens[2];
			}
		}
		return null;
	}
		
}
