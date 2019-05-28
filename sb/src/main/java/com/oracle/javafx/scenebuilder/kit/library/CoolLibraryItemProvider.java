package com.oracle.javafx.scenebuilder.kit.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class CoolLibraryItemProvider implements IExternalLibraryItemProvider {

	@Override
	public Collection<Class<?>> getItems() {
		return new ArrayList<>(Arrays.asList(javafx.scene.control.Button.class));
	}

}
