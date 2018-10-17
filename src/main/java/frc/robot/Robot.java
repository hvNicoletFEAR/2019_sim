package frc.robot;

import java.text.DecimalFormat;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.Robot.MovementState.Position;

public class Robot extends TimedRobot {
  static class MovementState {
    public Double height;
    public Double angle;

    public MovementState(double height, double angle, boolean forwards) {
      setSideRelative(height, angle, forwards);
    }

    public void setSideRelative(double height, double angle, boolean forwards) {
      set(height, angle * (forwards ? 1 : -1));
    }

    public void set(double height, double angle) {
      this.height = height;
      this.angle = angle;
    }

    public Double getHeight() {
      return this.height;
    }

    public Double getAngle() {
      return this.angle;
    }

    public static enum Position {
      BACKWARDS, FORWARDS, TOP
    }

    public Position getPosition() {
      if (angle > 0)
        return Position.FORWARDS;

      if (angle < 0)
        return Position.BACKWARDS;

      return Position.TOP;
    }

    public String getForwardsString() {
      String retval = "";
      retval = getPosition().equals(Position.FORWARDS) ? "Forwards" : "Backwards";
      return retval;
    }

    public String getHeightString() {
      String retval = "";
      retval = this.height.toString();
      return retval;
    }

    public String getAngleString() {
      String retval = "";
      retval = String.valueOf(Math.abs(this.angle)) + "\t" + getForwardsString();
      return retval;
    }

  }

  Timer timer = new Timer();
  DecimalFormat df = new DecimalFormat("###.##");
  Joystick joy = new Joystick(0);

  int targetCycle = 0;

  boolean useJoystick;

  MovementState[] targets = { new MovementState(0.0, 120.0, true), new MovementState(0.0, 50, false) };

  boolean ifEverBadZone = false;

  Double elevSpeed = .05;
  Double wristSpeed = .5;
  Double cycleTimeSeconds = 10.0;

  Double elevatorPrecision = .05;
  Double wristPrecision = 1.0;

  // ELEVATOR
  double kElevatorMinHeight = 0.0;
  double kElevatorMaxHeight = 10.0;

  double kElevatorFloorLevel = 2;
  double kElevatorMinHeightForFlip = 8.0;

  // WRIST
  // 0 is top, 180 is down
  /**
   * @see MovementState for forwards or backwards
   */
  double kWristMinRangeIfAboveFlipHeight = 0.0; // Top
  double kWristMinRangeIfBelowFlipHeight = 35.0; // if elevator below flip height, don't try to flip more than

  double kWristMaxRangeIfBelowFloorLevel = 90; // if elevator at floor, wrist can't move more than
  double kWristMaxRangeIfAboveFloorLevel = 130.0; // if wrist is

  double kWristMax;
  double kWristMin = 0;

  MovementState mTarget = new MovementState(kElevatorMinHeight, kWristMinRangeIfBelowFlipHeight, true);
  MovementState mOutput = new MovementState(kElevatorMinHeight, kWristMinRangeIfBelowFlipHeight, true);
  MovementState mCurrent = new MovementState(kElevatorMinHeight, kWristMinRangeIfBelowFlipHeight, true);

  public void teleopInit() {
    if (joy.getRawButton(1))
      useJoystick = true;
    else
      useJoystick = false;
  }

  @Override
  public void robotInit() {
    kWristMax = Math.max(kWristMinRangeIfAboveFlipHeight, kWristMinRangeIfBelowFlipHeight);
    kWristMax = Math.max(kWristMax, kWristMaxRangeIfBelowFloorLevel);
    kWristMax = Math.max(kWristMax, kWristMaxRangeIfAboveFloorLevel);

    timer.start();
    System.out.println("C [Current value]\t\tA [Allowed value]\t\tT [Target value]");
  }

  int scaleBetween(int unscaledNum, int minAllowed, int maxAllowed, int min, int max) {
    return (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min) + minAllowed;
  }

  double scaleBetween(double unscaledNum, double minAllowed, double maxAllowed, double min, double max) {
    return (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min) + minAllowed;
  }

  public void getTargetFromJoystick() {
    mTarget.height = scaleBetween(-joy.getRawAxis(0), kElevatorMinHeight, kElevatorMaxHeight, -1, 1);
    mTarget.angle = scaleBetween(joy.getRawAxis(3), kWristMin, kWristMax, -1, 1);
  }

  public void format(MovementState n) {
    n.angle = format(n.angle);
    n.height = format(n.height);
  }

  public Double format(Double n) {
    return Double.valueOf(df.format(n));
  }

  public void roundAllNumbers() {
    format(mCurrent);
    format(mOutput);
    format(mTarget);
  }

