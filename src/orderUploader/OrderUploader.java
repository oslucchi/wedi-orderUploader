package orderUploader;

import java.io.IOException;

import orderUploader.utils.ApplicationProperties;

public class OrderUploader {
	public static void main(String[] args) throws IOException {
		ApplicationProperties ap;
		ap = ApplicationProperties.getInstance();
//		PDFReader.readFile(ap);
		EmailReader.gmailReader(ap);
//		EmailReader.gmailReaderTest(ap);
	}

}
