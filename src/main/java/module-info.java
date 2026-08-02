
module Asac {

	requires javafx.controls;
	requires javafx.media;
	requires java.prefs;
	requires io.github.classgraph;

	// Requirements for systray
	requires java.desktop;
	requires javafx.swing;
	requires com.dustinredmond.fxtrayicon;

	requires dorkbox.systemtray;

	exports com.ralf.asac;
}