  public void cycleTargets() {
    if (timer.get() > cycleTimeSeconds) {
      targetCycle++;
      timer.stop();
      timer.reset();
      timer.start();
    }

    if (targetCycle > targets.length - 1)
      targetCycle = 0;

    mTarget.height = targets[targetCycle].height;
    mTarget.angle = targets[targetCycle].angle;
  }

  public void simulateMotion() {
    if (mCurrent.height > mOutput.height + elevatorPrecision)
      mCurrent.height -= elevSpeed;
    else if (mCurrent.height < mOutput.height - elevatorPrecision)
      mCurrent.height += elevSpeed;
    else
      mCurrent.height = mOutput.height;

    if (mCurrent.angle > mOutput.angle + wristPrecision)
      mCurrent.angle -= wristSpeed;
    else if (mCurrent.angle < mOutput.angle - wristPrecision)
      mCurrent.angle += wristSpeed;
    else
      mCurrent.angle = mOutput.angle;
  }

  public void teleopPeriodic() {
    if (useJoystick)
      getTargetFromJoystick();
    else
      cycleTargets();

    // This is where we would want to do something to identify impossible targets
    // and don't even attempt those

    // If we're where we shouldn't beh
    boolean badZone = mCurrent.height < kElevatorMinHeightForFlip && Math.abs(mCurrent.angle) < kWristMinRangeIfBelowFlipHeight;
    badZone |= mCurrent.height < kElevatorFloorLevel && Math.abs(mCurrent.angle) > kWristMaxRangeIfBelowFloorLevel;

    //Flip over, Dont smash into elevator
    if (mTarget.getPosition() != mCurrent.getPosition() && mCurrent.height < kElevatorMinHeightForFlip)
    {
      mOutput.height = Math.max(mTarget.height, kElevatorMinHeightForFlip);

      if (mCurrent.getPosition() == Position.BACKWARDS)
        mOutput.angle = Math.min(mTarget.angle, -kWristMinRangeIfBelowFlipHeight);
      else
        mOutput.angle = Math.max(mTarget.angle, kWristMinRangeIfBelowFlipHeight);
    // } 
    // else if (Math.abs(mTarget.angle) > kWristMaxRangeIfBelowFloorLevel && mCurrent.height > kElevatorFloorLevel) {
    //   // Dont drive into the floor
    //   mOutput.height = Math.min(mTarget.height, kElevatorFloorLevel);

    //   if (mTarget.angle < 0)
    //     mOutput.angle = Math.max(mTarget.angle, -kWristMaxRangeIfBelowFloorLevel);
    //   else
    //     mOutput.angle = Math.min(mTarget.angle, kWristMaxRangeIfBelowFloorLevel);
    } else {
      mOutput.angle = mTarget.angle;
      mOutput.height = mTarget.height;
    }

    // Hold wrist if can't flip over yet, or is trying to fold too early
    // if (mCurrent.height < kElevatorMinHeightForFlip && mCurrent.angle <
    // kWristMinRangeIfBelowFlipHeight) {
    // System.out.println("B");
    // mOutput.height = mTarget.height;
    // mOutput.angle = mTarget.angle;
    // }

    // Want to flip over
    // if (mTarget.forwards != mCurrent.forwards) {
    // //Limit height until we are flipped over
    // mOutput.height = Math.min(mTarget.height, kElevatorMinHeightForFlip);

    // //If we're not ready to flip, don't let wrist flip
    // if (mCurrent.height < kElevatorMinHeightForFlip){
    // mOutput.angle = Math.min(mTarget.angle, kWristMinRangeIfBelowFlipHeight);
    // } else {
    // //if we are ready to flip, let it flip
    // mOutput.angle = Math.min(mTarget.angle, kWristMinRangeIfAboveFlipHeight);
    // }
    // }

    // // Dont try something dumb
    mOutput.height = Math.min(mOutput.height, kElevatorMaxHeight);
    mOutput.height = Math.max(mOutput.height, kElevatorMinHeight);

    mOutput.angle = Math.min(mOutput.angle, kWristMax);
    mOutput.angle = Math.max(mOutput.angle, 0);

    simulateMotion();
    roundAllNumbers();

    if (badZone)
      ifEverBadZone = true;

    System.out.println("Elevator C[" + mCurrent.getHeightString() + "]   A[" + mOutput.getHeightString() + "]   T["
        + mTarget.getHeightString() + "]" + "\t\tWrist C[" + mCurrent.getAngleString() + "]   A["
        + mOutput.getAngleString() + "]   T[" + mTarget.getAngleString() + "]" + "\t\tCorrect zone: " + !badZone
        + "\tBadZone has occured: " + ifEverBadZone);
  }

}
