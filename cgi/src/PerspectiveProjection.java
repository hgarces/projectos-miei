/**
 * @author Andre Correia, 41626
 * @author Henrique Garces, 42077
 */

public class PerspectiveProjection {
	
	private double d;
	
	/**
	 * a matrix no opengl segue esta ordem:
	 * m[0] m[4] m[8]  m[12]
	 * m[1] m[5] m[9]  m[13]
	 * m[2] m[6] m[10] m[14]
	 * m[3] m[7] m[11] m[15]
	 */
	private double[] matrix = {	1, 0, 0, 0,
								0, 1, 0, 0,
							    0, 0, 0, 0,
								0, 0, 1, 1 };
	
	public PerspectiveProjection(double d) {
		this.d = d;
	}
	
	public double getD() {
		return d;
	}
	
	public void setD(double d) {
		this.d = d;
	}
	
	private void calcMatrix() {
		matrix[11] = -1/d;
	}
	
	public double[] getMatrix() {
		calcMatrix();
		return matrix;
	}

}
