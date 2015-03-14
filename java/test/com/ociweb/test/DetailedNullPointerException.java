package com.ociweb.test;

/**
 * A more detailed version of NullPointerException that contains
 * information about what argument was null.
 *
 * @version  $Revision: 1.4 $
 * @author  <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 */
public class DetailedNullPointerException extends NullPointerException {
    private static final long serialVersionUID = -2554279092032000569L;
	private final String argumentName_;


    /**
     * Create an instance
     *
     * @param argumentName The name of the argument that was null
     * @param message The message to use in the exception
     */
    public DetailedNullPointerException( final String argumentName, final String message ) {

        super(message);
        argumentName_ = argumentName;
    }


    /**
     * Create an instance
     *
     * @param argumentName The name of the argument that was null
     */
    public DetailedNullPointerException( final String argumentName ) {
        this( argumentName, argumentName );
    }


    /**
     * Return the name of the argument that was null.
     * @return The name of the argument
     */
    public String getArgumentName() {
        return argumentName_;
    }
}

