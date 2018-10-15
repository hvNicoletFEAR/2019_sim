package frc.robot;

public class Subsystem {
    public Subsystem()
    {

    }

    public void printPath()
    {
        a();
    }

    private void a()
    {
        b();
    }

    private void b()
    {
        c();
    }

    private void c()
    {
        System.out.println(Robot.trace(Robot.currentThread()));
    }
}