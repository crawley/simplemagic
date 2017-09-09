package com.j256.simplemagic.entries;

import java.lang.reflect.Field;
import java.util.List;

import com.j256.simplemagic.ContentInfoUtil;

public class MagicEntryDumper {

	public static void main(String[] args) throws Exception {
		ContentInfoUtil util = new ContentInfoUtil();
		Field entriesField = ContentInfoUtil.class.getDeclaredField("magicEntries");
		entriesField.setAccessible(true);
		MagicEntries entries = (MagicEntries) entriesField.get(util);
		Field entryListField = MagicEntries.class.getDeclaredField("entryList");
		entryListField.setAccessible(true); 
		@SuppressWarnings("unchecked")
		List<MagicEntry> entryList = (List<MagicEntry>) entryListField.get(entries);
		for (MagicEntry entry: entryList) {
			System.out.println(entry.toString2());
		}
	}

}
