package jarden.maze;

public interface MazeListener {
	/**
	 * Increment the game level, and return the new value.
	 * Called on achieving some goal, e.g. reached end of maze.
	 * @return new game level
	 */
	public int onNextLevel();
	public void onLost();
	public void onLookOut();
	public int getLevel();
}
