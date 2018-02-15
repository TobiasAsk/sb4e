package no.tobask.sb4e;

import com.oracle.javafx.scenebuilder.kit.library.BuiltinLibrary;

public class CustomClassLoaderLibrary extends BuiltinLibrary {
	
	public CustomClassLoaderLibrary(ClassLoader classLoader) {
		super();
		classLoaderProperty.set(classLoader);
	}
}
