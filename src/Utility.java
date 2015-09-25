
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;




public class Utility {

	public static long getSizeOfUnCompressedIndex(Map<String, IndexBuilder.DictionaryEntry> uncompressedIndex){
		long lenght = 0;
		for(String term : uncompressedIndex.keySet()){
			IndexBuilder.DictionaryEntry entry = uncompressedIndex.get(term); 
			lenght += entry.term.length() + 2*Integer.SIZE + 8;  // Length(term) + size(df) + size(tf) + size of pointer 
			lenght += entry.postingList.size() *2*Integer.SIZE;  // list size * (size(docId) + size(tf))
		}
		return lenght;
	}
	
	public static Map<String, DictionaryEntry> createCompressedIndex(Map<String, IndexBuilder.DictionaryEntry> uncompressedIndex) {	
		Map<String, DictionaryEntry> compressedIndex = new HashMap<>();	
		for(String term : uncompressedIndex.keySet()){
			IndexBuilder.DictionaryEntry entry = uncompressedIndex.get(term); 	
			List<PostingEntry> postingList = new ArrayList<>(entry.postingList.size());
			int prevDocId = 0;	
			for(IndexBuilder.PostingEntry postingEntry: entry.postingList){
				byte[] docId = deltaCode(postingEntry.docID - prevDocId);
				byte[] frequency = gammaCode(postingEntry.frequency);
				postingList.add(new PostingEntry(docId, frequency));
			}		
			byte[] docFrequency = gammaCode(entry.docFrequency);
			byte[] termFrequency = gammaCode(entry.termFrequency);
			DictionaryEntry compressedEntry = new DictionaryEntry(term, docFrequency, termFrequency, postingList);
			compressedIndex.put(term, compressedEntry);
		}
		return compressedIndex;
	}
	
	public static long getSizeOfCompressedIndex( Map<String, DictionaryEntry> compressedIndex) {		
		long lenght = 0;
		for(String term : compressedIndex.keySet()){
			DictionaryEntry entry = compressedIndex.get(term); 
			lenght += entry.term.length() + entry.docFrequency.length + entry.termFrequency.length + 8;  // Length(term) + size(df) + size(tf) + size of pointer
			lenght += getSizeOfCompressedPostingList(entry.postingList);
		}
		return lenght;
	}

	public static long getSizeOfCompressedPostingList(List<PostingEntry> postingList) {
		long lenght = 0;
		for(PostingEntry postingEntry : postingList){
			lenght += postingEntry.docID.length + postingEntry.frequency.length;  // list size * (size(docId) + size(tf))
		}
		return lenght;
	}
	
	public static byte[] gammaCode(int number){
		String gammacode = getGammaCode(number);		
		return convertToByteArray(gammacode);
	}
		
	private static String getGammaCode(int number) {
		String binaryRep = Integer.toBinaryString(number);
		String offset = binaryRep.substring(1);	
		String unaryValue = getUnaryValue(offset.length());	
		String gammacode =  unaryValue.concat("0").concat(offset);
		return gammacode;
	}
	
	private static String getUnaryValue(int lenght) {
		String unaryValue="";
		for(int i=0;i<lenght;i++){
			unaryValue=unaryValue.concat("1");
		}
		return unaryValue;
	}
	
	private static byte[] convertToByteArray(String gammacode) {
		BitSet bitSet = new BitSet(gammacode.length());
		for(int i = 0; i < gammacode.length(); i ++){
			Boolean value = gammacode.charAt(i) == '1' ? true : false;
			bitSet.set(i, value);
		}
		return bitSet.toByteArray();
	}
	
	public static byte[] deltaCode(int number){
		String binaryRep = Integer.toBinaryString(number);
		String gammaCode = getGammaCode(binaryRep.length());
		String offset = binaryRep.substring(1);
		return convertToByteArray(gammaCode.concat(offset));
	}
	
	@SuppressWarnings("serial")
	static class DictionaryEntry implements Serializable {
		String term;
		byte[]	docFrequency;
		byte[] termFrequency;
		List<PostingEntry> postingList;
		
		public DictionaryEntry(String term, byte[] docFrequency, byte[] termFrequency, List<PostingEntry> postingList) {
			this.term = term;
			this.docFrequency = docFrequency;
			this.termFrequency = termFrequency;
			this.postingList = postingList;
		}

	}
	
	@SuppressWarnings("serial")
	static class PostingEntry implements Serializable {
		byte[] docID;
		byte[] frequency;
		public PostingEntry(byte[] docID, byte[] frequency) {
			this.docID = docID;
			this.frequency = frequency;
		}
	}
}
