package edu.kit.aifb.cass.shared;

/**
 * <p>
 * FieldVerifier validates that the tweet is valid.
 * </p>
 */
public class FieldVerifier {

	/**
	 * Verifies that the tweet is not longer than 140 characters.
	 * 
	 * @param name the name to validate
	 * @return true if valid, false if invalid
	 */
	public static boolean isValidTweet(String name) {
		if (name == null) {
			return false;
		}
		return name.length() < 141;
	}
}
