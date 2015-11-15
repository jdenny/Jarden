package jarden.provider.engspa;

import android.net.Uri;
import android.provider.BaseColumns;

public final class EngSpaContract {
	public static final String AUTHORITY = "com.jardenconsulting.engspa.provider";
	public static final String TABLE = "EngSpa";
	public static final String CONTENT_URI_STR = "content://" + AUTHORITY + "/" + TABLE;
	public static final Uri CONTENT_URI_ENGSPA = Uri.parse(CONTENT_URI_STR);
	public static final String ENGLISH = "english";
	public static final String SPANISH = "spanish";
	public static final String WORD_TYPE = "wordType";
	public static final String QUALIFIER = "qualifier";
	public static final String ATTRIBUTE = "attribute";
	public static final String LEVEL = "level";
	public static final String[] PROJECTION_ALL_FIELDS = {
		BaseColumns._ID, ENGLISH, SPANISH, WORD_TYPE, QUALIFIER, ATTRIBUTE, LEVEL
	};
	
	// constants for User table:
	public static final String USER_TABLE = "User";
	public static final String CONTENT_URI_USER_STR = "content://" +
			AUTHORITY  + "/" + USER_TABLE;
	public static final Uri CONTENT_URI_USER = Uri.parse(CONTENT_URI_USER_STR);
	public static final String NAME = "name";
	public static final String QUESTION_STYLE = "questionStyle";
	public static final String[] PROJECTION_ALL_USER_FIELDS = {
		BaseColumns._ID, NAME, LEVEL, QUESTION_STYLE
	};

	// constants for UserWord table:
	public static final String USER_WORD_TABLE = "UserWord";
	public static final String CONTENT_URI_USER_WORD_STR = "content://" +
			AUTHORITY  + "/" + USER_WORD_TABLE;
	public static final Uri CONTENT_URI_USER_WORD =
			Uri.parse(CONTENT_URI_USER_WORD_STR);
	public static final String USER_ID = "userId";
	public static final String WORD_ID = "wordId";
	public static final String CONSEC_RIGHT_CT = "consecutiveRightCt";
	public static final String WRONG_CT = "wrongCt";
	public static final String LEVELS_WRONG_CT = "levelsWrongCt";
	
	public static final String[] PROJECTION_ALL_USER_WORD_FIELDS = {
		USER_ID, WORD_ID, CONSEC_RIGHT_CT, WRONG_CT, LEVELS_WRONG_CT
	};
	public static final String[] QUESTION_STYLES = {
        "Spoken Spanish -> Spanish",
		"Spoken Spanish -> English",
		"Spoken + Written Sp. -> English",
		"Written Spanish -> English",
		"Written English -> Spanish",
		"Random: any of above"
	};

	public enum WordType {
		noun, verb, adjective, adverb, number,
		pronoun, preposition, phrase;
	}
	
	public enum Qualifier {
		n_a, // not applicable
		masculine, feminine, // for nouns
		transitive, intransitive, transIntrans, auxiliary; // for verbs
	}
	
	public enum Attribute {
		animal, body, building, clothing, colour, drink, fact, food, home, language,
		mineral, music, n_a, permanent, person, place, size, temporary, time, transport,
		weather;
	}
	public static final String[] wordTypeNames;
	public static final String[] qualifierNames;
	public static final String[] attributeNames;
	
	static {
		WordType[] wordTypes = WordType.values();
		wordTypeNames = new String[wordTypes.length];
		for (int i = 0; i < wordTypes.length; i++) {
			wordTypeNames[i] = wordTypes[i].name();
		}
		Qualifier[] qualifiers = Qualifier.values();
		qualifierNames = new String[qualifiers.length];
		for (int i = 0; i < qualifiers.length; i++) {
			qualifierNames[i] = qualifiers[i].name();
		}
		Attribute[] attributes = Attribute.values();
		attributeNames = new String[attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			attributeNames[i] = attributes[i].name();
		}
	}
}
