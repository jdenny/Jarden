package mallon.generics;

public class Lab1 {
	public static void main(String[] args) {
		

	}

}

class Stack<T extends Comparable<T>> {
	private class Link {
		T element;
		Link next;
		Link(T element, Link next) {
			this.element = element;
			this.next = next;
		}
	}
	private Link first = null;
	private int sze = 0;
	
	public void push(T t) {
		first = new Link(t, first);
		sze++;
	}
	public T top() {
		return first==null?null:first.element;
	}
	public void pop() {
		if (first != null) {
			first = first.next;
			sze--;
		}
	}
	public int size() {
		return sze;
	}
}
