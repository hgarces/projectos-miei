/**
 * @author Andre Correia, 41626
 * @author Henrique Garces, 42077
 */

public class ObliqueProjection {

	private double alpha;
	private double l;
	
	/**
	 * a matrix no opengl segue esta ordem:
	 * m[0] m[4] m[8]  m[12]
	 * m[1] m[5] m[9]  m[13]
	 * m[2] m[6] m[10] m[14]
	 * m[3] m[7] m[11] m[15]
	 */
	private double[] matrix = {	1, 0, 1, 0,
								0, 1, 0, 0,
							   -1, 0, 1, 0,
								0, 0, 0, 1 };
	
	
	public ObliqueProjection(double alpha, double l) {
		this.alpha = Math.toRadians(alpha);
		this.l = l;
		calcMatrix();
	}
	
	private void calcMatrix() {
		matrix[8] = -l * Math.cos(alpha);
		matrix[9] = -l * Math.sin(alpha);
	}
	
	public void setAlpha(double a) {
		alpha = Math.toRadians(a);
	}
	
	public void setL(double l) {
		this.l = l;
	}
	
	public double getAlpha() {
		return Math.toDegrees(alpha);
	}
	
	public double getL() {
		return l;
	}
	
	public double[] getMatrix() {
		calcMatrix();
		return matrix;
	}
}
