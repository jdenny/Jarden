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
	/**
	 * Compare 2 Spanish words for equality.
	 * 
	 * @return -1 if equal, -2 if different, 0 or positive if
	 * 			differ only in accents at position n
	 */
	public static int compareSpaWords(String word1, String word2) {
		int pos;
		int res = -1;
		for (pos = 0; pos < word1.length(); pos++) {
			char char1 = word1.charAt(pos);
			char char2 = word2.charAt(pos);
			if (char1 != char2) {
				if (differByAccent(char1, char2)) {
					if (res < 0) res = pos; // only capture 1st difference
				}
				else return -2;
			}
		}
		if (pos < word2.length()) res = -2;
		return res;
		
	}
	/*
	 * Assuming char1 and char2 are different, and both in lowercase,
	 * would they be the same if their accents were removed?
	 * 
	 * @return true if same with the accents removed
	 */
	private static boolean differByAccent(char char1, char char2) {
		char char1a = removeAccent(char1);
		char char2a = removeAccent(char2);
		return char1a == char2a;
	}
	private static char removeAccent(char ch) {
		if (ch == 'á') return 'a';
		if (ch == 'é') return 'e';
		if (ch == 'í') return 'i';
		if (ch == 'ó') return 'o';
		if (ch == 'ú') return 'u';
		if (ch == 'ñ') return 'n';
		if (ch == 'ü') return 'u';
		return ch;
	}
}
