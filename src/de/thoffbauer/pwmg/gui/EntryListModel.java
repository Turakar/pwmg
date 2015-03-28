package de.thoffbauer.pwmg.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JList;

import de.thoffbauer.pwmg.manager.PasswordManager;
import de.thoffbauer.pwmg.util.CharHelper;

@SuppressWarnings("serial")
public class EntryListModel extends AbstractListModel<String> {

	ArrayList<char[]> patterns = new ArrayList<>();
	
	private ArrayList<Entry> filteredList;
	private JList<String> list;
	
	public EntryListModel() {
		update();
	}
	
	public void setList(JList<String> list) {
		this.list = list;
	}
	
	@Override
	public String getElementAt(int index) {
		return CharHelper.getFirstLine(filteredList.get(index).entry);
	}

	@Override
	public int getSize() {
		return filteredList.size();
	}
	
	public void setSearchPattern(String pattern) {
		if(pattern.isEmpty()) {
			this.patterns = new ArrayList<char[]>(0);
		} else {
			String patterns[] = pattern.split(" ");
			this.patterns.clear();
			this.patterns.ensureCapacity(patterns.length);
			for(String p : patterns) {
				this.patterns.add(p.toCharArray());
			}
		}
		update();
	}
	
	public void update() {
		List<char[]> entries = PasswordManager.getInstance().getEntries();
		if(entries == null || entries.isEmpty()) {
			filteredList = new ArrayList<Entry>(0);
			return;
		}
		int previousSize = filteredList == null ? 0 : filteredList.size();
		filteredList = new ArrayList<Entry>();
		for(int i = 0; i < entries.size(); i++) {
			char[] entry = entries.get(i);
			boolean match = true;
			for(int j = 0; j < patterns.size(); j++) {
				if(!CharHelper.contains(entry, patterns.get(j))) {
					match = false;
					break;
				}
			}
			if(match) {
				filteredList.add(new Entry(i, entry));
			}
		}
		if(filteredList.size() < previousSize) {
			fireIntervalRemoved(this, filteredList.size(), previousSize - 1);
		} else if(filteredList.size() > previousSize) {
			fireIntervalAdded(this, previousSize, filteredList.size() - 1);
		}
		fireContentsChanged(this, 0, filteredList.size() - 1);
	}
	
	public int listToEntryIndex(int listIndex) {
		if(listIndex == -1) {
			return listIndex;
		}
		return filteredList.get(listIndex).index;
	}
	
	public int getSelectedEntryIndex() {
		if(list == null) {
			throw new IllegalStateException("List is not set yet!");
		}
		return listToEntryIndex(list.getSelectedIndex());
	}

}

class Entry {
	Integer index;
	char[] entry;
	
	public Entry(Integer index, char[] entry) {
		super();
		this.index = index;
		this.entry = entry;
	}
}