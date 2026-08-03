
module Asac {

	requires javafx.controls;
	requires javafx.media;
	requires java.prefs;
	requires io.github.classgraph;

	// Requirements for systray
	requires java.desktop;

	exports com.ralf.asac;
}
