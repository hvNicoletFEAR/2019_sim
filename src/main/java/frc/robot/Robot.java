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

  @Override
  public void robotInit() {
    L1 = new Spark(1);
    L2 = new Spark(2);
    
    L2.setInverted(true);
    
    drive = new DifferentialDrive(L1,L2);
  }

  
  @Override
  public void robotPeriodic() {
  }

 
  @Override
  public void autonomousInit() {
    timer.stop();
    timer.reset();
    timer.start();
  }

  @Override
  public void autonomousPeriodic() {
    if (timer.get() <= 5)
      drive.arcadeDrive(1.0,0.0);
    else
      drive.arcadeDrive(0,0);
  }

  
  @Override
  public void teleopPeriodic() {
    drive.arcadeDrive(-joy.getRawAxis(0), joy.getRawAxis(4));
  }

  @Override
  public void testPeriodic() {
  }
}
