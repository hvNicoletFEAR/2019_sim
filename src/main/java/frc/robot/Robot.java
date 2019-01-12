package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

public class Robot extends TimedRobot {

  Timer timer = new Timer();
  Joystick joy = new Joystick(0);
  Spark L1, L2;
  DifferentialDrive drive;

  boolean[] boolArray = { false, true, true, false, true };
  Spark[] sparkArray = { L1, L2 };
  String[] stringArray = { "A", "B", "C", "D" };
  int[] intArray = { 2, 4, 5, 5 };
  Joystick[] joystickArray = { joy, new Joystick(1) };
  double[] array = { 1.1, 2.2, 3.3, 4.4, 5.5 };

  @Override
  public void robotInit() {

    for (int i = 0; i < intArray.length; i++) {
      System.out.println(intArray[i]);
    }
    for (int i = 0; i < intArray.length; i++) {
      intArray[i] = intArray[i] * 2;

    }
    for (int i = 0; i < intArray.length; i++) {
      intArray[i] = intArray[i] + 3;
    }

    for (int i = 0; i < intArray.length; i++) {
      System.out.println(intArray[i]);
    }

    // System.out.println("BoolArray: " + boolArray.length);
    // System.out.println("SparkArray: " + sparkArray.length);

    // 1 + 2 = 3;
    // "A" + "B" = "AB";
    // "A" + 2 = "A2";
    // "A" + 2 + 3 = "A23";
    // "A" + (2 + 3) = "A5";

    // System.out.println(stringArray[0]);
    // System.out.println(array[2]);
  }

  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
  }

  @Override
  public void robotPeriodic() {
  }

  @Override
  public void autonomousInit() {
  }

  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopPeriodic() {
  }

  @Override
  public void testPeriodic() {
  }
}
