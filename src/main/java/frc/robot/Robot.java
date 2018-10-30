package frc.robot;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.followers.EncoderFollower;
import jaci.pathfinder.modifiers.TankModifier;

public class Robot extends TimedRobot {

  Timer timer = new Timer();
  Joystick joy = new Joystick(0);
  Spark L1, L2;
  DifferentialDrive drive;

  // Config
  Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_HIGH,
      0.05, 1.7, 2.0, 60.0);

  // WPI Encoders
  Encoder leftWPIEncoder = new Encoder(0, 0);
  Encoder rightWPIEncoder = new Encoder(0, 0);

  Waypoint[] points = new Waypoint[] { new Waypoint(-4, -1, Pathfinder.d2r(-45)), // Waypoint @ x=-4, y=-1, exit
                                                                                  // angle=-45 degrees
      new Waypoint(-2, -2, 0), // Waypoint @ x=-2, y=-2, exit angle=0 radians
      new Waypoint(0, 0, 0) // Waypoint @ x=0, y=0, exit angle=0 radians
  };

  Trajectory trajectory = Pathfinder.generate(points, config);

  TankModifier modifier = new TankModifier(trajectory).modify(0.5);

  EncoderFollower left = new EncoderFollower(modifier.getLeftTrajectory());
  EncoderFollower right = new EncoderFollower(modifier.getRightTrajectory());

  @Override
  public void robotInit() {

    left.configureEncoder(0, 4096, 6);
    right.configureEncoder(0, 4096, 6);

    left.configurePIDVA(1.0, 0.0, 0.0, 1 / 1, 0);
    right.configurePIDVA(1.0, 0.0, 0.0, 1 / 1, 0);

    L1 = new Spark(1);
    L2 = new Spark(2);

    L2.setInverted(true);

    drive = new DifferentialDrive(L1, L2);
  }

  @Override
  public void robotPeriodic() {
    double l = left.calculate(leftWPIEncoder.get());
    double r = right.calculate(rightWPIEncoder.get());

    System.out.println(l + "\t" + r);
    
  }

  @Override
  public void autonomousInit() {
    timer.stop();
    timer.reset();
    timer.start();
  }

  double scaleBetween(double unscaledNum, double minAllowed, double maxAllowed, double min, double max) {
    return (maxAllowed - minAllowed) * (unscaledNum - min) / (max - min) + minAllowed;
  }

  @Override
  public void autonomousPeriodic() {
    if (timer.get() <= 5) {
      drive.tankDrive(scaleBetween(timer.get(), -1, 1, 0, 5), 0);
    } else if (timer.get() >= 5 && timer.get() <= 10) {
      drive.tankDrive(0, scaleBetween(timer.get(), -1, 1, 0, 5));
    } else {
      timer.stop();
      timer.reset();
      timer.start();
    }
    // if (timer.get() <= 5)
    // drive.arcadeDrive(1.0,0.0);
    // else
    // drive.arcadeDrive(0,0);
  }

  @Override
  public void teleopPeriodic() {
    drive.arcadeDrive(-joy.getRawAxis(0), joy.getRawAxis(4));
  }

  @Override
  public void testPeriodic() {
  }
}
