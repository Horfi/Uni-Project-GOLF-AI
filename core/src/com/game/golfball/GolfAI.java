package com.game.golfball;

import com.badlogic.gdx.math.Vector3;
import com.game.terrain.GameRules;
import com.game.terrain.GetHeight;
import com.game.terrain.Maze.Wall;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class GolfAI {
    private Vector3 targetPosition;
    private PhysicsEngine physicsEngine;
    private GolfBall AIball;
    private static final double EPSILON_ADAM = 1e-8; // Epsilon for Adam optimizer to ensure numerical stability
    private static final double INITIAL_EPSILON_GRAD = 0.5; // Initial epsilon for gradient approximation
    private static final double MIN_EPSILON_GRAD = 1e-4; // Minimum epsilon for gradient approximation
    private static final int MAX_ITERATIONS = 500; // Max iterations for convergence
    private static final double INITIAL_LEARNING_RATE = 1; // Initial learning rate for large steps
    private static final double MIN_LEARNING_RATE = 0.005; // Minimum learning rate for fine adjustments
    private static final double TOLERANCE = 0.05; // Tolerance for stopping condition
    private static final double BETA1 = 0.9; // Decay rate for the first moment estimate
    private static final double BETA2 = 0.999; // Decay rate for the second moment estimate

    private Vector3 m; // First moment vector
    private Vector3 v; // Second moment vector
    private double learningRate; // Current learning rate
    private double epsilonGrad; // Current epsilon for gradient approximation
    private int t; // Time step
    private GameRules gameRules; // Game rules
    private List<Wall> walls; // List of walls
    private Queue<Vector3> pathSegments; // Queue to hold the path segments

    /**
     * Constructs a GolfAI object with the specified parameters
     *
     * @param AIball         The golf ball controlled by the AI
     * @param targetPosition The target position for the golf ball
     * @param physicsEngine  The physics engine used for simulations
     * @param gameRules      The game rules defining constraints and objectives
     * @param walls          The list of walls in the maze
     */
    public GolfAI(GolfBall AIball, Vector3 targetPosition, PhysicsEngine physicsEngine, GameRules gameRules, List<Wall> walls) {
        this.AIball = AIball;
        this.targetPosition = targetPosition;
        this.physicsEngine = physicsEngine;
        this.m = new Vector3(0, 0, 0);
        this.v = new Vector3(0, 0, 0);
        this.learningRate = INITIAL_LEARNING_RATE;
        this.epsilonGrad = INITIAL_EPSILON_GRAD;
        this.t = 0;
        if (gameRules == null) {
            throw new IllegalArgumentException("gameRules cannot be null");
        }
        this.gameRules = gameRules;
        this.walls = walls;
        this.pathSegments = new LinkedList<>();
    }

    /**
     * Calculates the best shot (figure out perfect velocity) for the golf ball to reach the target position
     *
     * @return The velocity vector representing the best shot
     */
    public Vector3 findBestShot() {
        Vector3 currentVelocity = new Vector3(2f, 0f, -6f);
        Vector3 bestVelocity = new Vector3(currentVelocity);
        float lowestDeviation = Float.MAX_VALUE;

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            Vector3 deviation = calculateFunction(currentVelocity);

            if (deviation.len() < lowestDeviation) {
                lowestDeviation = deviation.len();
                bestVelocity.set(currentVelocity);
            }

            if (deviation.len() < TOLERANCE) {
                System.out.println("Convergence achieved.");
                break;
            }

            Vector3 gradient = approximateGradient(currentVelocity, deviation);

            t++;

            m.x = (float) (BETA1 * m.x + (1 - BETA1) * gradient.x);
            m.z = (float) (BETA1 * m.z + (1 - BETA1) * gradient.z);

            v.x = (float) (BETA2 * v.x + (1 - BETA2) * gradient.x * gradient.x);
            v.z = (float) (BETA2 * v.z + (1 - BETA2) * gradient.z * gradient.z);

            float mHatX = (float) (m.x / (1 - Math.pow(BETA1, t)));
            float mHatZ = (float) (m.z / (1 - Math.pow(BETA1, t)));

            float vHatX = (float) (v.x / (1 - Math.pow(BETA2, t)));
            float vHatZ = (float) (v.z / (1 - Math.pow(BETA2, t)));

            // Update velocity with momentum
            currentVelocity.x -= learningRate * mHatX / (Math.sqrt(vHatX) + EPSILON_ADAM);
            currentVelocity.z -= learningRate * mHatZ / (Math.sqrt(vHatZ) + EPSILON_ADAM);

            // Gradient and velocity clipping to prevent overshooting
            gradientClip(gradient);
            clipVelocity(currentVelocity);

            // Update learning rate and epsilon for gradient approximation
            learningRate = Math.max(MIN_LEARNING_RATE, learningRate * 0.98);
            epsilonGrad = Math.max(MIN_EPSILON_GRAD, epsilonGrad * 0.99);
        }

        return bestVelocity;
    }

    /**
     * Calculates the function representing the deviation between the current shot
     * and the target position.
     *
     * @param velocity The velocity vector representing the current shot
     * @return The deviation vector between the current shot and the target position.
     */
    private Vector3 calculateFunction(Vector3 velocity) {
        physicsEngine.setState(AIball.getPosition().x, AIball.getPosition().z, velocity.x, velocity.z);
        double[] afterShot = physicsEngine.runSimulation(velocity.x, velocity.z);
        Vector3 finalPosition = new Vector3((float) afterShot[0], 0, (float) afterShot[1]);
        return new Vector3(finalPosition.x - targetPosition.x, 0, finalPosition.z - targetPosition.z);
    }

    /**
     * Approximates the gradient of the deviation function with respect to the velocity
     *
     * @param velocity The velocity vector representing the current shot
     * @param originalDeviation The original deviation between the current shot and the target position
     * @return The gradient vector approximating the rate of change of deviation with respect to velocity
     */
    private Vector3 approximateGradient(Vector3 velocity, Vector3 originalDeviation) {
        Vector3 gradient = new Vector3();

        // Perturbation for x velocity
        Vector3 perturbedVelocityX = new Vector3(velocity);
        perturbedVelocityX.x += epsilonGrad;
        Vector3 deviationX = calculateFunction(perturbedVelocityX);
        gradient.x = (float) ((deviationX.len() - originalDeviation.len()) / epsilonGrad);

        // Perturbation for z velocity
        Vector3 perturbedVelocityZ = new Vector3(velocity);
        perturbedVelocityZ.z += epsilonGrad;
        Vector3 deviationZ = calculateFunction(perturbedVelocityZ);
        gradient.z = (float) ((deviationZ.len() - originalDeviation.len()) / epsilonGrad);

        return gradient;
    }

    /**
     * Clips the gradient vector to prevent overshooting during optimization
     *
     * @param gradient gradient vector to be clipped
     */
    private void gradientClip(Vector3 gradient) {
        // Clipping the gradient to ensure it doesn't overshoot
        double maxGradient = 2.0; // Example value, adjust as needed
        if (gradient.len() > maxGradient) {
            gradient.scl((float) (maxGradient / gradient.len()));
        }
    }

    public void updateTarget(Vector3 newTarget) {
        targetPosition.x = newTarget.z;
        targetPosition.z = newTarget.x;
    }

    /**
     * Sets the path segments for the A* bot to follow
     *
     * @param pathSegments The list of path segments
     */
    public void setPathSegments(List<Vector3> pathSegments) {
        this.pathSegments = new LinkedList<>(pathSegments);
    }

    /**
     * Clips the velocity vector to prevent overshooting during optimization
     *
     * @param velocity The velocity vector to be clipped
     */
    private void clipVelocity(Vector3 velocity) {
        double maxVelocity = 70.0;
        if (velocity.len() > maxVelocity) {
            velocity.scl((float) (maxVelocity / velocity.len()));
        }
    }

    /**
     * Updates the position and velocity of the golf ball according to the calculated shot
     */
    public void update(GolfBall ball) {
        Vector3 currentPosition = ball.getPosition();
        Vector3 currentVelocity = ball.getVelocity();
    
        if (!isVelocityEffectivelyZero(currentVelocity)) {
            physicsEngine.setState(currentPosition.x, currentPosition.z, currentVelocity.x, currentVelocity.z);
            double[] newState = physicsEngine.runSingleStep(currentPosition, currentVelocity);
    
            // Update ball position and velocity
            Vector3 newPosition = new Vector3((float) newState[0], ball.getPosition().y, (float) newState[1]);
            Vector3 newVelocity = new Vector3((float) newState[2], 0, (float) newState[3]);
    
            // Check for collisions and apply bounce logic
            if (walls != null) {
                newVelocity = Bouncing.detectCollisionAndBounce(newPosition, newVelocity, walls);
            }
    
            ball.setPosition(newPosition);
            ball.setVelocity(newVelocity);
            ball.getPosition().y = (float) GetHeight.getHeight(PhysicsEngine.heightFunction, ball.getPosition().x, ball.getPosition().z);
    
            // Log the ball state after each update
            // System.out.println("Updated Ball State - Position: " + ball.getPosition() + ", Velocity: " + ball.getVelocity());
        } else if (!pathSegments.isEmpty()) {
            Vector3 nextTarget = pathSegments.poll();
            System.out.println("Next Target: " + nextTarget);
            System.err.println(" ball position: "+ball.getPosition()+" expected segment postion : "+ nextTarget);
            updateTarget(nextTarget);
            reset(); // Reset parameters before finding the best shot
            Vector3 aiShot = findBestShot();
            ball.setVelocity(aiShot);
        }
    }
    
    /**
     * Retrieves the next path segment from the queue
     *
     * @return The next path segment
     */
    public Vector3 getNextPathSegment() {
        return pathSegments.poll();
    }

    public boolean isVelocityEffectivelyZero(Vector3 velocity) {
        return velocity.len() < 0.04;
    }

    public boolean hasReachedTarget(Vector3 currentPosition, Vector3 targetPosition, float tolerance) {
        return currentPosition.dst(targetPosition) < tolerance;
    }
    

    /**
     * Gets the remaining path segments
     *
     * @return The list of remaining path segments
     */
    public Queue<Vector3> getPathSegments() {
        return pathSegments;
    }

    public void reset() {
        this.m.set(0, 0, 0);
        this.v.set(0, 0, 0);
        this.learningRate = INITIAL_LEARNING_RATE;
        this.epsilonGrad = INITIAL_EPSILON_GRAD;
        this.t = 0;
    }
}
