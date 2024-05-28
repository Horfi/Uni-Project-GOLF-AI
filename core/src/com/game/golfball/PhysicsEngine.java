package com.game.golfball;


import com.badlogic.gdx.math.Vector3;
import com.game.terrain.GetHeight;
import com.game.terrain.TerrainV2;

public class PhysicsEngine {

    double X0, Y0; // initial position of the ball
    double Xt, Yt, Rt; // position of the target and its radius
    double GRASS_K, GRASS_S; // kinetic and static coefficients on the grass
    double SAND_K, SAND_S; // kinetic and static coefficients on the sand
    public static String heightFunction; // h(x,y) function of the height profile
    double xMapStart = 0, xMapEnd = 50, yMapStart = 0, yMapEnd = 50; // x and y map limits
    double maxVelocity = 5;
    double xInitialVelocity = 0, zInitialVelocity = 0;
    double terrainHeight = 0;

    final double g = 9.80665;
    final static double LIMIT_ZERO = 0.0000001;
    final double h = 0.005; // Reduced step size for better precision
    private double currentTime = 0.0;

    double[] stateVector = new double[4];
    double[] systemFunction = new double[4];

    public PhysicsEngine(String heightFunction, double X0, double Y0, double Xt, double Yt, double Rt, double GRASS_K,
                         double GRASS_S, double SAND_K, double SAND_S, double xInitialVelocity, double zInitialVelocity) {

        PhysicsEngine.heightFunction = heightFunction;
        this.X0 = X0;
        this.Y0 = Y0;
        this.Xt = Xt;
        this.Yt = Yt;
        this.Rt = Rt;
        this.GRASS_K = GRASS_K;
        this.GRASS_S = GRASS_S;
        this.SAND_K = SAND_K;
        this.SAND_S = SAND_S;
        this.xInitialVelocity = xInitialVelocity;
        this.zInitialVelocity = zInitialVelocity;
    }

