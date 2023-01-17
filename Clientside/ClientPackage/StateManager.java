package ClientPackage;

//Simple data structure that manages a single state value.
public class StateManager {
	private State currentState = null;

//A default constructor that sets the value of the currentState field to State.NONE.
	public StateManager() {currentState = State.NONE;}

//Returns the value of the currentState field.
	public State getState() {return currentState;}

//Sets the value of the currentState field to the value of the state parameter.
	public void setState(State state) {currentState = state;}
}
