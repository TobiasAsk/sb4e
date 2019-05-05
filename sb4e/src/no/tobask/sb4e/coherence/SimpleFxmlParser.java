package no.tobask.sb4e.coherence;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class SimpleFxmlParser {

	private URL documentLocation;
	private Map<String, String> fxIds = new HashMap<>(); // id -> type
	private List<String> eventHandlers = new ArrayList<>();
	private static XMLInputFactory factory = XMLInputFactory.newInstance();
	private List<String> qualifiedTypeNames = new ArrayList<>();

	public void setDocumentLocation(URL documentLocation) {
		this.documentLocation = documentLocation;
	}

	public void parseDocument() throws IOException, XMLStreamException {
		try (InputStream input = documentLocation.openStream()) {
			XMLStreamReader reader = factory.createXMLStreamReader(input);
			while (reader.hasNext()) {
				int event = reader.next();
				if (event == XMLStreamConstants.START_ELEMENT) {
					for (int i = 0; i < reader.getAttributeCount(); i++) {
						String prefix = reader.getAttributePrefix(i);
						String attributeName = reader.getAttributeLocalName(i);
						if ("fx".equals(prefix)) {
							if ("id".equals(attributeName)) {
								String fxId = reader.getAttributeValue(i);
								String componentType = qualifiedName(reader.getLocalName());
								fxIds.put(fxId, componentType);
							}
						}
						else if (attributeName.startsWith("on")) {
							String handlerNameWithHash = reader.getAttributeValue(i);
							eventHandlers.add(handlerNameWithHash.substring(1));
						}
					}
				}
				else if (event == XMLStreamConstants.PROCESSING_INSTRUCTION) {
					qualifiedTypeNames.add(reader.getPIData());
				}
			}
		}
	}

	private String qualifiedName(String localName) {
		return qualifiedTypeNames.stream().filter(n -> n.endsWith(localName)).findFirst().get();
	}

	public Map<String, String> getFxIds() {
		return Collections.unmodifiableMap(fxIds);
	}

	public List<String> getEventHandlers() {
		return Collections.unmodifiableList(eventHandlers);
	}

}
