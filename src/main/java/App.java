import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import controller.FlooringMasteryController;

public class App {
	public static void main(String[] args) {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		FlooringMasteryController controller = ctx.getBean("controller", FlooringMasteryController.class);
		controller.run();
	}
}
