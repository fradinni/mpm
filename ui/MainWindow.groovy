import groovy.swing.SwingBuilder
import javax.swing.*
import java.awt.*

class MainWindow {

	public MainWindow() {}

	public void show() {
		def swingBuilder = new SwingBuilder()
		swingBuilder.frame(title:"Minecraft Package Manager", 
                       defaultCloseOperation:JFrame.EXIT_ON_CLOSE, 
                       size:[800,500],
                       show:true) {
    	}
	}
}