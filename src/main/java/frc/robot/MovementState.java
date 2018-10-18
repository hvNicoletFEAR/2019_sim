package frc.robot;

public class MovementState {
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

    public void set(MovementState other)
    {
      this.height = other.height;
      this.angle = other.angle;
    }

    public void setAbsoluteAngleLimit(double target, double limit, boolean dontGoBelow)
    {
      if (this.getPosition() == Position.FORWARDS) {

        if (dontGoBelow)
          this.angle = Math.min(target, limit);
        else
          this.angle = Math.max(target, limit);

      } else {

        if (dontGoBelow)
          this.angle = Math.max(target, -limit);
        else
          this.angle = Math.min(target, -limit);
      }
    }

    public Double getAbsoluteAngle()
    {
      return Math.abs(this.angle);
    }

    public static enum Position {
      BACKWARDS, FORWARDS
    }

    public Position getPosition() {
      if (angle > 0)
        return Position.FORWARDS;
        
      return Position.BACKWARDS;
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
      retval = String.valueOf(getAbsoluteAngle()) + "\t" + getForwardsString();
      return retval;
    }

  }
