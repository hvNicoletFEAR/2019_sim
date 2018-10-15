package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import frc.robot.Subsystem;

public class Robot extends TimedRobot {

  Subsystem sub = new Subsystem();

  public void teleopInit()
  {
    sub.printPath();
  }

  public static StackTraceElement[] currentThread()
  {
    return Thread.currentThread().getStackTrace();
  }
  /**
	 * Thread.currentThread().getStackTrace();
	 */
	public static String trace(StackTraceElement e[]) {

		String retval = "";
		try {
			for (int i = e.length - 5; i > 1; i--) {
				retval += e[i].getMethodName();

				if (i != 2)
					retval += ".";
			}
		} catch (Exception ex) {
			System.out.println(
					"Max was a dummy that tried to write something to make his life easier but he made it much much harder");
			// ex.printStackTrace();
		}

		return retval;
  }
}