    public static void main(String[] args) {
        PhysicsEngine testEngine = new PhysicsEngine(
                " sqrt ( ( sin ( 0.1 * x ) + cos ( 0.1 * y ) ) ^ 2 ) + 0.5 * sin ( 0.3 * x ) * cos ( 0.3 * y ) ",
                5, 2, 4, 1, 0.15, 1, 0.5, 0.3, 0.4, 0.0, 0.0
        );

        double[] a = testEngine.runSimulation(5, 5);

        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]);
        }
    }

    // Set desired initial velocities and position of the ball
    public void setState(double x0, double y0, double vx0, double vy0) {
        this.X0 = x0;
        this.Y0 = y0;
        this.xInitialVelocity = vx0;
        this.zInitialVelocity = vy0;
    }

    public double[] runSimulation(double xInitialVelocity, double yInitialVelocity) {
        // Initialize the state vector for position and velocity
        stateVector[0] = X0;
        stateVector[1] = Y0;
        stateVector[2] = xInitialVelocity;
        stateVector[3] = yInitialVelocity;

        // System.out.println(
        //         "Initial State: X0=" + X0 + ", Y0=" + Y0 + ", Vx0=" + xInitialVelocity + ", Vy0=" + yInitialVelocity);

        int maxIterations = 1000; // Set a reasonable limit for iterations
        int iteration = 0;

        double[] previousState = new double[4];
        System.arraycopy(stateVector, 0, previousState, 0, stateVector.length);

        double positionThreshold = 0.0001; // Increased precision
        double velocityThreshold = 0.0001; // Increased precision

        // Simulation loop: run until both velocities are close to zero or max iterations reached
        while (true) {
            updateStateVectorEuler(false);

            if (GetHeight.getHeight(heightFunction, stateVector[0], stateVector[1]) < 0){
                stateVector[2] = 0;
                stateVector[3] = 0;
                break;
            }

            // Check for small changes in position and velocity
            double positionChange = Math.sqrt(
                    Math.pow(stateVector[0] - previousState[0], 2) +
                            Math.pow(stateVector[1] - previousState[1], 2)
            );
            double velocityChange = Math.sqrt(
                    Math.pow(stateVector[2] - previousState[2], 2) +
                            Math.pow(stateVector[3] - previousState[3], 2)
            );

            // Debugging: print intermediate states
            // if (iteration % 100 == 0) {
            //     System.out.println("Iteration: " + iteration + ", Position Change: " + positionChange + ", Velocity Change: " + velocityChange);
            //     System.out.println("Current State: X=" + stateVector[0] + ", Z=" + stateVector[1] + ", Vx=" + stateVector[2] + ", Vz=" + stateVector[3]);
            // }

            // Early stopping if changes are very small
            if (positionChange < positionThreshold && velocityChange < velocityThreshold) {
                System.out.println("Early stopping due to small changes in state.");
                break;
            }

            if(Math.abs(stateVector[2]) <0.01 && Math.abs(stateVector[3]) <0.01){
                //System.out.println("fast end");
                break;
            }

            // Update the previous state
            System.arraycopy(stateVector, 0, previousState, 0, stateVector.length);

            iteration++;
            if (iteration >= maxIterations) {
                //System.out.println("Max iterations reached. Stopping simulation.");
                break;
            }
        }

       // System.out.println("Updated State: X=" + stateVector[0] + ", Z=" + stateVector[1] + ", Vx=" + stateVector[2] + ", Vz=" + stateVector[3]);

        return stateVector;
    }

    public void updateStateVectorRungeKutta(double time, boolean isImmobile) {
        double x = stateVector[0];
        double z = stateVector[1];

        double kineticCoefficient = isWithinSandArea(x, z) ? SAND_K : GRASS_K;
        // Calculate the next state vector using the Runge-Kutta method

        double[] stateVector1 = returnUpdatedVector(time, isImmobile, x, z, kineticCoefficient);
        double[] stateVector2 = returnUpdatedVector(time + 0.5 * h, isImmobile, x + 0.5 * h, z + 0.5 * h, kineticCoefficient);
        double[] stateVector3 = returnUpdatedVector(time + 0.5 * h, isImmobile, x + 0.5 * h, z + 0.5 * h, kineticCoefficient);
        double[] stateVector4 = returnUpdatedVector(time + h, isImmobile, x + h, z + h, kineticCoefficient);

        for (int i = 0; i < stateVector.length; i++) {
            stateVector[i] += (1.0 / 6.0) * h * (stateVector1[i] + 2 * stateVector2[i] + 2 * stateVector3[i] + stateVector4[i]);
        }

        // Check if the velocity is low enough to consider the ball stopped
        if (Math.sqrt(stateVector[2] * stateVector[2] + stateVector[3] * stateVector[3]) < LIMIT_ZERO) {
            stateVector[2] = 0;
            stateVector[3] = 0;
        }
    }

    public double[] returnUpdatedVector(double t, boolean isImmobile, double x, double z, double kineticCoefficient) {
        double xVelocity = stateVector[2];
        double zVelocity = stateVector[3];

        double slopeX = calculateDerivativeX(x, z);
        double slopeZ = calculateDerivativeZ(x, z);

        double xFirstTerm = -g * slopeX;
        double zFirstTerm = -g * slopeZ;

        double xSecondTerm;
        double zSecondTerm;

        double normVelocity = Math.sqrt(xVelocity * xVelocity + zVelocity * zVelocity);
        // Determine friction components based on motion state
        if (normVelocity <= LIMIT_ZERO) {
            // Friction acts against the slope
            xSecondTerm = -kineticCoefficient * g * (slopeX / Math.sqrt(slopeX * slopeX + slopeZ * slopeZ));
            zSecondTerm = -kineticCoefficient * g * (slopeZ / Math.sqrt(slopeX * slopeX + slopeZ * slopeZ));
        } else {
            // Friction acts against velocity
            xSecondTerm = -kineticCoefficient * g
                    * (xVelocity / normVelocity);
            zSecondTerm = -kineticCoefficient * g
                    * (zVelocity / normVelocity);
        }

        // Calculate total acceleration
        double xAcceleration = xFirstTerm + xSecondTerm;
        double zAcceleration = zFirstTerm + zSecondTerm;

        //double windForceX = 0.1 * Math.sin(t); 
        //double windForceZ = 0.1 * Math.cos(t);

        // Update system function array
        systemFunction[0] = stateVector[2]; // xVelocity
        systemFunction[1] = stateVector[3]; // zVelocity
        systemFunction[2] = xAcceleration;
        systemFunction[3] = zAcceleration;

        return systemFunction;
    }

    public void updateStateVectorEuler(boolean isImmobile) {
        double x = stateVector[0];
        double z = stateVector[1];

        double xVelocity = stateVector[2];
        double zVelocity = stateVector[3];

        double normVelocity = Math.sqrt(xVelocity * xVelocity + zVelocity * zVelocity);

        // Calculate the slope of the height field at the current position
        double slopeX = calculateDerivativeX(x, z);
        double slopeZ = calculateDerivativeZ(x, z);

        // Determine the coefficient of kinetic friction based on the terrain
        double kineticCoefficient = isWithinSandArea(x, z) ? SAND_K : GRASS_K;

        // Calculate the force of gravity acting along the slope
        double gravityForceX = -g * slopeX;
        double gravityForceZ = -g * slopeZ;

        // Calculate friction force magnitude; it should oppose the velocity vector
        double frictionForceX = normVelocity > LIMIT_ZERO ? kineticCoefficient * g * (xVelocity / normVelocity)
                : kineticCoefficient * g * (slopeX / Math.sqrt(slopeX * slopeX + slopeZ * slopeZ));
        double frictionForceZ = normVelocity > LIMIT_ZERO ? kineticCoefficient * g * (zVelocity / normVelocity)
                : kineticCoefficient * g * (slopeZ / Math.sqrt(slopeX * slopeX + slopeZ * slopeZ));

        // Update accelerations by combining gravitational and frictional forces
        double xAcceleration = gravityForceX - frictionForceX;
        double zAcceleration = gravityForceZ - frictionForceZ;

        // Apply Euler's method to update velocities and positions
        stateVector[2] += xAcceleration * h; // Update x velocity
        stateVector[3] += zAcceleration * h; // Update z velocity

        // Update positions based on new velocities
        stateVector[0] += stateVector[2] * h;
        stateVector[1] += stateVector[3] * h;

        // Check if the velocity is low enough to consider the ball stopped
        if (Math.sqrt(stateVector[2] * stateVector[2] + stateVector[3] * stateVector[3]) < LIMIT_ZERO) {
            stateVector[2] = 0;
            stateVector[3] = 0;
        }
    }

    private boolean isWithinSandArea(double x, double z) {

        float sand = TerrainV2.getSandHeight((float) x+50, (float) z+50);

        if(sand > 0.5) {
           //System.out.println("x: " + x + ",z: " + z + " SAND:" + sand);
            return true;
        }
        //System.out.println("x: " + x + ",z: " + z + " GRASS " + sand);
        return false;
    }

    public static double calculateDerivativeX(double x, double y) {
        double forwardHeight = GetHeight.getHeight(heightFunction, x + LIMIT_ZERO, y);
        double backwardHeight = GetHeight.getHeight(heightFunction, x - LIMIT_ZERO, y);
        return (forwardHeight - backwardHeight) / (2 * LIMIT_ZERO);
    }

    public static double calculateDerivativeZ(double x, double z) {
        double forwardHeight = GetHeight.getHeight(heightFunction, x, z + LIMIT_ZERO);
        double backwardHeight = GetHeight.getHeight(heightFunction, x, z - LIMIT_ZERO);
        return (forwardHeight - backwardHeight) / (2 * LIMIT_ZERO);
    }

    public double[] runSingleStep(Vector3 ballPosition, Vector3 ballVelocity) {
        // filling state vectors with values
        stateVector[0] = ballPosition.x;
        stateVector[1] = ballPosition.z;
        stateVector[2] = ballVelocity.x;
        stateVector[3] = ballVelocity.z;

        double time = 0.0;

        // updating state vectors
        updateStateVectorRungeKutta(currentTime, false);

        // checking if any of the states in NaN
        boolean hasNaN = false;
        for (double value : stateVector) {
            if (Double.isNaN(value)) {
                hasNaN = true;
                break;
            }
        }

        // If any value is NaN, revert to the previous valid state
        if (hasNaN) {
            stateVector[0] = ballPosition.x;
            stateVector[1] = ballPosition.z;
            stateVector[2] = ballVelocity.x;
            stateVector[3] = ballVelocity.z;
        }

        // Update height to keep the ball on the terrain
        terrainHeight = (float) GetHeight.getHeight(heightFunction, ballPosition.x, ballPosition.z);

        currentTime += h;
        return stateVector;
    }

    public double[] getStateVector() {
        return stateVector;
    }
}
