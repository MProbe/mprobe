package misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.AbstractListModel;

public class SortedArrayListListModel< E extends Comparable< ? super E> >
 extends AbstractListModel {

	
	ArrayList<E> list;
	private ReverseOrderingComparator comp;
	
	public SortedArrayListListModel()
	{
		list = new ArrayList<E>();
		comp = new ReverseOrderingComparator();
	}
	
	class ReverseOrderingComparator implements Comparator<E>
	{
		@Override
		public int compare(E o1, E o2) {
			return -o1.compareTo(o2);
		}
	}
	
	public void add(E element)
	{
		int index = Collections.binarySearch(list, element, comp);
		if (index < 0)
		{
			index = -index -1;
			list.add(index, element);
			fireIntervalAdded(this, index, index);
		}
	}

	public void addAll(Collection<E> c)
	{
		for (E e: c)
			add(e);
	}
	
	public void clear()
	{
		list.clear();
		fireContentsChanged(this, 0, 0);
	}

	public boolean contains(E e)
	{
		return list.contains(e);
	}

	public boolean containsAll(Collection<E> c)
	{
		return list.containsAll(c);
	}

	public boolean isEmpty()
	{
		return list.isEmpty();
	}

	public Iterator<E> iterator()
	{
		return list.iterator();
	}

	public boolean remove(int index)
	{
		if (index >= 0 && index < list.size())
		{
			list.remove(index);
			fireIntervalRemoved(this, index, index);
			return true;
		}
		return false;
	}

	public int size()
	{
		return list.size();
	}

	@SuppressWarnings("unchecked")
	public E[] toArray()
	{
		return (E[]) list.toArray();
	}

	public void replace(E[] newElements)
	{
		E[] tmp = newElements.clone();
		int originalSize = list.size();
		Arrays.sort(tmp, comp);
		list.clear();
		fireIntervalRemoved(this, 0, Math.max(originalSize-1, 0));
		for (E e: newElements)
			list.add(e);
		fireIntervalAdded(this, 0, Math.max(list.size()-1, 0));
	}

	@Override
	public E getElementAt(int index) {
		return list.get(index);
	}

	@Override
	public int getSize() {
		return list.size();
	}

	public void removeIndices(int[] indices) {
		int[] tmpIndices = indices.clone();
		Arrays.sort(tmpIndices);
		for (int i = tmpIndices.length-1; i >= 0; --i)
		{
			remove(tmpIndices[i]);
		}
	}
}
