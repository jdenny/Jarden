package jarden.engspa;

import jarden.provider.engspa.EngSpaContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.util.Log;

public class EngSpaUtils {
	private final static String TAG = "EngSpaUtils";

	// Note: don't run this method on the UI thread!
	public static ContentValues[] getContentValuesArray(InputStream is) throws IOException {
		List<String> engSpaLines = new ArrayList<String>();
		BufferedReader reader = null;
		try {
			// iso-8859 needed for Android, and maybe for Java;
			// see javadocs in QuizCache.loadQuizFromServer()
			InputStreamReader isReader = new InputStreamReader(is, "iso-8859-1");
			reader = new BufferedReader(isReader);
			String line; // e.g. room,habitación,n,f,place,2
			while ((line = reader.readLine()) != null) {
				engSpaLines.add(line);
			}
			return getContentValuesArray(engSpaLines);
		} finally {
			is.close();
		}
	}
	public static ContentValues[] getContentValuesArray(List<String> engSpaLines) {
		ArrayList<ContentValues> contentValuesList = new ArrayList<ContentValues>(); 
		ContentValues contentValues;
		for (String line: engSpaLines) {
			String[] tokens = line.split(",");
			if (tokens.length == 6) {
				contentValues = new ContentValues();
				contentValues.put(EngSpaContract.ENGLISH, tokens[0]);
				contentValues.put(EngSpaContract.SPANISH, tokens[1]);
				contentValues.put(EngSpaContract.WORD_TYPE, tokens[2]);
				contentValues.put(EngSpaContract.QUALIFIER, tokens[3]);
				contentValues.put(EngSpaContract.ATTRIBUTE, tokens[4]);
				contentValues.put(EngSpaContract.LEVEL, tokens[5]);
				contentValuesList.add(contentValues);
			} else {
				Log.w(TAG, "line from bulk update file in wrong format: " +
						line);
			}
		}
		return contentValuesList.toArray(new ContentValues[contentValuesList.size()]);
	}
}
