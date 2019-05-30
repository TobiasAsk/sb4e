package no.tobask.sb4e.coherence;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	private String controllerName;

	private static final String FX_PREFIX = "fx";
	private static final String FX_ID = "id";
	private static final String FX_CONTROLLER = "controller";
	private static final String FX_EVENT_HANDLER_PREFIX = "on";

	public void setDocumentLocation(URL documentLocation) {
		this.documentLocation = documentLocation;
		clear();
	}

	private void clear() {
		fxIds.clear();
		eventHandlers.clear();
		qualifiedTypeNames.clear();
		controllerName = null;
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
						String attributeValue = reader.getAttributeValue(i);
						if (FX_PREFIX.equals(prefix)) {
							if (FX_ID.equals(attributeName)) {
								String componentType = qualifiedName(reader.getLocalName());
								fxIds.put(attributeValue, componentType);
							} else if (FX_CONTROLLER.equals(attributeName)) {
								controllerName = attributeValue;
							}
						}
						else if (attributeName.startsWith(FX_EVENT_HANDLER_PREFIX)) {
							eventHandlers.add(attributeValue.substring(1));
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
		return new HashMap<>(fxIds);
	}

	public List<String> getEventHandlers() {
		return new ArrayList<String>(eventHandlers);
	}

	public String getControllerName() {
		return controllerName;
	}

	public static void main(String[] args) {
		Set<String> viewHandlers = new HashSet<>(Arrays.asList("A", "B"));
		Set<String> controllerHandlers = new HashSet<>(Arrays.asList("A", "C"));
		Set<String> missingFromController = new HashSet<>(viewHandlers);
		missingFromController.removeAll(controllerHandlers);
		System.out.println(missingFromController);
	}

}
