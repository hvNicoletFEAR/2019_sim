package frc.robot;

import java.text.DecimalFormat;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

public class Robot extends TimedRobot {

  Timer timer = new Timer();
  Joystick joy = new Joystick(0);

  double clearFirstStageMaxHeight = 5;
  double clearFirstStageMinimumAngle = 35;

  double targetHeight = 0;
  double targetAngle = 0;
  double elevatorHeight = 0;
  double wristAngle = 0;

  @Override
  public void robotInit() {
  }

  int scaleBetween(int unscaledNum, int minAllowed, int maxAllowed, int min, int max) {
    return (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min) + minAllowed;
  }

  double scaleBetween(double unscaledNum, double minAllowed, double maxAllowed, double min, double max) {
    return (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min) + minAllowed;
  }

  @Override
  public void teleopPeriodic() {
    DecimalFormat df = new DecimalFormat("###.##");

    targetHeight = scaleBetween(-joy.getRawAxis(0), 0, 10, -1, 1);
    targetHeight = Double.valueOf(df.format(targetHeight));

    targetAngle = scaleBetween(joy.getRawAxis(3), 0, 180, -1, 1);
    targetAngle = Double.valueOf(df.format(targetAngle));

    boolean badZone = targetHeight >= clearFirstStageMaxHeight && targetAngle < clearFirstStageMinimumAngle;

    if (badZone) {

      //folded, want to go high, let wrist move first
      if (wristAngle < clearFirstStageMinimumAngle && targetHeight > clearFirstStageMaxHeight) {
      
        elevatorHeight = Math.min(targetHeight, clearFirstStageMaxHeight);
        wristAngle = targetAngle;
      
      //want to fold, dont let wrist move until elevator good
      } else {
        
        elevatorHeight = targetHeight;
        wristAngle = Math.max(targetAngle, clearFirstStageMinimumAngle);
        
      }
    } else {
      elevatorHeight = targetHeight;
      wristAngle = targetAngle;
    }

    System.out.println("Elevator [" + elevatorHeight + "]   [" + targetHeight + "]" + "\t\tWrist [" + wristAngle
        + "]   [" + targetAngle + "]" + "\t\tCorrect zone: " + !badZone);
  }

  @Override
  public void testPeriodic() {
  }
}
