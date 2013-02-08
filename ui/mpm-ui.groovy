///////////////////////////////////////////////////////////////////////////////
//
//
//
///////////////////////////////////////////////////////////////////////////////

//INIT_COMMON = 1
//evaluate(new File("scripts/_global.groovy")) 	// Include common properties

System.properties.putAll( ["http.proxyHost":"proxy.intra.bt.com", "http.proxyPort":"8080"] )

// Build MPM UI Main Window adn display it
MPM_MAIN_WINDOW = new MainWindow()
MPM_MAIN_WINDOW.show()