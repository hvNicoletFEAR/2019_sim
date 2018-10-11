package frc.robot;

import java.io.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Arrays;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.Timer;

public class Robot extends TimedRobot {

  Timer timer = new Timer();
  DecimalFormat df = new DecimalFormat("###.##");
  Joystick joy = new Joystick(0);

  Double currentHeight = 0.0;
  Double currentAngle = 0.0;
  Double targetHeight = 0.0;
  Double targetAngle = 0.0;
  Double outputHeight = 0.0;
  Double outputAngle = 0.0;
  int targetCycle = 0;

  boolean useJoystick;
  double[] heightTargets = { 0, 10 };
  double[] angleTargets = { 0, 180 };

  boolean waitUp = false;

  Double elevSpeed = .05 / (waitUp ? 1.0 : 5.0);
  Double wristSpeed = 1.0 / (waitUp ? 5.0 : 1.0);
  Double cycleTimeSeconds = (waitUp ? 7.0 : 14.0);
  Double elevatorPrecision = .05;
  Double wristPrecision = 1.0;

  Double elevatorMaxHeight = 10.0;
  Double wristMaxRange = 180.0; // wrist stowed inside elevator, pointing towards center of robot = 0deg,
                                // pointing outwards is 180
  Double clearFirstStageMaxHeight = 5.0;
  Double clearFirstStageMinimumAngle = 35.0;

  public void teleopInit() {
    if (joy.getRawButton(1))
      useJoystick = true;
    else
      useJoystick = false;
  }

  @Override
  public void robotInit() {
    timer.start();
  }

  public void robotPeriodic() {

  }

  int scaleBetween(int unscaledNum, int minAllowed, int maxAllowed, int min, int max) {
    return (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min) + minAllowed;
  }

  double scaleBetween(double unscaledNum, double minAllowed, double maxAllowed, double min, double max) {
    return (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min) + minAllowed;
  }

  public void getTargetFromJoystick() {
    targetHeight = scaleBetween(-joy.getRawAxis(0), 0, elevatorMaxHeight, -1, 1);

    targetAngle = scaleBetween(joy.getRawAxis(3), 0, wristMaxRange, -1, 1);
  }

  public Double format(Double n) {
    return Double.valueOf(df.format(n));
  }

  public void roundAllNumbers() {
    targetHeight = format(targetHeight);
    targetAngle = format(targetAngle);
    currentHeight = format(currentHeight);
    currentAngle = format(currentAngle);
    outputHeight = format(outputHeight);
    outputAngle = format(outputAngle);
  }

  public void cycleTargets() {
    if (timer.get() > cycleTimeSeconds) {
      targetCycle++;
      timer.stop();
      timer.reset();
      timer.start();
    }

    if (targetCycle > heightTargets.length - 1 || targetCycle > angleTargets.length - 1)
      targetCycle = 0;

    targetHeight = heightTargets[targetCycle];
    targetAngle = angleTargets[targetCycle];
  }

  public void simulateMotion() {

    if (currentHeight > outputHeight + elevatorPrecision)
      currentHeight -= elevSpeed;
    else if (currentHeight < outputHeight - elevatorPrecision)
      currentHeight += elevSpeed;
    else
      currentHeight = outputHeight;

    if (currentAngle > outputAngle + wristPrecision)
      currentAngle -= wristSpeed;
    else if (currentAngle < outputAngle - wristPrecision)
      currentAngle += wristSpeed;
    else
      currentAngle = outputAngle;
  }

  @Override
  public void teleopPeriodic() {
    if (useJoystick)
      getTargetFromJoystick();
    else
      cycleTargets();

      
    // This is where we would want to do something to identify impossible targets
    // and don't even attempt those

    // If we're where we shouldn't be
    boolean badZone = currentHeight >= clearFirstStageMaxHeight && currentAngle < clearFirstStageMinimumAngle;

    if (badZone) {
      
      // folded, want to go high, let wrist move first
      if (currentAngle <= clearFirstStageMinimumAngle && targetHeight >= clearFirstStageMaxHeight) {
        
        outputHeight = Math.min(targetHeight, clearFirstStageMaxHeight);
        outputAngle = targetAngle;
        
      }
      
      // want to fold, dont let wrist move until elevator good
      if (targetAngle <= clearFirstStageMinimumAngle && currentHeight >= clearFirstStageMaxHeight) {

        outputHeight = targetHeight;
        outputAngle = Math.max(targetAngle, clearFirstStageMinimumAngle + 5);
      
      }
      //TODO Fix that when folding down, once wrist moves as much as it can, it thinks it can move more and starts to move, realizes it cant, then stops 

    } else {
      outputHeight = targetHeight;
      outputAngle = targetAngle;
    }
    
    // Dont let it crash
    // outputHeight = Math.min(Math.abs(outputHeight), elevatorMaxHeight);
    // outputAngle = Math.min(Math.abs(outputAngle), wristMaxRange);
    
    simulateMotion();
    roundAllNumbers();
    System.out
        .println("Elevator [" + outputHeight + "]   [" + currentHeight + "]   [" + targetHeight + "]" + "\t\tWrist ["
            + outputAngle + "]   [" + currentAngle + "]   [" + targetAngle + "]" + "\t\tCorrect zone: " + !badZone);
  }

}
