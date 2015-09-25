import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({ "serial", "unused" })
public class IndexBuilder implements Serializable {
	private Set<String> stopWords;
	private Map<String, DictionaryEntry> dictionary;
	private static Integer documentIdIndex = 0;

	public IndexBuilder(Set<String> stopWords) {
		this.stopWords = stopWords;
		this.dictionary = new HashMap<String, DictionaryEntry>();
	}

	public Map<String, DictionaryEntry> buildIndex(String directoryPath)
			throws Exception {
		File directory = new File(directoryPath);
		Tokenizer tokenizer = new Tokenizer();

		System.out
				.println("FILE NAME \t DOC_ID \t  maxTermFreq  \t docLength  ");
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				System.out.print(file.getName());
				Map<String, Integer> termFreqTable = tokenizer
						.buildTermFreqTable(file);
				insertIntoIndex(++documentIdIndex, termFreqTable);
			}
		}
		return dictionary;
	}

	private void insertIntoIndex(Integer docId,
			Map<String, Integer> termFreqTable) {
		long maxTermFrequency = 0L, docLength = 0L;
		for (String term : termFreqTable.keySet()) {
			int termFrequency = termFreqTable.get(term);
			docLength += termFrequency;
			if (termFrequency > maxTermFrequency) {
				maxTermFrequency = termFrequency;
			}

			if (!stopWords.contains(term)) {
				updateDictionaryAndPosting(docId, term, termFreqTable.get(term));
			}
		}
		System.out.println(", docId = " + docId + " maxTermFreq = "
				+ maxTermFrequency + ", docLength = " + docLength);
	}

	private void updateDictionaryAndPosting(Integer docId, String term,
			Integer termFrequency) {
		DictionaryEntry entry = dictionary.get(term);
		if (entry == null) {
			entry = new DictionaryEntry(term, 0, 0,
					new LinkedList<PostingEntry>());
			dictionary.put(term, entry);
		}
		entry.postingList.add(new PostingEntry(docId, termFrequency));
		entry.docFrequency += 1;
		entry.termFrequency += termFrequency;
	}

	@Override
	public String toString() {
		String[] terms = dictionary.keySet().toArray(new String[1]);
		Arrays.sort(terms);
		StringBuilder stringBuilder = new StringBuilder("");
		for (String term : terms) {
			IndexBuilder.DictionaryEntry entry = dictionary.get(term);
			stringBuilder.append(entry);
		}
		return stringBuilder.toString();
	}

	static class DictionaryEntry implements Serializable {
		String term;
		Integer docFrequency;
		Integer termFrequency;
		List<PostingEntry> postingList;

		public DictionaryEntry(String term, int docFrequency,
				int termFrequency, List<PostingEntry> postingList) {
			this.term = term;
			this.docFrequency = docFrequency;
			this.termFrequency = termFrequency;
			this.postingList = postingList;
		}

		@Override
		public String toString() {
			StringBuilder stringBuilder = new StringBuilder("");
			stringBuilder.append("\n" + term + " " + docFrequency + "/"
					+ termFrequency + "->");
			for (PostingEntry postingEntry : postingList) {
				stringBuilder.append(postingEntry);
			}
			stringBuilder.length();
			return stringBuilder.toString();
		}
	}

	static class PostingEntry implements Serializable {
		Integer docID;
		Integer frequency;

		public PostingEntry(Integer docID, Integer frequency) {
			this.docID = docID;
			this.frequency = frequency;
		}

		@Override
		public String toString() {
			return docID + "/" + frequency + ",";
		}
	}
}
