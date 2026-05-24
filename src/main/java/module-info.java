
// module-info.java
module Asac {

	requires javafx.controls;
	// requires javafx.fxml;
	requires javafx.media;
	requires java.desktop;
	requires javafx.base;
	requires java.prefs;
	requires io.github.classgraph;
	// requires javafx.web;
	// requires java.desktop;

	// opens com.ralf.javafxexperiments.control to javafx.base;

	exports com.ralf.asac;
}
