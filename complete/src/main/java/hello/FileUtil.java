package hello;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FileUtil {

	private static final String FILENAME = "/Users/ashokarulsamy/projects/poc/REQUEST_XML_TRAIL_BALANCE.xml";

	public static String getFileDataAsString() {

		BufferedReader br = null;
		FileReader fr = null;
		StringBuilder data = new StringBuilder();

		try {

			//br = new BufferedReader(new FileReader(FILENAME));
			fr = new FileReader(FILENAME);
			br = new BufferedReader(fr);

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				//System.out.println(sCurrentLine);
				
				data.append(sCurrentLine.trim());
			}

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (br != null)
					br.close();

				if (fr != null)
					fr.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}
		
		return data.toString();

	}

}
