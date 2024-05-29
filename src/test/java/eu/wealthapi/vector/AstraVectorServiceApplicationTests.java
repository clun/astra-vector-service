package eu.wealthapi.vector;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.AstraDBVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Slf4j
@SpringBootTest
class AstraVectorServiceApplicationTests {

	@Autowired
	AstraDBVectorStore astraDBVectorStore;

	private String loadText(String uri) {
		var resource = new DefaultResourceLoader().getResource(uri);
		try {
			return resource.getContentAsString(StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Value("classpath:website-api-documentation.txt")
	Resource apiDocumentation;

	@Value("classpath:website-plug-play.txt")
	Resource plugPlay;

	private void ingest(Resource resource, String url) {
		// Document READER
		TextReader textReader = new TextReader(apiDocumentation);
		textReader.getCustomMetadata().put("filename", resource.getFilename());
		textReader.getCustomMetadata().put("url", url);
		textReader.getCustomMetadata().put("ingestionDate", new Date());
		List<Document> singleDoc = textReader.read();

		// Document TRANSFORMER
		TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
		List<Document> docs = tokenTextSplitter.apply(singleDoc);

		// Document WRITER (ASTRA DB)
		astraDBVectorStore.accept(docs);
	}

	@Test
	void shouldDemoNaiveRag() {
		// Empty the Store
		astraDBVectorStore.clear();

		// Ingest a couple of documents
		ingest(apiDocumentation, "https://wealthapi.eu/en/api-documentation/");
		ingest(plugPlay, "https://wealthapi.eu/en/plug-play/");

		// Similarity Search
		
	}

}
