package keyvaluepair;

public class Key<E> {

	// I looked up java documents there's no good way to get generic type info except for passing in the Type.class
	String myType = "blah blah..."; 
	E myKey;
	
	public Key (E k){
		myKey=k;
	}
	public String getKeyType(){
		return myType;
	}
	
	public E getKey(){
		return myKey;
	}

}