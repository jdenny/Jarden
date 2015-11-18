package jarden.app.race;

public interface QuizRaceIF {
	int LANE_LENGTH = 8;
    int BASE_MAX = 20;
    
	int SINGLE_USER_MODE = 0;
	int BLUETOOTH_MODE = 1;
	int WEB_MODE = 2; // looking to the future!
	void onModeSelected(int mode);
}
