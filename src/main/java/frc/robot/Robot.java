package frc.robot;

import java.text.DecimalFormat;
import java.util.zip.Deflater;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;

public class Robot extends TimedRobot {

  Timer timer = new Timer();
  DecimalFormat df = new DecimalFormat("###.##");
  Joystick joy = new Joystick(0);

  int targetCycle = 0;

  boolean useJoystick;
  boolean defaultJoyLocationForward = true;

  /** Over the head, don't hit floor forwards */
  // MovementState[] targets = {new MovementState(0, 90, true), new
  // MovementState(0, 90, false), new MovementState(3, 130, true), new
  // MovementState(0, 90, true)};
  /**
   * Result: Raise elevator to flip direction, wait for wrist to move up before
   * lowering elevator
   */

  /** Over the head, don't hit floor backwards */
  MovementState[] targets = { new MovementState(0, 90, true), new MovementState(0, 90, false),
      new MovementState(3, 130, false), new MovementState(0, 90, false) };
  /**
   * Result: Raise elevator to flip direction, wait for wrist to move up before
   * lowering elevator
   */

  /** Over the head, try to crush arm into elevator */
  // MovementState[] targets = { new MovementState(0.0, 90, true), new
  // MovementState(0, 90, false), new MovementState(0, 35, true), new
  // MovementState(0, 30, true)};
  /**
   * Result: Raise elevator to flip direction, don't allow wrist to move inside
   * elevator
   */

  /** Straight up, try to go straight down */
  // MovementState[] targets = {new MovementState(0, 90, true), new
  // MovementState(9, 0, true), new MovementState(0, 0, true)};
  /** Result: Limit elevator to not lower and crush arm on top of itself */

  /** Intaking level, try to angle down **/
  // MovementState[] targets = {new MovementState(0, 90, true), new
  // MovementState(0, 120, true)};
  /** Result: Raise elevator so we can reach desired target angle */

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
  // -180 is down backwards, 0 is top, 180 is down
  /**
   * @see MovementState for forwards or backwards
   */
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
    kWristMax = Math.max(kWristMax, kWristMinRangeIfBelowFlipHeight);
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

  public enum Buttons {
    A(1), B(2), X(3), Y(4), LB(5), RB(6), BACK(7), START(8);

    private int number;

    private Buttons(int value) {
      number = value;
    }
  }

  public boolean getButton(Buttons b) {
    return joy.getRawButton(b.number);
  }

  /**
   * A is set direction to forward
   * B is set direction to behind
   * 
   * X = Intake
   * B = Switch
   * LB = Scale don't rotate arm
   * RB = Scale rotate arm
   * 
   * None is holster 
   */
  public void getTargetFromJoystick() {
    if (getButton(Buttons.A))
      defaultJoyLocationForward = true;
    else if (getButton(Buttons.Y))
      defaultJoyLocationForward = false;
    else if (getButton(Buttons.X))
      mTarget = new MovementState(0, 90, defaultJoyLocationForward);
    else if (getButton(Buttons.B))
      mTarget = new MovementState(3, 110, defaultJoyLocationForward);
    else if (getButton(Buttons.LB))
      mTarget = new MovementState(9.5, 35, defaultJoyLocationForward);
    else if (getButton(Buttons.RB))
      mTarget = new MovementState(9.5, 90, defaultJoyLocationForward);
    else {
      mTarget = new MovementState(0, 35, defaultJoyLocationForward);
    }
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

    // If we're where we shouldn't be
    boolean badZone = mCurrent.height < kElevatorMinHeightForFlip
        && mCurrent.getAbsoluteAngle() < kWristMinRangeIfBelowFlipHeight;
    badZone |= mCurrent.height < kElevatorFloorLevel && mCurrent.getAbsoluteAngle() > kWristMaxRangeIfBelowFloorLevel;

    // Flip over
    if (mCurrent.getPosition() != mTarget.getPosition()) {
      mOutput.height = Math.max(mTarget.height, kElevatorMinHeightForFlip);

      if (mCurrent.height < kElevatorMinHeightForFlip)
        mOutput.setAbsoluteAngleLimit(mTarget.height, kWristMinRangeIfBelowFlipHeight, false);
      else
        mOutput.angle = mTarget.angle;
    }

    // Mid flip
    else if (mCurrent.getAbsoluteAngle() < kWristMinRangeIfBelowFlipHeight) {
      mOutput.height = Math.max(mTarget.height, kElevatorMinHeightForFlip);

      mOutput.angle = mTarget.angle;
    }

    // Trying to point down
    else if (mTarget.getAbsoluteAngle() > kWristMaxRangeIfBelowFloorLevel) {
      // Limit height to as low as it can be and still point down
      mOutput.height = Math.max(mTarget.height, kElevatorFloorLevel);

      // If we aren't to that height yet, limit to floor level
      if (mCurrent.height < kElevatorFloorLevel) {
        mOutput.setAbsoluteAngleLimit(mTarget.angle, kWristMaxRangeIfBelowFloorLevel, true);
      } else {
        mOutput.angle = mTarget.angle;
      }

      // Are pointing down, need to point up (implied target less than max floor level
      // range)
    } else if (mCurrent.getAbsoluteAngle() > kWristMaxRangeIfBelowFloorLevel) {
      // Raise
      mOutput.height = Math.max(mTarget.height, kElevatorFloorLevel);
      mOutput.angle = mTarget.angle;

      // Impossible move, don't raise elevator, just limit arm
    } else if (mTarget.height < kElevatorFloorLevel && mTarget.getAbsoluteAngle() < kWristMinRangeIfBelowFlipHeight) {
      mOutput.height = mTarget.height;

      mOutput.setAbsoluteAngleLimit(mTarget.angle, kWristMinRangeIfBelowFlipHeight, false);

      // No problem, just move
    } else {
      mOutput.set(mTarget);
    }

    // // Dont try something dumb
    mOutput.height = Math.min(mOutput.height, kElevatorMaxHeight);
    mOutput.height = Math.max(mOutput.height, kElevatorMinHeight);

    mOutput.angle = Math.min(mOutput.angle, kWristMax);
    mOutput.angle = Math.max(mOutput.angle, -kWristMax);

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