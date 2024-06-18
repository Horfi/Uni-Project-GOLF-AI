import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

// regarding reverse polish notation as input before and after operator use spaces if you want to have negative number use "-6" with only space in front
// use sqrt as normal operator with spaces after and in front the same with ( and )
public class HandleDerivatives {
    private List<String> derivativesString;
    private double time;

    HandleDerivatives(List<String> derivatives, double time) {
        this.derivativesString = derivatives;
        this.time = time;
    }

    // receives a state so array containing values for all dimensions(derivatives) for every variable
    public double[] calculate(double[] state) {
        double[] derivativesArray = new double[state.length];
        for (int i = 0; i < state.length; i++) {
            String derString = derivativesString.get(i);
            String replacedDerString = replaceVariables(derString, state);

            // Check if replacedDerString is null
            if (replacedDerString == null) {
                // Handle the error condition or return a default value
                System.err.println("An error occurred while replacing variables in the derivative expression.");
                return null;
            }

            List<String> rpn = str_to_RPN(replacedDerString);

            // Check if rpn is null
            if (rpn == null) {
                // Return a default value or handle the error condition
                return null;
            }

            derivativesArray[i] = rpn_to_double(rpn);
        }
        return derivativesArray;
    }

    // replace variables in derivative expression with values assigned to them in map
    public String replaceVariables(String derivative, double[] state) {
        String[] vari = { "a", "b", "c", "d", "e", "f", "g", "h", "i", "j" };

        derivative = derivative.replaceAll("time", String.valueOf(time));
        derivative = derivative.replaceAll("E", String.valueOf(Math.E));

        for (int i = 0; i < state.length; i++) {
            derivative = derivative.replaceAll(vari[i], String.valueOf(state[i]));
        }
        return derivative;
    }

    // polish reversed notation
    public List<String> str_to_RPN(String derivative) {
        derivative = derivative.trim();
        String[] tokens = derivative.split("\\s+"); // create tokens
        List<String> rpn = new LinkedList<>(); // contain rpn notation
        Stack<String> operators = new Stack<>(); // contain operators temporarily

        for (String token : tokens) { // iterate over all tokens
            token = token.trim();

            if (isDouble(token)) { // if token is a double add to list
                rpn.add(token);
            } else if (token.equals("(")) { // if token is ( add to stack
                operators.push(token);
            } else if (token.equals(")")) { // if token is )
                while (!operators.isEmpty() && !operators.peek().equals("(")) { // add all operators to rpn until token ( is found or empty stack
                    rpn.add(operators.pop()); // add operator to rpn
                }
                if (!operators.isEmpty()) { // if operator is ( we remove it from operators stack because we emptied operators in ()
                    operators.pop();
                }
            } else { // if token is an operator so + - / * ^ or sqrt
                while (!operators.isEmpty() && priority(operators.peek()) >= priority(token)) { // if operators stack is not empty and if a token is higher or equal priority as top operator on stack we remove the top operator and add it to rpn list
                    rpn.add(operators.pop());
                }
                operators.push(token); // add tokens to operators stack if top operator is of lower priority then token because we didn't go through the if statement
            }
        }
        while (!operators.isEmpty()) {
            rpn.add(operators.pop()); // add every operator that is left to rpn list
        }
        return rpn;
    }

    private int priority(String operator) {
        switch (operator) {
            case "sqrt":
            case "log":
            case "ln":
            case "sin":
            case "cos":
                return 4;
            case "^":
                return 3;
            case "*":
            case "/":
                return 2;
            case "+":
            case "-":
                return 1;
            default:
                return -1;
        }
    }

    private static boolean isDouble(String text) {
        try {
            Double.parseDouble(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // rpn solver
    public double rpn_to_double(List<String> rpn) {
        Stack<Double> stack = new Stack<>();

        for (String token : rpn) {
            switch (token) {
                case "sin":
                    if (stack.isEmpty())
                        throw new IllegalArgumentException("Insufficient operands for sin");
                    stack.push(Math.sin(Math.toRadians(stack.pop()))); // the angle is in degrees and converts to radians
                    break;
                case "cos":
                    if (stack.isEmpty())
                        throw new IllegalArgumentException("Insufficient operands for cos");
                    stack.push(Math.cos(Math.toRadians(stack.pop()))); // the angle is in degrees and converts to radians
                    break;
                case "ln":
                    if (stack.isEmpty())
                        throw new IllegalArgumentException("Insufficient operands for log");
                    double value = stack.pop();
                    if (value <= 0)
                        throw new IllegalArgumentException("Logarithm of non-positive number");
                    stack.push(Math.log(value)); // Natural logarithm base
                    break;
                case "log":
                    if (stack.isEmpty())
                        throw new IllegalArgumentException("Insufficient operands for log");
                    double v = stack.pop();
                    if (v <= 0)
                        throw new IllegalArgumentException("Logarithm of non-positive number");
                    stack.push(Math.log10(v)); // logarithm base 10
                    break;
                case "sqrt":
                    if (stack.isEmpty())
                        throw new IllegalArgumentException("Insufficient operands for sqrt");
                    stack.push(Math.sqrt(stack.pop()));
                    break;
                case "^":
                    if (stack.size() < 2)
                        throw new IllegalArgumentException("Insufficient operands for ^");
                    double expo = stack.pop();
                    stack.push(Math.pow(stack.pop(), expo));
                    break;
                case "*":
                    if (stack.size() < 2)
                        throw new IllegalArgumentException("Insufficient operands for *");
                    stack.push(stack.pop() * stack.pop());
                    break;
                case "/":
                    if (stack.size() < 2)
                        throw new IllegalArgumentException("Insufficient operands for /");
                    double devider = stack.pop();
                    stack.push((double) stack.pop() / (double) devider);
                    break;
                case "+":
                    if (stack.size() < 2)
                        throw new IllegalArgumentException("Insufficient operands for +");
                    stack.push(stack.pop() + stack.pop());
                    break;
                case "-":
                    if (stack.size() < 2)
                        throw new IllegalArgumentException("Insufficient operands for -");
                    double a = stack.pop();
                    double b = stack.pop();
                    stack.push(b - a);
                    break;
                default:
                    stack.push(Double.parseDouble(token)); // from str to double
                    break;
            }
        }
        if (stack.size() != 1) {
            throw new IllegalArgumentException("Invalid RPN expression");
        }
        return stack.pop();
    }
}