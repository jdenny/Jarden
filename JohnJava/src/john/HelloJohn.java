package john;

public class HelloJohn {
	public static final String[] QUESTION_STYLES = {
        "Spoken Spanish -> Spanish",
		"Spoken Spanish -> English",
		"Spoken + Written Sp. -> English",
		"Written Spanish -> English",
		"Written English -> Spanish",
		"Random: any of above"
	};
	private final static int QAS_SpokenSpaToSpa = 0;
	// private final static int QAS_SpokenSpaToEng = 1;
	// private final static int QAS_SpokenWrittenSpaToEng = 2;
	// private final static int QAS_WrittenSpaToEng = 3;
	private final static int QAS_WrittenEngToSpa = 4;
	private final static int QAS_Random = 5;

	public enum QuestionStyle {
		spokenSpaToSpa("Spoken Spanish -> Spanish"),
		spokenSpaToEng("Spoken Spanish -> English"),
		spokenWrittenSpaToEng("Spoken + Written Sp. -> English"),
		writtenSpaToEng("Written Spanish -> English"),
		writtenEngToSpa("Written English -> Spanish"),
		random("Random: any of above");
		
		private static String[] fullNameArray;
		static {
			QuestionStyle[] values = QuestionStyle.values();
			fullNameArray = new String[values.length];
			for (int i = 0; i < values.length; i++) {
				fullNameArray[i] = values[i].fullName;
			}
		}
		
		private String fullName;

		private QuestionStyle(String fullName) {
			this.fullName = fullName;
		}
		public String getFullName() {
			return this.fullName;
		}
		public static String[] getFullNameArray() {
			return fullNameArray;
		}
	}


	public static void main(String[] args) {
		System.out.println("hi Julie!");
		testIt();
		System.out.println("adios mi Julita");
	}
	private static void testIt() {
		if (QAS_SpokenSpaToSpa != QuestionStyle.spokenSpaToSpa.ordinal()) {
			System.out.println("QAS_SpokenSpaToSpa=" + QAS_SpokenSpaToSpa +
					"; QuestionStyle.spokenSpaToSpa.ordinal()=" +
					QuestionStyle.spokenSpaToSpa.ordinal());
		}
		if (QAS_Random != QuestionStyle.random.ordinal()) {
			System.out.println("QAS_Random=" + QAS_Random +
					"; QuestionStyle.random.ordinal()=" +
					QuestionStyle.random.ordinal());
		}
		if (!QUESTION_STYLES[1].equals(QuestionStyle.spokenSpaToEng.getFullName())) {
			System.out.println("QUESTION_STYLES[1]=" +
					QUESTION_STYLES[1] + "; QuestionStyle.spokenSpaToEng.getFullName()=" +
					QuestionStyle.spokenSpaToEng.getFullName());
		}
		if (!QUESTION_STYLES[4].equals(QuestionStyle.writtenEngToSpa.getFullName())) {
			System.out.println("QUESTION_STYLES[4]=" + QUESTION_STYLES[4] +
					"; QuestionStyle.writtenEngToSpa.getFullName()=" +
					QuestionStyle.writtenEngToSpa.getFullName());
		}
		System.out.println("fullNameArray:");
		for (String name: QuestionStyle.fullNameArray) System.out.println("  " + name);
	}

}
