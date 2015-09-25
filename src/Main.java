
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Main {

	public static String PathDirectory;
	public static String StopWordFile = "./stopwords";

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {

		if (args.length < 1) {
			System.out.println("Enter Path of Directory = ");
			Scanner scanner = new Scanner(System.in);
			PathDirectory = scanner.nextLine();
		} else {
			PathDirectory = args[0];
		}

		Set<String> stopWords = readStopWords(StopWordFile);
		IndexBuilder indexBuilder = new IndexBuilder(stopWords);

		long startTime = System.currentTimeMillis();
		Map<String, IndexBuilder.DictionaryEntry> uncompressedIndex = indexBuilder
				.buildIndex(PathDirectory);
		long endTime = System.currentTimeMillis();

		long uncompressedLenght = Utility
				.getSizeOfUnCompressedIndex(uncompressedIndex);

		Map<String, Utility.DictionaryEntry> compressedIndex = Utility
				.createCompressedIndex(uncompressedIndex);
		long compressedLenght = Utility
				.getSizeOfCompressedIndex(compressedIndex);
		System.out.println("");
		System.out.println("1. Time required to build Index_Version2 = "
				+ (endTime - startTime) + " milliseconds");
		System.out.println("2. Size of the Index_Version2 uncompressed = "
				+ uncompressedLenght + " bytes");
		System.out.println("3. Size of the Index_Version2 compressed   = "
				+ compressedLenght + " bytes");
		System.out
				.println("4. number of inverted lists in the Index_Version2 = "
						+ uncompressedIndex.size());
		System.out
				.println("5. term,df,tf,and inverted list length (in bytes) for the given terms ");
		System.out.println(String.format("\n\t %-10s  %-10s %-10s %-10s bytes",
				"Term", "DF", "TF", "Inverted List Length"));

		String[] terms = { "Reynolds", "NASA", "Prandtl", "flow", "pressure",
				"boundary", "shock" };
		Porter porterStemmer = new Porter();
		for (String term : terms) {
			String stemmedTerm = porterStemmer.stripAffixes(term.toLowerCase());
			IndexBuilder.DictionaryEntry entry = uncompressedIndex
					.get(stemmedTerm);
			System.out.println(String.format(
					"\t %-10s  %-10d %-10d %-10d bytes", stemmedTerm,
					entry.docFrequency, entry.termFrequency, Utility
							.getSizeOfCompressedPostingList(compressedIndex
									.get(stemmedTerm).postingList)));
		}
	}

	private static Set<String> readStopWords(String filename)
			throws FileNotFoundException {
		Set<String> stopWords = new HashSet<>();
		Scanner scanner = new Scanner(new File(filename));
		while (scanner.hasNext()) {
			stopWords.add(scanner.next());
		}
		scanner.close();
		return stopWords;
	}
}
