package com.oracle.javafx.scenebuilder.kit.metadata.spi;

import java.util.Map;

import com.oracle.javafx.scenebuilder.kit.metadata.klass.ComponentClassMetadata;

public interface IComponentClassMetadataProvider {
	
	public Map<Class<?>, ComponentClassMetadata> getMetadata();
	
}
