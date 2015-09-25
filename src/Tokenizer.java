
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeSet;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

@SuppressWarnings("unused")
public class Tokenizer {

	private Read read;
	private Map<String, Integer> termIndexTable;
	private Porter porterStemmer;

	public Tokenizer() throws Exception {
		read = new Read();
		porterStemmer = new Porter();
	}

	public Map<String, Integer> buildTermFreqTable(File file) throws Exception {
		termIndexTable = new HashMap<String, Integer>();
		if (file.isFile()) {
			String plainText = read.parse(file);
			plainText = plainText.replaceAll("[^\\w\\s-']+", " ").toLowerCase();
			Scanner scanner = new Scanner(plainText);
			while (scanner.hasNext()) {
				String token = scanner.next();
				processAndInsertToken(token);
			}
			scanner.close();
		}
		return termIndexTable;
	}

	private void processAndInsertToken(String token) {
		if (token.endsWith("'s")) {
			insertInToTable(token.replace("'s", ""));
		} else if (token.contains("-")) {
			splitAndInsertToken(token, "-");
		} else if (token.contains("_")) {
			splitAndInsertToken(token, "_");
		} else {
			insertInToTable(token);
		}

	}

	private void splitAndInsertToken(String token, String splitBy) {
		String[] newTokens = token.split(splitBy);
		for (String newToken : newTokens) {
			insertInToTable(newToken);
		}
	}

	private void insertInToTable(String token) {
		token = token.replaceAll("[']+", "");
		if (token.length() > 0)
			token = porterStemmer.stripAffixes(token);
		if (token.length() > 0) {
			if (termIndexTable.containsKey(token)) {
				termIndexTable.put(token, termIndexTable.get(token) + 1);
			} else {
				termIndexTable.put(token, 1);
			}
		}
	}
}
